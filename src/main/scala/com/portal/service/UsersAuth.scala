package com.portal.service

import cats._
import cats.syntax.all._
import com.portal.domain.auth.{UserName, UserRole}
import com.portal.domain.auth.UserRole.{Client, Courier, Manager}
import com.portal.http.auth.users.{ClientUser, CommonUser, CourierUser, ManagerUser, User}
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
        find(redis, token, Client).asInstanceOf[F[Option[ClientUser]]]
    }

  def courier[F[_]: Functor](
    redis: RedisCommands[F, String, String]
  ): UsersAuth[F, CourierUser] =
    new UsersAuth[F, CourierUser] {
      def findUser(token: JwtToken)(claim: JwtClaim): F[Option[CourierUser]] =
        find(redis, token, Courier).asInstanceOf[F[Option[CourierUser]]]
    }

  private def find[F[_]: Functor](redis: RedisCommands[F, String, String], token: JwtToken, role: UserRole) = {
    redis
      .get(token.value)
      .map {
        _.flatMap { u =>
          decode[User](u).toOption match {
            case Some(user) => Option(makeUser(role, user))
            case _          => None
          }
        }
      }
  }

  private def makeUser(role: UserRole, user: User): CommonUser = {
    role match {
      case Courier => CourierUser(user)
      case Client  => ClientUser(user)
      case Manager => ManagerUser(user)
    }
  }

}
