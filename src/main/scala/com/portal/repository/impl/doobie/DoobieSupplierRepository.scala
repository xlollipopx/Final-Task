package com.portal.repository.impl.doobie

import cats.Functor
import cats.effect.Bracket
import doobie.{ConnectionIO, Fragment, Transactor}
import doobie.implicits._
import com.portal.domain.supplier
import com.portal.domain.supplier.Supplier
import com.portal.repository.SupplierRepository
import doobie.Transactor
import meta.implicits._

class DoobieSupplierRepository[F[_]: Functor: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends SupplierRepository[F] {

  val selectSupplier = fr"SELECT * FROM suppliers"
  val createSupplier = fr"INSERT INTO suppliers"
  val updateSupplier = fr"UPDATE suppliers"

  override def all(): F[List[Supplier]] =
    selectSupplier.query[Supplier].to[List].transact(tx)

  override def create(supplier: Supplier): F[Int] =
    (createSupplier ++
      fr"VALUES(${supplier.id}, ${supplier.name})").update.run.transact(tx)

  override def update(supplier: Supplier): F[Int] =
    (updateSupplier ++ fr"SET name = ${supplier.name} WHERE uuid = ${supplier.id}").update.run.transact(tx)

}
