package com.portal.service

import cats.effect.Sync
import com.portal.repository.SubscriptionRepository
import com.portal.service.impl.SubscriptionServiceImpl

import java.util.UUID

trait SubscriptionService[F[_]] {
  def createSupplierSubscription(userId: UUID, supplierId: UUID): F[Boolean]
  def deleteSupplierSubscription(userId: UUID, supplierId: UUID): F[Boolean]
  def createCategorySubscription(userId: UUID, categoryId: UUID): F[Boolean]
  def deleteCategorySubscription(userId: UUID, categoryId: UUID): F[Boolean]
}
object SubscriptionService {
  def of[F[_]: Sync](
    subscriptionRepository: SubscriptionRepository[F],
  ): SubscriptionServiceImpl[F] =
    new SubscriptionServiceImpl[F](subscriptionRepository)
}
