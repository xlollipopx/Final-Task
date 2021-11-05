package com.portal.repository.impl.doobie

import cats.Functor
import cats.effect.Bracket
import com.portal.domain.supplier
import com.portal.repository.SubscriptionRepository
import doobie.Transactor
import doobie.{Fragment, Transactor}
import doobie.postgres.implicits._
import doobie.implicits._
import doobie.implicits.javatime._
import meta.implicits._
import cats.implicits._
import com.portal.domain.auth.Email
import com.portal.domain.supplier.{Supplier, SupplierWithUsers}

import java.time.LocalDate
import java.util.UUID

class DoobieSubscriptionRepository[F[_]: Functor: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends SubscriptionRepository[F] {

  val createSupplierSubscription = fr"INSERT INTO user_supplier_subscriptions"
  val deleteSupplierSubscription = fr"DELETE FROM user_supplier_subscriptions"
  val createCategorySubscription = fr"INSERT INTO user_category_subscriptions"
  val deleteCategorySubscription = fr"INSERT INTO user_category_subscriptions"

  override def createSupplierSubscription(userId: UUID, supplierId: UUID): F[Int] =
    (createSupplierSubscription ++
      fr"VALUES(${userId}, ${supplierId})").update.run.transact(tx)

  override def deleteSupplierSubscription(userId: UUID, supplierId: UUID): F[Int] =
    (deleteSupplierSubscription ++
      fr"WHERE user_id = $userId AND supplier_id = $supplierId").update.run.transact(tx)

  override def createCategorySubscription(userId: UUID, categoryId: UUID): F[Int] =
    (createCategorySubscription ++
      fr"VALUES(${userId}, ${categoryId})").update.run.transact(tx)

  override def deleteCategorySubscription(userId: UUID, categoryId: UUID): F[Int] =
    (deleteCategorySubscription ++
      fr"WHERE user_id = $userId AND category_id = $categoryId").update.run.transact(tx)

  override def getSuppliersWithUsers(date: LocalDate): F[List[SupplierWithUsers]] = for {
    sps <- (fr"SELECT DISTINCT s.uuid, s.name FROM products" ++
      fr"AS p" ++
      fr"INNER JOIN suppliers AS s ON p.supplier_id = s.uuid" ++
      fr"WHERE publication_date = ${date}").query[Supplier].to[List].transact(tx)
    res <- sps
      .map(l =>
        (fr"SELECT u.mail FROM user_supplier_subscriptions" ++
          fr"AS uss" ++
          fr"INNER JOIN users as u ON uss.user_id = u.uuid")
          .query[Email]
          .to[List]
          .transact(tx)
          .flatMap(x => SupplierWithUsers(l, x).pure[F])
      )
      .traverse(identity)
  } yield res
}
