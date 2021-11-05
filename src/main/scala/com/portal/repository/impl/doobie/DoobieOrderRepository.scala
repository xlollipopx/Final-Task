package com.portal.repository.impl.doobie

import com.portal.repository.OrderRepository
import cats.Functor
import cats.effect.Bracket
import doobie.{ConnectionIO, Fragment, Transactor}
import doobie.implicits._
import cats.implicits._
import doobie.postgres.implicits._
import meta.implicits._
import cats.effect._
import com.portal.domain.auth.UserId
import com.portal.domain.order.OrderStatus.NotComplete
import com.portal.domain.order.{Order, OrderId, OrderStatus, OrderWithProducts}
import com.portal.domain.product.{ProductItem, ProductItemForOrder, ProductItemId}
import com.portal.domain.{auth, order, product}
import com.portal.effects.GenUUID

import java.util.UUID

class DoobieOrderRepository[F[_]: Functor: Sync: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends OrderRepository[F] {

  private val selectOrder: Fragment =
    fr"SELECT o.uuid, o.status, u.name FROM orders" ++
      fr"AS o" ++
      fr"INNER JOIN users AS u ON o.user_id = u.uuid"

  override def all(): F[List[OrderWithProducts]] = for {
    list <- (selectOrder ++ fr"WHERE o.status = 'ORDERED'").query[Order].to[List].transact(tx)
    ans  <- fetchProducts(list)
  } yield ans

  def getProductsByOrderId(orderId: UUID): F[List[ProductItemForOrder]] = {
    (fr"SELECT p.uuid, p.name, p.cost, p.currency, op.quantity FROM orders_products AS op" ++
      fr"INNER JOIN products AS p ON p.uuid = op.product_id WHERE op.order_id = ${orderId}")
      .query[ProductItemForOrder]
      .to[List]
      .transact(tx)
  }

  override def addProductToOrder(
    productId: ProductItemId,
    userId:    UserId,
    quantity:  Int
  ): F[Boolean] = for {
    l <- fr"SELECT uuid from orders WHERE user_id = ${userId} AND status = 'NOT_COMPLETE'"
      .query[UUID]
      .to[List]
      .transact(tx)
    orderId <- { if (l.isEmpty) createOrder(userId) else l.head.pure[F] }
    b       <- insertIntoOrderProducts(OrderId(orderId), productId, quantity)
    res      = if (b == 1) true else false
  } yield res

  def createOrder(userId: UserId): F[UUID] = for {
    uuid <- GenUUID.forSync[F].make
    _ <- (fr"INSERT INTO orders (uuid, status, user_id)" ++
      fr"VALUES(${uuid}, 'NOT_COMPLETE', ${userId})").update.run.transact(tx)
  } yield uuid

  def insertIntoOrderProducts(orderId: OrderId, productId: ProductItemId, quantity: Int): F[Int] = {
    (fr"INSERT INTO orders_products (order_id, product_id, quantity)" ++
      fr"VALUES(${orderId.value}, ${productId}, ${quantity})").update.run.transact(tx)
  }

  override def assignToCourier(orderId: OrderId, courierId: UserId): F[Boolean] =
    for {
      x <-
        (fr"UPDATE orders SET courier_info_id = $courierId, " ++
          fr"status = 'ASSIGNED' WHERE uuid = $orderId AND status = 'ORDERED'").update.run.transact(tx)
      res <- (if (x == 1) true else false).pure[F]
    } yield res

  override def setDelivered(orderId: OrderId, courierId: UserId): F[Boolean] =
    for {
      x <-
        (fr"UPDATE orders" ++
          fr"SET status = 'DELIVERED' WHERE uuid = $orderId").update.run.transact(tx)
      res <- (if (x == 1) true else false).pure[F]
    } yield res

  override def getOrderByUserId(userId: UserId): F[Option[OrderWithProducts]] = for {
    l <- (selectOrder ++ fr"WHERE o.user_id = $userId AND o.status = 'NOT_COMPLETE'")
      .query[Order]
      .to[List]
      .transact(tx)
    res <- {
      if (l.isEmpty) None.pure[F]
      else {
        for {
          prs <- getProductsByOrderId(l.head.id.value)
          owp  = OrderWithProducts(l.head, prs)
        } yield Option(owp)
      }
    }
  } yield res

  override def makeOrder(userId: UserId, address: String): F[Boolean] = for {
    l <- fr"SELECT uuid from orders WHERE user_id = $userId AND status = 'NOT_COMPLETE'"
      .query[UUID]
      .to[List]
      .transact(tx)
    res <- if (l.isEmpty) false.pure[F] else setOrdered(l.head, address)
  } yield res

  def setOrdered(orderId: UUID, address: String): F[Boolean] = for {
    i <- (fr"UPDATE orders SET status = 'ORDERED'," ++
      fr" user_address = $address WHERE uuid = $orderId").update.run.transact(tx)
    res <- if (i == 1) true.pure[F] else false.pure[F]
  } yield res

  override def getCourierAssignedOrders(courierId: UserId): F[List[OrderWithProducts]] = for {
    list <- (selectOrder ++ fr"WHERE o.courier_info_id = $courierId AND status = 'ASSIGNED'")
      .query[Order]
      .to[List]
      .transact(tx)
    ans <- fetchProducts(list)
  } yield ans

  def fetchProducts(list: List[Order]): F[List[OrderWithProducts]] = for {
    res <- list
      .map(x =>
        for {
          pr <- getProductsByOrderId(x.id.value)
          op  = OrderWithProducts(x, pr)
        } yield op
      )
      .pure[F]
    ans <- res.traverse(identity)
  } yield ans

}
