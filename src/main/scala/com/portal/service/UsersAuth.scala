package com.portal.service

import cats._
import cats.syntax.all._
import com.portal.domain.auth.UserName
import com.portal.domain.auth.UserRole.Client
import com.portal.http.auth.users.{ClientUser, ManagerUser, User}
import com.portal.repository.UserRepository
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.RedisCommands
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.parser.decode
import io.circe.syntax._
import pdi.jwt.JwtClaim

trait UsersAuth[F[_], A] {
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}

object UsersAuth {
  def admin[F[_]: Applicative](
    users: UserRepository[F]
  ): UsersAuth[F, ManagerUser] =
    new UsersAuth[F, ManagerUser] {
      def findUser(token: JwtToken)(claim: JwtClaim): F[Option[ManagerUser]] =
        for {
          manager <- users.findByName(UserName("manager"))
          res = manager match {
            case Some(x) if x.password.value == token.value => Option(ManagerUser(User(x.id, x.name, x.mail, x.role)))
            case Some(_)                                    => None
            case None                                       => None
          }
        } yield res
    }

  def client[F[_]: Functor](
    redis: RedisCommands[F, String, String]
  ): UsersAuth[F, ClientUser] =
    new UsersAuth[F, ClientUser] {
      def findUser(token: JwtToken)(claim: JwtClaim): F[Option[ClientUser]] =
        redis
          .get(token.value)
          .map {
            _.flatMap { u =>
              decode[User](u).toOption match {
                case Some(user) if user.role == Client => Option(ClientUser(user))
                case _                                 => None
              }
            }
          }
    }

}
