package com.portal.domain

import com.portal.domain.category.{Category, CategoryId}
import com.portal.domain.money.Money
import com.portal.domain.supplier.{Supplier, SupplierId}
import io.circe.{Decoder, Encoder}
import enumeratum.{CirceEnum, Enum, EnumEntry}

import scala.language.implicitConversions
import java.time.LocalDate
import java.util.UUID

object product {

  final case class ProductItemId(value: UUID)

  final case class ProductItem(
    id:              ProductItemId,
    name:            String,
    description:     String,
    cost:            Money,
    publicationDate: LocalDate,
    status:          ProductStatus,
    supplier:        Supplier
  )

  final case class ProductItemSearch(
    name:            Option[String],
    description:     Option[String],
    cost:            Option[CostInterval],
    publicationDate: Option[DateInterval],
    status:          Option[ProductStatus],
    supplierId:      Option[UUID],
    categoriesId:    Option[List[UUID]]
  )
  final case class CostInterval(min: Int, max: Int)
  final case class DateInterval(min: LocalDate, max: LocalDate)

  final case class ProductItemWithCategories(product: ProductItem, categories: List[Category])

  final case class ProductItemWithCategoriesModify(product: ProductItem, categories: List[CategoryId])

  final case class ProductItemForOrder(id: ProductItemId, name: String, cost: Money, quantity: Int)

  sealed trait ProductStatus extends EnumEntry
  object ProductStatus extends Enum[ProductStatus] with CirceEnum[ProductStatus] {

    val values: IndexedSeq[ProductStatus] = findValues
    final case object InProcessing extends ProductStatus
    final case object Available extends ProductStatus
    final case object NotAvailable extends ProductStatus
  }

}
