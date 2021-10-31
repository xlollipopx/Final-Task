package com.portal.repository

import cats.effect.Sync
import com.portal.domain.product._
import com.portal.repository.impl.doobie.DoobieProductRepository
import doobie.Transactor

import java.util.UUID

trait ProductItemRepository[F[_]] {

  def all(): F[List[ProductItemWithCategories]]
  def create(item:           ProductItemWithCategoriesModify): F[Int]
  def findById(id:           UUID): F[Option[ProductItemWithCategories]]
  def setStatus(id:          UUID, status: ProductStatus): F[Int]
  def update(item:           ProductItemWithCategoriesModify): F[Int]
  def delete(id:             UUID):                            F[Int]
  def searchByCriteria(item: ProductItemSearch):               F[List[ProductItemWithCategories]]
}

object ProductItemRepository {
  def of[F[_]: Sync](tx: Transactor[F]): DoobieProductRepository[F] =
    new DoobieProductRepository[F](tx)
}
