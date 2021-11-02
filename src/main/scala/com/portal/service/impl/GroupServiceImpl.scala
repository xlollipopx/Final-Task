package com.portal.service.impl

import cats.Monad
import cats.effect.Sync
import com.portal.domain.group
import com.portal.domain.group.{UserGroup, UserGroupCreate}
import com.portal.effects.GenUUID
import cats.implicits._
import com.portal.repository.GroupRepository
import com.portal.service.GroupService

import java.util.UUID

class GroupServiceImpl[F[_]: Sync: Monad](groupRepository: GroupRepository[F]) extends GroupService[F] {
  override def all(): F[List[UserGroup]] = groupRepository.all()

  override def create(group: UserGroupCreate): F[Boolean] = for {
    id  <- GenUUID.forSync[F].make
    res <- groupRepository.create(UserGroup(id, group.name))
  } yield (if (res == 1) true else false)

  override def delete(groupId: UUID): F[Boolean] =
    groupRepository.delete(groupId).map(x => if (x == 1) true else false)

  override def addUser(groupId: UUID, userId: UUID): F[Boolean] =
    groupRepository
      .addUser(groupId, userId)
      .map(x => if (x == 1) true else false)

  override def addProduct(groupId: UUID, productId: UUID): F[Boolean] =
    groupRepository
      .addProduct(groupId, productId)
      .map(x => if (x == 1) true else false)
}
