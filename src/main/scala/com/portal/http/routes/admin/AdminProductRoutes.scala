package com.portal.http.routes.admin

import com.portal.dto.product.{ProductItemWithCategoriesDto, ProductItemWithCategoriesDtoModify}
import com.portal.http.auth.users.ManagerUser
import com.portal.service.ProductItemService
import cats.Monad
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes, Response}

case class AdminProductRoutes[F[_]: Monad: Sync](
  productService: ProductItemService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/products"

  private val httpRoutes: AuthedRoutes[ManagerUser, F] = AuthedRoutes.of {
    //localhost:9001/admin/products/create ...
    case req @ POST -> Root / "create" as manager =>
      req.req.as[ProductItemWithCategoriesDtoModify].flatMap { dto =>
        for {
          product <- productService.create(dto)
          res     <- Ok(product)
        } yield res
      }

    case req @ POST -> Root / "update" / "status" / UUIDVar(id) as manager =>
      req.req.as[String].flatMap { dto =>
        for {
          status <- productService.setStatus(id, dto)
          res    <- Ok(status)
        } yield res
      }

    case req @ POST -> Root / "update" / UUIDVar(id) as manager =>
      req.req.as[ProductItemWithCategoriesDtoModify].flatMap { dto =>
        for {
          status <- productService.update(id, dto)
          res    <- Ok(status)
        } yield res
      }
  }

  def routes(authMiddleware: AuthMiddleware[F, ManagerUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
