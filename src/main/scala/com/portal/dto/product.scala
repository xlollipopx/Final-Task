package com.portal.dto

import com.portal.domain.product.CostInterval

object product {

  final case class ProductItemDto(
    name:            String,
    description:     String,
    cost:            String,
    currency:        String,
    publicationDate: String,
    status:          String,
    supplierId:      String,
    supplierName:    String
  )

  final case class ProductItemSearchDto(
    name:            Option[String],
    description:     Option[String],
    cost:            Option[CostInterval],
    publicationDate: Option[DateIntervalDto],
    status:          Option[String],
    supplierId:      Option[String],
    categoriesId:    Option[List[String]]
  )
  final case class DateIntervalDto(min: String, max: String)

  final case class ProductItemDtoWithId(
    id:              String,
    name:            String,
    description:     String,
    cost:            String,
    currency:        String,
    publicationDate: String,
    status:          String,
    supplierId:      String,
    supplierName:    String
  )

  final case class ProductItemWithIdCategoriesDto(
    product:    ProductItemDtoWithId,
    categories: List[CategoryDto]
  )

  final case class ProductItemWithCategoriesDtoModify(
    product:    ProductItemDto,
    categories: List[CategoryIdDto]
  )

  final case class ProductItemWithCategoriesDto(
    product:    ProductItemDto,
    categories: List[CategoryDto]
  )

  final case class CategoryDto(id: String, name: String, description: String)
  final case class CategoryIdDto(id: String)
  final case class CategoryDtoModify(name: String, description: String)
  final case class QuantityDto(quantity: Int)

  final case class SupplierDto(id: String, name: String)
  final case class SupplierDtoModify(name: String)

  final case class ProductStatusDto(id: String, status: String)

}
