package com.portal.modules

import cats.effect.Sync
import com.portal.service.{CategoryService, OrderService, ProductItemService, SupplierService}
import com.portal.validation.ProductItemValidator

sealed abstract class Services[F[_]] private (
  val orderService:       OrderService[F],
  val productItemService: ProductItemService[F],
  val categoryService:    CategoryService[F],
  val supplierService:    SupplierService[F]
)

object Services {
  def make[F[_]: Sync](repositories: Repositories[F]): Services[F] = {
    new Services[F](
      OrderService.of[F](repositories.orderRepository),
      ProductItemService.of[F](repositories.productItemRepository, new ProductItemValidator),
      CategoryService.of[F](repositories.categoryRepository),
      SupplierService.of[F](repositories.supplierRepository)
    ) {}
  }
}
