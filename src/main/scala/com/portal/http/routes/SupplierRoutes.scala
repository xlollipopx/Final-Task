package com.portal.http.routes

import cats.Monad
import cats.effect.Sync
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import com.portal.service.SupplierService
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.server.Router

final case class SupplierRoutes[F[_]: Monad: Sync](supplierService: SupplierService[F]) extends Http4sDsl[F] {
  private val prefixPath = "/products"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "suppliers" =>
    for {
      l   <- supplierService.all()
      res <- Ok(l)
    } yield res
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
