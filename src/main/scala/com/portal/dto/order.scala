package com.portal.dto

object order {
  final case class OrderDto(
    id:       String,
    status:   String,
    username: String
  )

  final case class OrderWithProductsDto(order: OrderDto, products: List[ProductItemForOrderDto])

  final case class ProductItemForOrderDto(
    id:       String,
    name:     String,
    cost:     String,
    currency: String,
    quantity: Int
  )

}
