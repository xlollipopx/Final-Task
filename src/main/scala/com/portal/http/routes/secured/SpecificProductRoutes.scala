package com.portal.http.routes.secured

import cats.{Defer, Monad}
import cats.effect.Sync
import cats.implicits._
import com.portal.http.auth.users.ClientUser
import com.portal.service.SpecificProductsService
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, EntityEncoder, HttpRoutes, Response}

case class SpecificProductRoutes[F[_]: Monad: Sync](
  specificProductsService: SpecificProductsService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/products"

  private val httpRoutes: AuthedRoutes[ClientUser, F] = AuthedRoutes.of {

    case GET -> Root / "my-specific-groups" as user =>
      for {
        list <- specificProductsService.getGroupsByUserId(user.value.id.value)
        res  <- Ok(list)
      } yield res

    case GET -> Root / "my-specific-groups" / UUIDVar(id) as user =>
      for {
        list <- specificProductsService.getProductsByGroupId(id)
        res  <- Ok(list)
      } yield res
  }

  def routes(authMiddleware: AuthMiddleware[F, ClientUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
