package com.portal.service.impl

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import com.portal.domain.group.UserGroup
import com.portal.dto.product.ProductItemDto
import com.portal.repository.SpecificProductsRepository
import com.portal.service.SpecificProductsService
import com.portal.util.ModelMapper.ProductItemDomainToDto

import java.util.UUID

class SpecificProductsServiceImpl[F[_]: Sync: Monad](specificProductsRepository: SpecificProductsRepository[F])
  extends SpecificProductsService[F] {
  override def getGroupsByUserId(userId: UUID): F[List[UserGroup]] =
    specificProductsRepository.getGroupsByUserId(userId)

  override def getProductsByGroupId(groupId: UUID): F[List[ProductItemDto]] =
    specificProductsRepository.getProductsByGroupId(groupId).map(l => l.map(ProductItemDomainToDto))
}
