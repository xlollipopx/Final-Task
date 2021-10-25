package com.portal.repository.impl.doobie

import cats.Functor
import cats.effect.Bracket
import com.portal.domain.auth.{EncryptedPassword, UserId, UserName}
import com.portal.http.auth.users.{User, UserWithPassword}
import com.portal.repository.UserRepository
import doobie.{Fragment, Transactor}
import doobie.postgres.implicits._
import doobie.implicits._
import meta.implicits._

import java.util.UUID

class DoobieUserRepository[F[_]: Functor: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends UserRepository[F] {
  private val selectUser: Fragment = fr"SELECT * FROM users"
  private val createUser: Fragment = fr"INSERT INTO users(" ++
    fr"uuid, name, email, role, password)"

  override def all(): F[List[User]] = selectUser.query[User].to[List].transact(tx)

  override def create(user: User, password: EncryptedPassword): F[Int] =
    (createUser ++ fr"VALUES(${user.id}, ${user.name}, ${user.mail}, ${user.role}, ${password})").update.run
      .transact(tx)

  override def findByName(name: UserName): F[Option[UserWithPassword]] = {
    (selectUser ++ fr"WHERE name = ${name}").query[UserWithPassword].option.transact(tx)
  }
}
