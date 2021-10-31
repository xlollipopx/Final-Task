package com.portal.repository.impl.doobie

import cats.Functor
import cats.effect.Bracket
import com.portal.repository.SubscriptionRepository
import doobie.Transactor
import doobie.{Fragment, Transactor}
import doobie.postgres.implicits._
import doobie.implicits._
import doobie.implicits.javatime._
import meta.implicits._

import java.util.UUID

class DoobieSubscriptionRepository[F[_]: Functor: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends SubscriptionRepository[F] {

  val createSupplierSubscription = fr"INSERT INTO suppliers"
  val deleteSupplierSubscription = fr"DELETE FROM suppliers"
  val createCategorySubscription = fr"INSERT INTO categories"
  val deleteCategorySubscription = fr"INSERT INTO categories"

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
}
