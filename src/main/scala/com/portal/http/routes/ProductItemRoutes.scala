package com.portal.http.routes

import com.portal.service.ProductItemService
import com.portal.dto.product.ProductItemWithCategoriesDto

import cats.Monad
import cats.effect.Sync
import org.http4s.HttpRoutes
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import org.http4s.server.Router

final case class ProductItemRoutes[F[_]: Monad: Sync](productItemService: ProductItemService[F]) extends Http4sDsl[F] {
  private val prefixPath = "/products"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "all" =>
      for {
        list <- productItemService.all
        res  <- Ok(list)
      } yield res

    case GET -> Root / "find" / UUIDVar(id) =>
      for {
        product <- productItemService.findById(id)
        res     <- Ok(product)
      } yield res

    case req @ POST -> Root / "create" =>
      req.as[ProductItemWithCategoriesDto].flatMap { dto =>
        for {
          product <- productItemService.create(dto)
          res     <- Ok(product)
        } yield res
      }

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
