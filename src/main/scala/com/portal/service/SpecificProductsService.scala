package com.portal.service

import cats.effect.Sync
import com.portal.domain.group.UserGroup
import com.portal.domain.product.ProductItem
import com.portal.dto.product.ProductItemDto
import com.portal.repository.SpecificProductsRepository
import com.portal.service.impl.SpecificProductsServiceImpl

import java.util.UUID

trait SpecificProductsService[F[_]] {
  def getGroupsByUserId(userId:     UUID): F[List[UserGroup]]
  def getProductsByGroupId(groupId: UUID): F[List[ProductItemDto]]
}

object SpecificProductsService {
  def of[F[_]: Sync](
    specificProductsRepository: SpecificProductsRepository[F],
  ): SpecificProductsServiceImpl[F] =
    new SpecificProductsServiceImpl[F](specificProductsRepository)
}
