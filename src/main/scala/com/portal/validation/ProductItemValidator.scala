package com.portal.validation

import com.portal.domain.category.{Category, CategoryId}
import com.portal.domain.money.Money
import com.portal.domain.product.ProductStatus
import com.portal.domain.product.ProductStatus.{Available, InProcessing, NotAvailable}
import com.portal.domain.supplier.{Supplier, SupplierId}
import com.portal.dto.product.{ProductItemWithCategoriesDto, ProductItemWithCategoriesDtoModify}
import com.portal.validation.ProductValidationError.{InvalidDateFormat, InvalidId, InvalidMoneyFormat, InvalidStatus}

import java.time.LocalDate
import java.util.{Currency, UUID}
import scala.util.control.NoStackTrace
import scala.util.{Failure, Success, Try}

sealed trait ProductValidationError extends NoStackTrace with ValidationError

object ProductValidationError {

  final case object InvalidDateFormat extends ProductValidationError {
    override def toString: String = "Wrong date format!"
  }

  final case object InvalidMoneyFormat extends ProductValidationError {
    override def toString: String = "Wrong money format!"
  }

  final case object InvalidStatus extends ProductValidationError {
    override def toString: String = "Wrong status!"
  }

  final case object InvalidId extends ProductValidationError {
    override def toString: String = "Wrong id!"
  }

}

class ProductItemValidator {

  def validateWithCategories(item: ProductItemWithCategoriesDto) = for {
    date      <- validateDate(item.product.publicationDate)
    cost      <- validateMoney(item.product.cost, item.product.currency)
    status    <- validateStatus(item.product.status)
    supplier  <- validateSupplier(item.product.supplierId, item.product.supplierName)
    categories = item.categories.map(x => Category(CategoryId(UUID.fromString(x.id)), x.name, x.description))
  } yield (item.product.name, item.product.description, cost, date, status, supplier, categories)

  def validateProductModify(item: ProductItemWithCategoriesDtoModify) = for {
    date      <- validateDate(item.product.publicationDate)
    cost      <- validateMoney(item.product.cost, item.product.currency)
    status    <- validateStatus(item.product.status)
    supplier  <- validateSupplier(item.product.supplierId, item.product.supplierName)
    categories = item.categories.map(x => (CategoryId(UUID.fromString(x.id))))
  } yield (item.product.name, item.product.description, cost, date, status, supplier, categories)

  def validateDate(birthday: String): Either[ProductValidationError, LocalDate] = {
    Either.cond(
      Try(LocalDate.parse(birthday)).isSuccess,
      LocalDate.parse(birthday),
      InvalidDateFormat
    )
  }

  def validateUUID(uuid: String): Either[ProductValidationError, UUID] = {
    Either.cond(
      UUID.fromString(uuid).isInstanceOf[UUID],
      UUID.fromString(uuid),
      InvalidId
    )
  }

  def validateMoney(cost: String, currency: String): Either[ProductValidationError, Money] = {
    for {
      amount <- Either.cond(
        Try(BigDecimal(cost)).isSuccess,
        BigDecimal(cost),
        InvalidMoneyFormat
      )
      currency <- Either.cond(
        Currency.getInstance(currency).isInstanceOf[Currency],
        Currency.getInstance(currency),
        InvalidMoneyFormat
      )
    } yield Money(amount, currency)
  }

  def validateStatus(status: String): Either[ProductValidationError, ProductStatus] = {
    status match {
      case "Available"     => Right(Available)
      case "Not available" => Right(NotAvailable)
      case "In processing" => Right(InProcessing)
      case _               => Left(InvalidStatus)
    }
  }

  def validateSupplier(id: String, name: String): Either[ProductValidationError, Supplier] = {
    Try(UUID.fromString(id)) match {
      case Success(x) => Right(Supplier(SupplierId(x), name))
      case Failure(_) => Left(InvalidId)
    }
  }
}
