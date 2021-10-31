package com.portal.repository

import cats.effect.Sync
import com.portal.domain.auth.UserId
import com.portal.domain.order.{Order, OrderId, OrderStatus, OrderWithProducts}
import com.portal.domain.product.ProductItemId
import com.portal.repository.impl.doobie.DoobieOrderRepository
import doobie.Transactor

import java.util.UUID

trait OrderRepository[F[_]] {
  def all(): F[List[OrderWithProducts]]
  def addProductToOrder(productId:        ProductItemId, userId: UserId, quantity: Int): F[Boolean]
  def getOrderByUserId(userId:            UserId): F[Option[OrderWithProducts]]
  def makeOrder(userId:                   UserId, address:    String): F[Boolean]
  def assignToCourier(orderId:            OrderId, courierId: UserId): F[Boolean]
  def setDelivered(orderId:               OrderId, courierId: UserId): F[Boolean]
  def getCourierAssignedOrders(courierId: UserId): F[List[OrderWithProducts]]
}

object OrderRepository {
  def of[F[_]: Sync](tx: Transactor[F]): DoobieOrderRepository[F] =
    new DoobieOrderRepository[F](tx)
}
