package com.portal.repository

import cats.effect.Sync
import com.portal.domain.auth.UserId
import com.portal.domain.group.UserGroup
import com.portal.domain.product.ProductItemWithCategories
import doobie.Transactor

import java.util.UUID

trait SpecialProductsRepository[F[_]] {

  def getGroupsByUserId(userId:     UUID): F[List[UserGroup]]
  def getProductsByGroupId(groupId: UUID, userId: UUID): F[List[ProductItemWithCategories]]

}
object SpecialProductsRepository {
  def of[F[_]: Sync](tx: Transactor[F]): DoobieProductRepository[F] =
    new DoobieProductRepository[F](tx)
}
