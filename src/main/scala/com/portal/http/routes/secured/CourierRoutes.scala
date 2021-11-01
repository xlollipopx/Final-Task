package com.portal.http.routes.secured

import cats.{Defer, Monad}
import cats.syntax.all._
import com.portal.domain.order.OrderId
import com.portal.http.auth.users.CourierUser
import com.portal.service.OrderService
import dev.profunktor.auth.AuthHeaders
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.http4s.server._

case class CourierRoutes[F[_]: Monad: Defer](orderService: OrderService[F]) extends Http4sDsl[F] {

  private val prefixPath = "/courier"
  private val httpRoutes: AuthedRoutes[CourierUser, F] = AuthedRoutes.of {

    case GET -> Root / "available-orders" as courier =>
      for {
        list <- orderService.all()
        res  <- Ok(list)
      } yield res

    case POST -> Root / "available-orders" / "take-order" / UUIDVar(id) as courier =>
      for {
        item <- orderService.assignToCourier(OrderId(id), courier.value.id)
        res  <- Ok(item)
      } yield res

    case GET -> Root / "my-orders" as courier =>
      for {
        item <- orderService.getCourierAssignedOrders(courier.value.id)
        res  <- Ok(item)
      } yield res

    case POST -> Root / "my-orders" / "set-delivered" / UUIDVar(id) as courier =>
      for {
        item <- orderService.setDelivered(OrderId(id), courier.value.id)
        res  <- Ok(item)
      } yield res

  }

  def routes(authMiddleware: AuthMiddleware[F, CourierUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
