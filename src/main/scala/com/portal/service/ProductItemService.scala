package com.portal.service

import cats.effect.Sync
import com.portal.dto.product.ProductItemWithCategoriesDto
import com.portal.repository.ProductItemRepository
import com.portal.service.impl.ProductItemServiceImpl
import com.portal.validation.{ProductItemValidator, ProductValidationError}

import java.util.UUID

trait ProductItemService[F[_]] {
  def all: F[List[ProductItemWithCategoriesDto]]
  def findById(id: UUID):                         F[Option[ProductItemWithCategoriesDto]]
  def create(item: ProductItemWithCategoriesDto): F[Either[ProductValidationError, ProductItemWithCategoriesDto]]
}
object ProductItemService {
  def of[F[_]: Sync](
    productItemRepository: ProductItemRepository[F],
    validator:             ProductItemValidator
  ): ProductItemService[F] =
    new ProductItemServiceImpl[F](productItemRepository, validator)
}
