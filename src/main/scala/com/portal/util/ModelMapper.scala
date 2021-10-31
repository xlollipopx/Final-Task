package com.portal.util

import com.portal.domain.category.Category
import com.portal.domain.order.OrderWithProducts
import com.portal.domain.product.{
  ProductItem,
  ProductItemForOrder,
  ProductItemWithCategories,
  ProductItemWithCategoriesModify
}
import com.portal.domain.supplier.Supplier
import com.portal.dto.order.{OrderDto, OrderWithProductsDto, ProductItemForOrderDto}
import com.portal.dto.product.{
  CategoryDto,
  CategoryIdDto,
  ProductItemDto,
  ProductItemDtoWithId,
  ProductItemWithCategoriesDto,
  ProductItemWithCategoriesDtoModify,
  ProductItemWithIdCategoriesDto,
  SupplierDto
}

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

  def ProductItemDomainToDtoWithId(product: ProductItem): ProductItemDtoWithId = {
    ProductItemDtoWithId(
      product.id.value.toString,
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

  def OrderWithProductsDomainToDto(order: OrderWithProducts): OrderWithProductsDto = {
    val ord          = order.order
    val prods        = order.products
    val orderDto     = OrderDto(ord.id.value.toString, ord.status.toString, ord.username.value)
    val productsList = prods.map(ProductItemForOrderDomainToDto)
    OrderWithProductsDto(orderDto, productsList)
  }

  def ProductItemForOrderDomainToDto(product: ProductItemForOrder): ProductItemForOrderDto = {
    ProductItemForOrderDto(
      product.id.value.toString,
      product.name,
      product.cost.amount.toString,
      product.cost.currency.toString,
      product.quantity
    )
  }

  def ProductItemWithCategoriesDomainToDto(product: ProductItemWithCategories): ProductItemWithCategoriesDto = {
    ProductItemWithCategoriesDto(
      ProductItemDomainToDto(product.product),
      product.categories.map(CategoryDomainToDto)
    )
  }

  def ProductItemWithCategoriesDomainToDtoWithId(product: ProductItemWithCategories): ProductItemWithIdCategoriesDto = {
    ProductItemWithIdCategoriesDto(
      ProductItemDomainToDtoWithId(product.product),
      product.categories.map(CategoryDomainToDto)
    )
  }

  def ProductWithCategoriesModifyDomainToDto(item: ProductItemWithCategoriesModify) = {
    ProductItemWithCategoriesDtoModify(
      ProductItemDomainToDto(item.product),
      item.categories.map(x => CategoryIdDto(x.value.toString))
    )
  }

  def SupplierDomainToDto(supplier: Supplier) = {
    SupplierDto(supplier.id.value.toString, supplier.name)
  }

}
