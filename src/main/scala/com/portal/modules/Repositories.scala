package com.portal.modules

import cats.effect.Sync
import com.portal.repository.{
  CategoryRepository,
  OrderRepository,
  ProductItemRepository,
  SupplierRepository,
  UserRepository
}
import doobie.Transactor

sealed abstract class Repositories[F[_]] private (
  val orderRepository:       OrderRepository[F],
  val productItemRepository: ProductItemRepository[F],
  val userRepository:        UserRepository[F],
  val categoryRepository:    CategoryRepository[F],
  val supplierRepository:    SupplierRepository[F]
)

object Repositories {
  def make[F[_]: Sync](tx: Transactor[F]): Repositories[F] = {
    new Repositories[F](
      OrderRepository.of[F](tx),
      ProductItemRepository.of[F](tx),
      UserRepository.of[F](tx),
      CategoryRepository.of[F](tx),
      SupplierRepository.of[F](tx)
    ) {}
  }
}
