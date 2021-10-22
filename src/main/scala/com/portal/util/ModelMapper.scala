package com.portal.util

import com.portal.domain.category.Category
import com.portal.domain.product.{ProductItem, ProductItemWithCategories}
import com.portal.dto.product.{CategoryDto, ProductItemDto, ProductItemWithCategoriesDto}

object ModelMapper {
  def ProductItemDomainToDto(product: ProductItem): ProductItemDto = {
    ProductItemDto(
      product.name,
      product.description,
      product.cost.amount.toString(),
      product.cost.currency.toString,
      product.publicationDate.toString,
      product.status.toString,
      product.supplier.id.value.toString,
      product.supplier.name
    )
  }

  def CategoryDomainToDto(category: Category): CategoryDto = {
    CategoryDto(category.id.value.toString, category.name, category.description)
  }

  def ProductItemWithCategoriesDomainToDto(product: ProductItemWithCategories): ProductItemWithCategoriesDto = {
    ProductItemWithCategoriesDto(
      ProductItemDomainToDto(product.product),
      product.categories.map(CategoryDomainToDto)
    )
  }

}
