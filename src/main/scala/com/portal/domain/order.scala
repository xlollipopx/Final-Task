package com.portal.domain

import com.portal.domain.auth.UserName
import com.portal.domain.product.ProductItemForOrder
import enumeratum.{CirceEnum, Enum, EnumEntry}

import java.util.UUID

object order {

  final case class OrderId(value: UUID)
  final case class Order(id: OrderId, status: OrderStatus, username: UserName)
  final case class OrderWithProducts(order: Order, products: List[ProductItemForOrder])
  final case class UserAddress(address: String)

  sealed trait OrderStatus extends EnumEntry
  object OrderStatus extends Enum[OrderStatus] with CirceEnum[OrderStatus] {

    val values: IndexedSeq[OrderStatus] = findValues
    final case object NotComplete extends OrderStatus
    final case object Ordered extends OrderStatus
    final case object Assigned extends OrderStatus
    final case object Delivered extends OrderStatus
  }
}
