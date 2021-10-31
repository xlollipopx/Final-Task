package com.portal.http.routes

import com.portal.service.CategoryService
import cats.Monad
import cats.effect.Sync
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}

import org.http4s.server.Router

final case class CategoryRoutes[F[_]: Monad: Sync](categoryService: CategoryService[F]) extends Http4sDsl[F] {
  private val prefixPath = "/products"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "categories" / "all" =>
    for {
      list <- categoryService.all()
      res  <- Ok(list)
    } yield res
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
