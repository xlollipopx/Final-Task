package com.portal.repository.impl.doobie

import cats.Functor
import cats.effect.Bracket
import com.portal.domain.group.UserGroup
import com.portal.domain.product.{ProductItem, ProductItemWithCategories}
import com.portal.domain.{group, product}
import com.portal.repository.SpecificProductsRepository
import doobie.{Fragment, Transactor}
import doobie.postgres.implicits._
import doobie.implicits._
import doobie.implicits.javatime._
import meta.implicits._
import cats.effect._

import java.util.UUID

class DoobieSpecificProductsRepository[F[_]: Functor: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends SpecificProductsRepository[F] {

  private val selectGroup: Fragment =
    fr"SELECT g.uuid, g.name FROM groups" ++
      fr"AS g" ++
      fr"INNER JOIN groups_and_users AS gu ON gu.user_group_id = g.uuid "

  override def getGroupsByUserId(userId: UUID): F[List[UserGroup]] =
    (selectGroup ++ fr"WHERE gu.user_id = ${userId}").query[UserGroup].to[List].transact(tx)

  override def getProductsByGroupId(groupId: UUID): F[List[ProductItem]] = {
    (fr"SELECT  p.uuid, p.name, p.description, p.cost, p.currency, " ++
      fr"p.publication_date, p.status, p.supplier_id, s.name " ++
      fr"FROM specific_products AS sp " ++
      fr"INNER JOIN products " ++
      fr"AS p ON p.uuid = sp.product_id " ++
      fr"INNER JOIN suppliers AS s ON p.supplier_id = s.uuid" ++
      fr"WHERE sp.user_group_id = ${groupId}").query[ProductItem].to[List].transact(tx)
  }
}
