package com.portal.http.routes.secured

import cats.{Defer, Monad}
import cats.syntax.all._
import com.portal.http.auth.users.ClientUser
import com.portal.service.AuthService
import dev.profunktor.auth.AuthHeaders
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

case class OrderRoutes[F[_]: Monad: Defer](
) extends Http4sDsl[F] {

  private val prefixPath = "/order"

  private val httpRoutes: AuthedRoutes[ClientUser, F] = AuthedRoutes.of { case GET -> Root / "get" as user =>
    Ok("svfdfjvdlkfv " + user.value.name)
  }

  def routes(authMiddleware: AuthMiddleware[F, ClientUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
