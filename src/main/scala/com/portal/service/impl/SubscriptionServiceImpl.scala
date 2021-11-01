package com.portal.service.impl

import cats.Monad
import cats.effect.Sync
import com.portal.repository.SubscriptionRepository
import com.portal.service.SubscriptionService
import cats.implicits._
import java.util.UUID

class SubscriptionServiceImpl[F[_]: Sync: Monad](subscriptionRepository: SubscriptionRepository[F])
  extends SubscriptionService[F] {
  override def createSupplierSubscription(userId: UUID, supplierId: UUID): F[Boolean] =
    subscriptionRepository
      .createSupplierSubscription(userId, supplierId)
      .map(x => if (x == 1) true else false)

  override def deleteSupplierSubscription(userId: UUID, supplierId: UUID): F[Boolean] =
    subscriptionRepository
      .deleteSupplierSubscription(userId, supplierId)
      .map(x => if (x == 1) true else false)

  override def createCategorySubscription(userId: UUID, categoryId: UUID): F[Boolean] =
    subscriptionRepository
      .createCategorySubscription(userId, categoryId)
      .map(x => if (x == 1) true else false)

  override def deleteCategorySubscription(userId: UUID, categoryId: UUID): F[Boolean] =
    subscriptionRepository
      .deleteCategorySubscription(userId, categoryId)
      .map(x => if (x == 1) true else false)
}
