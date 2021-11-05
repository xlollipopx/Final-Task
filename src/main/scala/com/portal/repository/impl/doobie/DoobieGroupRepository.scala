package com.portal.repository.impl.doobie

import cats.Functor
import cats.effect.Bracket
import com.portal.domain.group
import com.portal.domain.group.{UserGroup, UserGroupCreate}
import com.portal.repository.GroupRepository
import doobie.Transactor
import doobie.postgres.implicits._
import doobie.implicits._
import doobie.postgres.sqlstate
import meta.implicits._

import java.util.UUID

class DoobieGroupRepository[F[_]: Functor: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends GroupRepository[F] {

  val selectGroup = fr"SELECT * FROM user_groups"
  val createGroup = fr"INSERT INTO user_groups"
  val deleteGroup = fr"DELETE FROM user_groups"

  override def all(): F[List[UserGroup]] =
    selectGroup.query[UserGroup].to[List].transact(tx)

  override def create(group: UserGroup): F[Int] =
    (createGroup ++
      fr"VALUES(${group.id}, ${group.name})").update.run.transact(tx)

  override def delete(groupId: UUID): F[Int] =
    (deleteGroup ++ fr"WHERE uuid = ${groupId}").update.run.transact(tx)

  override def addUser(groupId: UUID, userId: UUID): F[Int] =
    fr"INSERT INTO groups_and_users VALUES(${groupId}, ${userId})".update.run
      .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION =>
        "Error"
      }
      .map(x => x.toOption.getOrElse(0))
      .transact(tx)

  override def addProduct(groupId: UUID, productId: UUID): F[Int] = {
    fr"INSERT INTO specific_products VALUES(${groupId}, ${productId})".update.run
      .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION =>
        "Error"
      }
      .map(x => x.toOption.getOrElse(0))
      .transact(tx)
  }

}
