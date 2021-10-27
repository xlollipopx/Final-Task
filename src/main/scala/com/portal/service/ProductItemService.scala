package com.portal.service

import cats.effect.Sync
import com.portal.domain.product.ProductStatus
import com.portal.dto.product.ProductItemWithCategoriesDto
import com.portal.repository.ProductItemRepository
import com.portal.service.impl.ProductItemServiceImpl
import com.portal.validation.{ProductItemValidator, ProductValidationError}

import java.util.UUID

trait ProductItemService[F[_]] {
  def all: F[List[ProductItemWithCategoriesDto]]
  def findById(id:  UUID):                         F[Option[ProductItemWithCategoriesDto]]
  def create(item:  ProductItemWithCategoriesDto): F[Either[ProductValidationError, ProductItemWithCategoriesDto]]
  def update(item:  ProductItemWithCategoriesDto): F[Either[ProductValidationError, ProductItemWithCategoriesDto]]
  def delete(id:    UUID): F[Boolean]
  def setStatus(id: UUID, status: String): F[Either[ProductValidationError, ProductStatus]]
}
object ProductItemService {
  def of[F[_]: Sync](
    productItemRepository: ProductItemRepository[F],
    validator:             ProductItemValidator
  ): ProductItemService[F] =
    new ProductItemServiceImpl[F](productItemRepository, validator)
}
