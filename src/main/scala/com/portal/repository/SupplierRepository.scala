package com.portal.repository

import cats.effect.Sync
import com.portal.domain.supplier.Supplier
import com.portal.repository.impl.doobie.DoobieSupplierRepository
import doobie.Transactor

trait SupplierRepository[F[_]] {
  def all(): F[List[Supplier]]
  def create(supplier: Supplier): F[Int]
  def update(supplier: Supplier): F[Int]
}

object SupplierRepository {
  def of[F[_]: Sync](tx: Transactor[F]): DoobieSupplierRepository[F] = new DoobieSupplierRepository[F](tx)
}
