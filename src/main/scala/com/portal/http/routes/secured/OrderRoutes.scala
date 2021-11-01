package com.portal.http.routes.secured

import com.portal.http.auth.users.ClientUser
import com.portal.dto.product.QuantityDto
import com.portal.service.OrderService
import com.portal.domain.order.UserAddress
import com.portal.domain.product.ProductItemId
import dev.profunktor.auth.AuthHeaders
import cats.{Defer, Monad}
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, EntityEncoder, HttpRoutes, Response}

case class OrderRoutes[F[_]: Monad: Sync](
  orderService: OrderService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/order"

  private val httpRoutes: AuthedRoutes[ClientUser, F] = AuthedRoutes.of {
    case GET -> Root / "shopping-cart" as user =>
      for {
        l   <- orderService.getOrderByUserId(user.value.id)
        res <- Ok(l)
      } yield res

    case req @ POST -> Root / "add-product" / UUIDVar(id) as user =>
      req.req.as[QuantityDto].flatMap { dto =>
        for {
          resp <- orderService.addProductToOrder(ProductItemId(id), user.value.id, dto.quantity)
          res  <- Ok(resp)
        } yield res
      }

    case req @ PUT -> Root / "make-order" as user =>
      req.req.as[UserAddress].flatMap { dto =>
        for {
          resp <- orderService.makeOrder(user.value.id, dto.address)
          res  <- Ok(resp)
        } yield res
      }

  }

  def routes(authMiddleware: AuthMiddleware[F, ClientUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
