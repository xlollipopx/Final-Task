package com.portal.service

import cats.effect.Sync
import com.portal.domain.product.ProductStatus
import com.portal.dto.product._
import com.portal.repository.ProductItemRepository
import com.portal.service.impl.ProductItemServiceImpl
import com.portal.validation.{ProductItemValidator, ProductValidationError}

import java.util.UUID

trait ProductItemService[F[_]] {
  def all: F[List[ProductItemWithIdCategoriesDto]]
  def findById(id: UUID): F[Option[ProductItemWithCategoriesDto]]
  def create(
    item: ProductItemWithCategoriesDtoModify
  ): F[Either[ProductValidationError, ProductItemWithCategoriesDtoModify]]

  def update(
    id:   UUID,
    item: ProductItemWithCategoriesDtoModify
  ): F[Either[ProductValidationError, ProductItemWithCategoriesDtoModify]]

  def delete(id:    UUID): F[Boolean]
  def setStatus(id: UUID, status: String): F[Either[ProductValidationError, ProductStatus]]

  def searchByCriteria(
    item: ProductItemSearchDto
  ): F[Either[ProductValidationError, List[ProductItemWithIdCategoriesDto]]]

}
object ProductItemService {
  def of[F[_]: Sync](
    productItemRepository: ProductItemRepository[F],
    validator:             ProductItemValidator
  ): ProductItemService[F] =
    new ProductItemServiceImpl[F](productItemRepository, validator)
}
