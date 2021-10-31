package com.portal.repository

import cats.effect.Sync
import com.portal.domain.category.Category
import com.portal.dto.product.CategoryDto
import com.portal.repository.impl.doobie.DoobieCategoryRepository
import doobie.Transactor

trait CategoryRepository[F[_]] {
  def all(): F[List[Category]]
  def create(category: Category): F[Int]
  def update(category: Category): F[Int]
}

object CategoryRepository {
  def of[F[_]: Sync](tx: Transactor[F]): DoobieCategoryRepository[F] = new DoobieCategoryRepository[F](tx)
}
