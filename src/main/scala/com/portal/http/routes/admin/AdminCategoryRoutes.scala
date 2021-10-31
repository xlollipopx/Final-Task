package com.portal.http.routes.admin

import com.portal.dto.product.CategoryDtoModify
import com.portal.http.auth.users.ManagerUser
import com.portal.service.CategoryService
import cats.Monad
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes, Response}

case class AdminCategoryRoutes[F[_]: Monad: Sync](
  categoryService: CategoryService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/categories"

  private val httpRoutes: AuthedRoutes[ManagerUser, F] = AuthedRoutes.of {

    case req @ POST -> Root / "create" as manager =>
      req.req.as[CategoryDtoModify].flatMap { dto =>
        for {
          item <- categoryService.create(dto)
          res  <- Ok(item)
        } yield res
      }

    case req @ POST -> Root / "update" / UUIDVar(id) as manager =>
      req.req.as[CategoryDtoModify].flatMap { dto =>
        for {
          item <- categoryService.update(id, dto)
          res  <- Ok(item)
        } yield res
      }
  }

  def routes(authMiddleware: AuthMiddleware[F, ManagerUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
