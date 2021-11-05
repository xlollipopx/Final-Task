package com.portal.service.impl

import cats.Monad
import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import com.portal.domain.product._
import com.portal.dto.product._
import com.portal.repository.ProductItemRepository
import com.portal.service.ProductItemService
import com.portal.util.ModelMapper._
import com.portal.validation.ProductSearchValidation.validateProductItemSearch
import com.portal.validation.{ProductItemValidator, ProductValidationError}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import java.util.UUID

class ProductItemServiceImpl[F[_]: Sync: Monad](
  productItemRepository: ProductItemRepository[F],
  validator:             ProductItemValidator
) extends ProductItemService[F] {

  override def all: F[List[ProductItemWithIdCategoriesDto]] = for {
    products <- productItemRepository.all()
  } yield products.map(ProductItemWithCategoriesDomainToDtoWithId)

  override def findById(id: UUID): F[Option[ProductItemWithCategoriesDto]] = for {
    product <- productItemRepository.findById(id)
  } yield product.map(ProductItemWithCategoriesDomainToDto)

  override def create(
    item: ProductItemWithCategoriesDtoModify
  ): F[Either[ProductValidationError, ProductItemWithCategoriesDtoModify]] = {
    val result: EitherT[F, ProductValidationError, ProductItemWithCategoriesDtoModify] = for {
      x                                                            <- EitherT(validator.validateProductModify(item).pure[F])
      logger                                                       <- EitherT.liftF(Slf4jLogger.create[F])
      _                                                            <- EitherT.liftF(logger.info("Creating product item..."))
      (name, description, cost, date, status, supplier, categories) = x
      productItem =
        ProductItem(ProductItemId(UUID.randomUUID()), name, description, cost, date, status, supplier)
      domain = ProductItemWithCategoriesModify(productItem, categories)

      _ <- EitherT.liftF(productItemRepository.create(domain))
    } yield ProductWithCategoriesModifyDomainToDto(domain)

    result.value
  }

  override def update(
    id:   UUID,
    item: ProductItemWithCategoriesDtoModify
  ): F[Either[ProductValidationError, ProductItemWithCategoriesDtoModify]] = {

    val result: EitherT[F, ProductValidationError, ProductItemWithCategoriesDtoModify] = for {

      x                                                            <- EitherT(validator.validateProductModify(item).pure[F])
      logger                                                       <- EitherT.liftF(Slf4jLogger.create[F])
      _                                                            <- EitherT.liftF(logger.info("Updating product item..."))
      (name, description, cost, date, status, supplier, categories) = x
      productItem =
        ProductItem(ProductItemId(id), name, description, cost, date, status, supplier)
      domain = ProductItemWithCategoriesModify(productItem, categories)

      _ <- EitherT.liftF(productItemRepository.update(domain))
    } yield ProductWithCategoriesModifyDomainToDto(domain)

    result.value
  }

  override def delete(id: UUID): F[Boolean] = for {
    cnt    <- (productItemRepository.delete(id))
    logger <- Slf4jLogger.create[F]
    _      <- logger.info("Deleting product item...")
    res     = if (cnt == 1) true else false
  } yield res

  override def setStatus(
    id:     UUID,
    status: String
  ): F[Either[ProductValidationError, ProductStatus]] = {
    val result: EitherT[F, ProductValidationError, ProductStatus] = for {

      x <- EitherT(validator.validateStatus(status).pure[F])
      _ <- EitherT.liftF(productItemRepository.setStatus(id, x))
    } yield x

    result.value
  }

  override def searchByCriteria(
    item: ProductItemSearchDto
  ): F[Either[ProductValidationError, List[ProductItemWithIdCategoriesDto]]] = {
    val result: EitherT[F, ProductValidationError, List[ProductItemWithIdCategoriesDto]] = for {

      domain <- EitherT(validateProductItemSearch(item).pure[F])

      res <- EitherT.liftF(productItemRepository.searchByCriteria(domain))
    } yield res.map(ProductItemWithCategoriesDomainToDtoWithId)

    result.value
  }
}
