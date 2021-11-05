package com.portal.service

import cats.effect.Sync
import com.portal.domain.auth.UserId
import com.portal.domain.order.OrderId
import com.portal.domain.product.ProductItemId
import com.portal.dto.order.OrderWithProductsDto
import com.portal.repository.OrderRepository
import com.portal.service.impl.OrderServiceImpl

trait OrderService[F[_]] {
  def all(): F[List[OrderWithProductsDto]]
  def addProductToOrder(productId:        ProductItemId, userId: UserId, quantity: Int): F[Boolean]
  def getOrderByUserId(userId:            UserId): F[Option[OrderWithProductsDto]]
  def makeOrder(userId:                   UserId, address:    String): F[Boolean]
  def assignToCourier(orderId:            OrderId, courierId: UserId): F[Boolean]
  def setDelivered(orderId:               OrderId, courierId: UserId): F[Boolean]
  def getCourierAssignedOrders(courierId: UserId): F[List[OrderWithProductsDto]]

}

object OrderService {
  def of[F[_]: Sync](
    orderRepository: OrderRepository[F]
  ): OrderServiceImpl[F] =
    new OrderServiceImpl[F](orderRepository)
}
