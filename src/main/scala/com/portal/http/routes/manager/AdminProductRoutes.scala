package com.portal.http.routes.manager

import com.portal.dto.product.{ProductItemWithCategoriesDto, ProductItemWithCategoriesDtoModify}
import com.portal.http.auth.users.ManagerUser
import com.portal.service.ProductItemService
import cats.Monad
import cats.effect.Sync
import cats.implicits._
import com.portal.http.routes.Marshaller.marshalResponse
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
        val res = for {
          product <- productService.create(dto)
        } yield product
        marshalResponse(res)
      }

    case req @ POST -> Root / "update" / "status" / UUIDVar(id) as manager =>
      req.req.as[String].flatMap { dto =>
        val res = for {
          status <- productService.setStatus(id, dto)
        } yield status
        marshalResponse(res)
      }

    case req @ POST -> Root / "update" / UUIDVar(id) as manager =>
      req.req.as[ProductItemWithCategoriesDtoModify].flatMap { dto =>
        val res = for {
          product <- productService.update(id, dto)
        } yield product
        marshalResponse(res)
      }

    case POST -> Root / "delete" / UUIDVar(id) as manager =>
      for {
        status <- productService.delete(id)
        res    <- Ok(status)
      } yield res

  }

  def routes(authMiddleware: AuthMiddleware[F, ManagerUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
