package com.portal.http.routes.admin

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import com.portal.dto.product.SupplierDtoModify
import com.portal.http.auth.users.ManagerUser
import com.portal.service.SupplierService
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}

case class AdminSupplierRoutes[F[_]: Monad: Sync](
  supplierService: SupplierService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/suppliers"

  private val httpRoutes: AuthedRoutes[ManagerUser, F] = AuthedRoutes.of {

    case req @ POST -> Root / "create" as manager =>
      req.req.as[SupplierDtoModify].flatMap { dto =>
        for {
          item <- supplierService.create(dto)
          res  <- Ok(item)
        } yield res
      }

    case req @ POST -> Root / "update" / UUIDVar(id) as manager =>
      req.req.as[SupplierDtoModify].flatMap { dto =>
        for {
          item <- supplierService.update(id, dto)
          res  <- Ok(item)
        } yield res
      }
  }

  def routes(authMiddleware: AuthMiddleware[F, ManagerUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
