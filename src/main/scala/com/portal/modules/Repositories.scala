package com.portal.modules

import cats.effect.Sync
import com.portal.repository._
import doobie.Transactor

sealed abstract class Repositories[F[_]] private (
  val orderRepository:            OrderRepository[F],
  val productItemRepository:      ProductItemRepository[F],
  val userRepository:             UserRepository[F],
  val categoryRepository:         CategoryRepository[F],
  val supplierRepository:         SupplierRepository[F],
  val groupRepository:            GroupRepository[F],
  val specificProductsRepository: SpecificProductsRepository[F],
  val subscriptionRepository:     SubscriptionRepository[F]
)

object Repositories {
  def make[F[_]: Sync](tx: Transactor[F]): Repositories[F] = {
    new Repositories[F](
      OrderRepository.of[F](tx),
      ProductItemRepository.of[F](tx),
      UserRepository.of[F](tx),
      CategoryRepository.of[F](tx),
      SupplierRepository.of[F](tx),
      GroupRepository.of[F](tx),
      SpecificProductsRepository.of[F](tx),
      SubscriptionRepository.of[F](tx)
    ) {}
  }
}
