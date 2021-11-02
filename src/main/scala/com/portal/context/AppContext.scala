package com.portal.context

import cats.effect.{Async, Concurrent, ContextShift, Resource, Sync}
import com.portal.auth.{Crypto, JwtAccessClientTokenKeyConfig, PasswordSalt, TokenExpiration, Tokens}
import com.portal.conf.app._
import com.portal.conf.db.{migrator, transactor}
import com.portal.modules.{HttpApi, Repositories, Security, Services}
import cats.syntax.all._
import com.portal.Scheduler.SchedulerMail
import com.portal.http.auth.users
import com.portal.repository.{ProductItemRepository, UserRepository}
import com.portal.service.{AuthService, ProductItemService}
import com.portal.validation.{ProductItemValidator, UserValidator}
import org.http4s.HttpApp
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout._

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object AppContext {

  def setUp[F[_]: ContextShift: Sync: Concurrent](conf: AppConf): Resource[F, HttpApp[F]] = for {

    tx       <- transactor[F](conf.db)
    migrator <- Resource.eval(migrator[F](conf.db))
    _        <- Resource.eval(migrator.migrate())

    repositories = Repositories.make[F](tx)
    services     = Services.make[F](repositories)

    redis <- Redis[F].utf8(conf.redis.url)

    security = Security.make[F](conf, redis, repositories.userRepository)
    //_       <- Resource.eval(SchedulerMail.startSchedule(repositories.subscriptionRepository))

    httpApp = HttpApi.make[F](services, security).httpApp

  } yield httpApp
}
