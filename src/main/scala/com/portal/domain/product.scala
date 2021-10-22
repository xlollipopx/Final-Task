package com.portal.domain

import com.portal.domain.category.Category
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

  final case class ProductItemWithCategories(product: ProductItem, categories: List[Category])

  sealed trait ProductStatus extends EnumEntry
  object ProductStatus extends Enum[ProductStatus] with CirceEnum[ProductStatus] {

    val values: IndexedSeq[ProductStatus] = findValues
    final case object InProcessing extends ProductStatus
    final case object Available extends ProductStatus
    final case object NotAvailable extends ProductStatus
  }

}
