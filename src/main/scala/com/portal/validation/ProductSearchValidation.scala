package com.portal.validation

import com.portal.domain.category.CategoryId
import com.portal.domain.product.{CostInterval, DateInterval, ProductItemSearch, ProductStatus}
import com.portal.dto.product.{DateIntervalDto, ProductItemSearchDto}
import com.portal.validation.ProductValidationError.InvalidDateFormat

import java.time.LocalDate
import java.util.UUID

object ProductSearchValidation {
  val productValidator = new ProductItemValidator

  def validateProductItemSearch(dto: ProductItemSearchDto) = for {
    date       <- validateLocalDateInterval(dto.publicationDate)
    status     <- validateStatusOption(dto.status)
    supplierId <- validateUuidOption(dto.supplierId)
    categories  = validateCategoriesOption(dto.categoriesId)
  } yield (ProductItemSearch(dto.name, dto.description, dto.cost, date, status, supplierId, categories))

  def validateLocalDateInterval(date: Option[DateIntervalDto]): Either[ProductValidationError, Option[DateInterval]] =
    date match {
      case Some(x) =>
        Either.cond(
          LocalDate.parse(x.min).isInstanceOf[LocalDate] && LocalDate.parse(x.max).isInstanceOf[LocalDate],
          Some(DateInterval(LocalDate.parse(x.min), LocalDate.parse(x.max))),
          InvalidDateFormat
        )
      case None => Right(None)
    }

  def validateStatusOption(status: Option[String]): Either[ProductValidationError, Option[ProductStatus]] =
    status match {
      case Some(x) => productValidator.validateStatus(x).map(Option(_))
      case None    => Right(None)
    }

  def validateUuidOption(id: Option[String]): Either[ProductValidationError, Option[UUID]] =
    id match {
      case Some(x) => productValidator.validateUUID(x).map(Option(_))
      case None    => Right(None)
    }

  def validateCategoriesOption(list: Option[List[String]]): Option[List[UUID]] =
    list.map(x => x.map(UUID.fromString))

}
