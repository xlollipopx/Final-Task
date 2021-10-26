package com.portal.http.routes.admin

import com.portal.service.ProductItemService
import com.portal.domain.auth.UserRole.Client
import com.portal.dto.product.ProductItemWithCategoriesDto
import cats.Monad
import cats.effect.Sync
import org.http4s.{AuthedRoutes, HttpRoutes}
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import com.portal.http.auth.users.ManagerUser
import org.http4s.server.{AuthMiddleware, Router}

case class AdminProductRoutes[F[_]: Monad: Sync](
  productService: ProductItemService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/products"

  private val httpRoutes: AuthedRoutes[ManagerUser, F] = AuthedRoutes.of {
    //localhost:9001/admin/products/create ...
    case req @ POST -> Root / "create" as manager =>
      req.req.as[ProductItemWithCategoriesDto].flatMap { dto =>
        for {
          product <- productService.create(dto)
          res     <- Ok(product)
        } yield res
      }
  }

  def routes(authMiddleware: AuthMiddleware[F, ManagerUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
