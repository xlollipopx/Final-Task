package com.portal.dto

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

  final case class ProductItemWithCategoriesDto(
    product:    ProductItemDto,
    categories: List[CategoryDto]
  )

  final case class CategoryDto(id: String, name: String, description: String)

  final case class SupplierDto(id: String, name: String)

}
