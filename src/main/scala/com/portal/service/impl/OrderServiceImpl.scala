package com.portal.service.impl

import cats.Monad
import cats.effect.Sync
import cats.implicits._

import com.portal.domain.auth.UserId
import com.portal.domain.order.OrderId
import com.portal.domain.product.ProductItemId
import com.portal.dto.order.OrderWithProductsDto
import com.portal.repository.OrderRepository
import com.portal.service.OrderService
import com.portal.util.ModelMapper.OrderWithProductsDomainToDto

class OrderServiceImpl[F[_]: Sync: Monad](orderRepository: OrderRepository[F]) extends OrderService[F] {
  override def all(): F[List[OrderWithProductsDto]] = for {
    l  <- orderRepository.all()
    res = l.map(OrderWithProductsDomainToDto)
  } yield res

  override def addProductToOrder(productId: ProductItemId, userId: UserId, quantity: Int): F[Boolean] =
    orderRepository.addProductToOrder(productId, userId, quantity)

  override def getOrderByToken(userId: UserId): F[Option[OrderWithProductsDto]] =
    orderRepository.getOrderByUserId(userId).flatMap(x => x.map(OrderWithProductsDomainToDto).pure[F])

  override def makeOrder(userId: UserId, address: String): F[Boolean] = {
    orderRepository.makeOrder(userId, address)
  }

  override def assignToCourier(orderId: OrderId, courierId: UserId): Unit = {}

  override def setDelivered(orderId: OrderId, courierId: UserId): Unit = {}
}
