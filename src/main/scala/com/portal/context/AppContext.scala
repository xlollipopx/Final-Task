package com.portal.context

import cats.effect.{Async, ContextShift, Resource}
import com.portal.conf.app.AppConf
import com.portal.conf.db.{migrator, transactor}
import com.portal.modules.HttpApi
import com.portal.repository.ProductItemRepository
import com.portal.service.ProductItemService
import com.portal.validation.ProductItemValidator
import org.http4s.HttpApp

object AppContext {
  def setUp[F[_]: ContextShift: Async](conf: AppConf): Resource[F, HttpApp[F]] = for {
    tx <- transactor[F](conf.db)

    migrator         <- Resource.eval(migrator[F](conf.db))
    _                <- Resource.eval(migrator.migrate())
    productRepository = ProductItemRepository.of[F](tx)
    productService    = ProductItemService.of[F](productRepository, new ProductItemValidator)
    httpApp           = HttpApi.make[F](productService).httpApp
  } yield httpApp
}
