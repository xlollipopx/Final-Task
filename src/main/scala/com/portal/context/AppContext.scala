package com.portal.context

import cats.effect.{Async, ContextShift, Resource}
import com.portal.conf.app.AppConf
import com.portal.conf.db.{migrator, transactor}
import org.http4s.HttpApp
import org.http4s.implicits._

object AppContext {
  def setUp[F[_]: ContextShift: Async](conf: AppConf): Resource[F, Unit] = for {
    tx <- transactor[F](conf.db)

    migrator <- Resource.eval(migrator[F](conf.db))
    _        <- Resource.eval(migrator.migrate())

  } yield ()
}
