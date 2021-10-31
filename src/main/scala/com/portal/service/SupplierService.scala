package com.portal.service

import cats.effect.Sync
import com.portal.dto.product.{SupplierDto, SupplierDtoModify}
import com.portal.repository.SupplierRepository
import com.portal.service.impl.SupplierServiceImpl

import java.util.UUID

trait SupplierService[F[_]] {
  def all(): F[List[SupplierDto]]
  def create(supplier: SupplierDtoModify): F[SupplierDto]
  def update(id:       UUID, supplier: SupplierDtoModify): F[SupplierDto]
}

object SupplierService {
  def of[F[_]: Sync](
    supplierRepository: SupplierRepository[F]
  ): SupplierServiceImpl[F] =
    new SupplierServiceImpl[F](supplierRepository)
}
