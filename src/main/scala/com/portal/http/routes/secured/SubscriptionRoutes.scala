package com.portal.http.routes.secured

import com.portal.http.auth.users.ClientUser
import com.portal.service.SubscriptionService

import dev.profunktor.auth.AuthHeaders
import cats.{Defer, Monad}
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, EntityEncoder, HttpRoutes, Response}

case class SubscriptionRoutes[F[_]: Monad: Sync](
  subscriptionService: SubscriptionService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/products/all"

  private val httpRoutes: AuthedRoutes[ClientUser, F] = AuthedRoutes.of {
    case POST -> Root / "supplier" / "subscribe" / UUIDVar(id) as user =>
      for {
        l   <- subscriptionService.createSupplierSubscription(user.value.id.value, id)
        res <- Ok(l)
      } yield res

    case POST -> Root / "supplier" / "unsubscribe" / UUIDVar(id) as user =>
      for {
        l   <- subscriptionService.deleteSupplierSubscription(user.value.id.value, id)
        res <- Ok(l)
      } yield res

    case POST -> Root / "category" / "subscribe" / UUIDVar(id) as user =>
      for {
        l   <- subscriptionService.createCategorySubscription(user.value.id.value, id)
        res <- Ok(l)
      } yield res

    case POST -> Root / "category" / "unsubscribe" / UUIDVar(id) as user =>
      for {
        l   <- subscriptionService.deleteCategorySubscription(user.value.id.value, id)
        res <- Ok(l)
      } yield res

  }

  def routes(authMiddleware: AuthMiddleware[F, ClientUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
