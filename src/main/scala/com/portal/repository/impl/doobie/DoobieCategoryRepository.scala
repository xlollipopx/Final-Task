package com.portal.repository.impl.doobie

import cats.Functor
import cats.effect.Bracket
import doobie.{ConnectionIO, Fragment, Transactor}
import doobie.implicits._
import cats.implicits._
import doobie.postgres.implicits._

import meta.implicits._
import cats.effect._
import com.portal.domain.category
import com.portal.domain.category.Category
import com.portal.repository.CategoryRepository

class DoobieCategoryRepository[F[_]: Functor: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends CategoryRepository[F] {

  val selectCategory = fr"SELECT * FROM categories"
  val createCategory = fr"INSERT INTO categories"
  val updateCategory = fr"UPDATE categories"

  override def all(): F[List[Category]] =
    selectCategory.query[Category].to[List].transact(tx)

  override def create(category: Category): F[Int] =
    (createCategory ++
      fr"VALUES(${category.id}, ${category.name}," ++
      fr" ${category.description})").update.run.transact(tx)

  override def update(category: Category): F[Int] =
    (updateCategory ++ fr"SET name = ${category.name}, " ++
      fr"description = ${category.description} WHERE uuid = ${category.id}").update.run.transact(tx)
}
