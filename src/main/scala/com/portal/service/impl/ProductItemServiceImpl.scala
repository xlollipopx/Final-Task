package com.portal.service.impl

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import cats.effect.Sync
import com.portal.domain.product
import com.portal.domain.product.{ProductItem, ProductItemId, ProductItemWithCategories}
import com.portal.dto.product._
import com.portal.repository.ProductItemRepository
import com.portal.service.ProductItemService
import com.portal.util.ModelMapper._
import com.portal.validation.{ProductItemValidator, ProductValidationError}

import java.util.UUID

class ProductItemServiceImpl[F[_]: Sync: Monad](
  productItemRepository: ProductItemRepository[F],
  validator:             ProductItemValidator
) extends ProductItemService[F] {

  override def all: F[List[ProductItemWithCategoriesDto]] = for {
    products <- productItemRepository.all()
  } yield products.map(ProductItemWithCategoriesDomainToDto)

  override def findById(id: UUID): F[Option[ProductItemWithCategoriesDto]] = for {
    product <- productItemRepository.findById(id)
  } yield product.map(ProductItemWithCategoriesDomainToDto)

  override def create(
    item: ProductItemWithCategoriesDto
  ): F[Either[ProductValidationError, ProductItemWithCategoriesDto]] = {
    val result: EitherT[F, ProductValidationError, ProductItemWithCategoriesDto] = for {

      x                                                            <- EitherT(validator.validate(item).pure[F])
      (name, description, cost, date, status, supplier, categories) = x
      productItem =
        ProductItem(ProductItemId(UUID.randomUUID()), name, description, cost, date, status, supplier)
      domain = ProductItemWithCategories(productItem, categories)

      _ <- EitherT.liftF(productItemRepository.create(domain))
    } yield ProductItemWithCategoriesDomainToDto(domain)

    result.value
  }

}
