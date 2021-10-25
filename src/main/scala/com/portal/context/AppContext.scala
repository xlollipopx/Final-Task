package com.portal.context

import cats.effect.{Async, Concurrent, ContextShift, Resource, Sync}
import com.portal.auth.{Crypto, JwtAccessTokenKeyConfig, PasswordSalt, TokenExpiration, Tokens}
import com.portal.conf.app._
import com.portal.conf.db.{migrator, transactor}
import com.portal.modules.HttpApi
import cats.syntax.all._
import com.portal.repository.{ProductItemRepository, UserRepository}
import com.portal.service.{AuthService, ProductItemService}
import com.portal.validation.{ProductItemValidator, UserValidator}
import org.http4s.HttpApp
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout._

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object AppContext {

  val jwtAccessTokenKeyConfig = JwtAccessTokenKeyConfig("portal4")
  val tokenExpiration         = TokenExpiration(30.minutes)
  val passwordSalt            = PasswordSalt("06!grsnxXG0d*Pj496p6fuA*o")

  def setUp[F[_]: ContextShift: Sync: Concurrent](conf: AppConf): Resource[F, HttpApp[F]] = for {

    tx               <- transactor[F](conf.db)
    migrator         <- Resource.eval(migrator[F](conf.db))
    _                <- Resource.eval(migrator.migrate())
    productRepository = ProductItemRepository.of[F](tx)
    userRepository    = UserRepository.of[F](tx)
    productService    = ProductItemService.of[F](productRepository, new ProductItemValidator)

    redis <- Redis[F].utf8(conf.redis.url)

    crypto = Crypto.make[F](passwordSalt)
    tokens = Tokens.make[F](jwtAccessTokenKeyConfig)
    authService = AuthService
      .of[F](userRepository, TokenExpiration(30.minutes), tokens, crypto, redis, new UserValidator)

    httpApp = HttpApi.make[F](productService, authService).httpApp

  } yield httpApp
}
