package com.portal.repository

import cats.effect.Sync
import com.portal.domain.product._
import com.portal.repository.impl.doobie.DoobieProductRepository
import doobie.Transactor

import java.util.UUID

trait ProductItemRepository[F[_]] {

  def all(): F[List[ProductItemWithCategories]]
  def create(product:   ProductItemWithCategories): F[Int]
  def findById(id:      UUID):                      F[Option[ProductItemWithCategories]]
  def setStatus(status: ProductStatus):             F[Int]
  def update(product:   ProductItem):               F[Int]
  def delete(product:   ProductItem):               F[Int]
}

object ProductItemRepository {
  def of[F[_]: Sync](tx: Transactor[F]): DoobieProductRepository[F] =
    new DoobieProductRepository[F](tx)
}
