package com.portal.http.routes.admin

import com.portal.service.ProductItemService
import com.portal.domain.auth.UserRole.Client
import com.portal.dto.product.{ProductItemWithCategoriesDto, ProductStatusDto}
import cats.Monad
import cats.effect.Sync
import org.http4s.{AuthedRoutes, EntityEncoder, HttpRoutes, Response}
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import com.portal.http.auth.users.ManagerUser
import com.portal.validation.ProductValidationError
import com.portal.validation.ProductValidationError._
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

    case req @ POST -> Root / "update" / "status" / UUIDVar(id) as manager =>
      req.req.as[String].flatMap { dto =>
        for {
          status <- productService.setStatus(id, dto)
          res    <- Ok(status)
        } yield res
      }

    case req @ POST -> Root / "update" as manager =>
      req.req.as[ProductItemWithCategoriesDto].flatMap { dto =>
        for {
          status <- productService.update(dto)
          res    <- Ok(status)
        } yield res
      }
  }

  def marshalResponse[T](
    result: F[Either[ProductValidationError, T]]
  )(
    implicit E: EntityEncoder[F, T]
  ): F[Response[F]] =
    result
      .flatMap {
        case Left(error) => productErrorToHttpResponse(error)
        case Right(dto)  => Ok(dto)
      }
      .handleErrorWith { ex =>
        InternalServerError(ex.getMessage)
      }

  def productErrorToHttpResponse(error: ProductValidationError): F[Response[F]] =
    error match {
      case e @ InvalidStatus => NotFound(e.toString)

      case e => BadRequest(e.toString)
    }

  def routes(authMiddleware: AuthMiddleware[F, ManagerUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
