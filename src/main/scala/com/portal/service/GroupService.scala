package com.portal.service

import cats.effect.Sync
import com.portal.domain.group.{UserGroup, UserGroupCreate}
import com.portal.repository.GroupRepository
import com.portal.service.impl.GroupServiceImpl

import java.util.UUID

trait GroupService[F[_]] {
  def all(): F[List[UserGroup]]
  def create(group:       UserGroupCreate): F[Boolean]
  def delete(groupId:     UUID): F[Boolean]
  def addUser(groupId:    UUID, userId: UUID): F[Boolean]
  def addProduct(groupId: UUID, productId:  UUID): F[Boolean]
}

object GroupService {
  def of[F[_]: Sync](
    groupRepository: GroupRepository[F]
  ): GroupServiceImpl[F] =
    new GroupServiceImpl[F](groupRepository)
}
