package com.portal.context

import cats.effect.{Concurrent, ContextShift, Resource, Sync, Timer}
import com.portal.conf.app._
import com.portal.conf.db.{migrator, transactor}
import com.portal.modules.{HttpApi, Repositories, Security, Services}
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout._
import org.http4s.HttpApp
import cats.implicits._
import com.emarsys.scheduler.Schedule
import com.emarsys.scheduler.syntax._
import com.portal.effects.EmailNotificationService
import com.portal.repository.SubscriptionRepository
import io.circe.config.parser

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

object AppContext {

  def setUp[F[_]: ContextShift: Sync: Concurrent: Timer](conf: AppConf): Resource[F, HttpApp[F]] = for {

    tx       <- transactor[F](conf.db)
    migrator <- Resource.eval(migrator[F](conf.db))
    _        <- Resource.eval(migrator.migrate())

    repositories = Repositories.make[F](tx)
    services     = Services.make[F](repositories)

    redis <- Redis[F].utf8(conf.redis.url)

    security = Security.make[F](conf, redis, repositories.userRepository)

    httpApp = HttpApi.make[F](services, security).httpApp

  } yield httpApp

  def setUpScheduler[F[_]: ContextShift: Sync: Concurrent: Timer](): Resource[F, Unit] = for {
    conf <- Resource.eval(parser.decodePathF[F, AppConf]("app"))

    tx                    <- transactor[F](conf.db)
    subscriptionRepository = SubscriptionRepository.of[F](tx)

    _ <- Resource.eval(for {
      users <- subscriptionRepository.getSuppliersWithUsers(LocalDate.now())

      res <- {
        val emailVerification = new EmailNotificationService()
        users.map(s => s.mails.map(m => emailVerification.send(m.value, s.supplier.name)))
      }.pure[F]

    } yield res) runOn Schedule.spaced(5.seconds)

  } yield ()

}
