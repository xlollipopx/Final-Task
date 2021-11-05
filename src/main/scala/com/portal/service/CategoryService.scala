package com.portal.service

import cats.effect.Sync
import com.portal.dto.product.{CategoryDto, CategoryDtoModify}
import com.portal.repository.CategoryRepository
import com.portal.service.impl.CategoryServiceImpl

import java.util.UUID

trait CategoryService[F[_]] {
  def all(): F[List[CategoryDto]]
  def create(category: CategoryDtoModify): F[CategoryDto]
  def update(id:       UUID, category: CategoryDtoModify): F[CategoryDto]
}

object CategoryService {
  def of[F[_]: Sync](
    categoryRepository: CategoryRepository[F]
  ): CategoryServiceImpl[F] =
    new CategoryServiceImpl[F](categoryRepository)
}
