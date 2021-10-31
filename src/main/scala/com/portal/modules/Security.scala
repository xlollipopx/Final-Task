package com.portal.modules

import com.portal.http.auth.users.{
  ClientUser,
  CourierJwtAuth,
  CourierUser,
  ManagerJwtAuth,
  ManagerUser,
  User,
  UserJwtAuth
}
import cats.ApplicativeThrow
import cats.effect._
import cats.syntax.all._
import com.portal.auth.{Crypto, TokenExpiration, Tokens}
import com.portal.service.{AuthService, UsersAuth}
import dev.profunktor.auth.jwt._
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import eu.timepit.refined.auto._
import io.circe.parser.{decode => jsonDecode}
import pdi.jwt._
import com.portal.conf.app.{AppConf, DbConf}
import com.portal.domain.auth.UserRole.Manager
import com.portal.domain.auth.{Email, UserId, UserName}
import com.portal.repository.UserRepository
import com.portal.validation.UserValidator
import dev.profunktor.redis4cats.effect.Log.Stdout.instance

import scala.concurrent.duration.DurationInt

sealed abstract class Security[F[_]] private (
  val authService:    AuthService[F],
  val adminAuth:      UsersAuth[F, ManagerUser],
  val usersAuth:      UsersAuth[F, ClientUser],
  val courierAuth:    UsersAuth[F, CourierUser],
  val adminJwtAuth:   ManagerJwtAuth,
  val userJwtAuth:    UserJwtAuth,
  val courierJwtAuth: CourierJwtAuth
)

object Security {
  def make[F[_]: Sync: ContextShift: Concurrent](
    cfg:   AppConf,
    redis: RedisCommands[F, String, String],
    users: UserRepository[F]
  ): Security[F] = {

    val adminJwtAuth: ManagerJwtAuth =
      ManagerJwtAuth(
        JwtAuth
          .hmac(
            cfg.tokenConf.adminTokenKeyConfig.value,
            JwtAlgorithm.HS256
          )
      )

    val userJwtAuth: UserJwtAuth =
      UserJwtAuth(
        JwtAuth
          .hmac(
            cfg.tokenConf.jwtAccessClientTokenKeyConfig.value,
            JwtAlgorithm.HS256
          )
      )

    val courierJwtAuth: CourierJwtAuth =
      CourierJwtAuth(
        JwtAuth
          .hmac(
            cfg.tokenConf.jwtAccessCourierTokenKeyConfig.value,
            JwtAlgorithm.HS256
          )
      )

    val crypto = Crypto.make[F](cfg.tokenConf.passwordSalt)
    val tokens = Tokens.make[F](cfg.tokenConf)
    val auth = AuthService
      .of[F](users, TokenExpiration(cfg.tokenConf.expiration.minutes), tokens, crypto, redis, new UserValidator)
    val adminAuth   = UsersAuth.admin[F](users)
    val usersAuth   = UsersAuth.client[F](redis)
    val courierAuth = UsersAuth.courier[F](redis)
    new Security[F](auth, adminAuth, usersAuth, courierAuth, adminJwtAuth, userJwtAuth, courierJwtAuth) {}
  }
}
