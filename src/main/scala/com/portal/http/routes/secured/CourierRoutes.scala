package com.portal.http.routes.secured

import cats.{Defer, Monad}
import cats.syntax.all._
import com.portal.http.auth.users.CourierUser
import dev.profunktor.auth.AuthHeaders
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

case class CourierRoutes[F[_]: Monad: Defer](
) extends Http4sDsl[F] {

  private val prefixPath = "/courier"
  private val httpRoutes: AuthedRoutes[CourierUser, F] = AuthedRoutes.of { case GET -> Root / "get" as courier =>
    Ok("courier " + courier.value.name)
  }

  def routes(authMiddleware: AuthMiddleware[F, CourierUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
