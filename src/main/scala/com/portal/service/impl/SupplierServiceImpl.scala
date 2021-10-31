package com.portal.service.impl

import cats.Monad
import cats.effect.Sync
import com.portal.dto.product
import cats.implicits._
import com.portal.domain.supplier.{Supplier, SupplierId}
import com.portal.dto.product.{SupplierDto, SupplierDtoModify}
import com.portal.effects.GenUUID
import com.portal.repository.SupplierRepository
import com.portal.service.SupplierService
import com.portal.util.ModelMapper.SupplierDomainToDto

import java.util.UUID

class SupplierServiceImpl[F[_]: Sync: Monad](supplierRepository: SupplierRepository[F]) extends SupplierService[F] {

  override def all(): F[List[SupplierDto]] =
    supplierRepository.all().flatMap(x => x.map(SupplierDomainToDto).pure[F])

  override def create(supplier: SupplierDtoModify): F[SupplierDto] =
    for {
      id <- GenUUID.forSync[F].make
      dto = SupplierDto(id.toString, supplier.name)
      _  <- supplierRepository.create(Supplier(SupplierId(id), supplier.name))
    } yield dto

  override def update(id: UUID, supplier: SupplierDtoModify): F[SupplierDto] =
    for {
      dto <- SupplierDto(id.toString, supplier.name).pure[F]
      _   <- supplierRepository.update(Supplier(SupplierId(id), supplier.name))
    } yield dto
}
