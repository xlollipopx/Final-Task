package com.portal.repository

import java.util.UUID

trait SubscriptionRepository[F[_]] {
  def createSupplierSubscription(userId: UUID, supplierId: UUID): F[Int]
  def deleteSupplierSubscription(userId: UUID, supplierId: UUID): F[Int]
  def createCategorySubscription(userId: UUID, categoryId: UUID): F[Int]
  def deleteCategorySubscription(userId: UUID, categoryId: UUID): F[Int]

}

object SubscriptionRepository {}
