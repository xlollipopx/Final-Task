package com.portal.repository

import cats.effect.Sync
import com.portal.domain.group.UserGroup
import com.portal.domain.product.ProductItem
import com.portal.dto.product.ProductItemDto
import com.portal.repository.impl.doobie.DoobieSpecificProductsRepository
import doobie.Transactor

import java.util.UUID

trait SpecificProductsRepository[F[_]] {

  def getGroupsByUserId(userId:     UUID): F[List[UserGroup]]
  def getProductsByGroupId(groupId: UUID): F[List[ProductItem]]

}

object SpecificProductsRepository {
  def of[F[_]: Sync](tx: Transactor[F]): DoobieSpecificProductsRepository[F] =
    new DoobieSpecificProductsRepository[F](tx)
}
