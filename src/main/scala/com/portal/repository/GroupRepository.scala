package com.portal.repository

import cats.effect.Sync
import com.portal.domain.group.{UserGroup, UserGroupCreate}
import com.portal.repository.impl.doobie.DoobieGroupRepository
import doobie.Transactor

import java.util.UUID

trait GroupRepository[F[_]] {
  def all(): F[List[UserGroup]]
  def create(group:       UserGroup):      F[Int]
  def delete(groupId:     UUID): F[Int]
  def addUser(groupId:    UUID, userId: UUID): F[Int]
  def addProduct(groupId: UUID, productId: UUID): F[Int]
}

object GroupRepository {
  def of[F[_]: Sync](tx: Transactor[F]): DoobieGroupRepository[F] =
    new DoobieGroupRepository[F](tx)
}
