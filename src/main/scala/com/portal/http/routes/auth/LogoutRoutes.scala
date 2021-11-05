package com.portal.http.routes.auth

import cats.syntax.all._
import cats.{Defer, Monad}
import com.portal.http.auth.users.{ClientUser, CommonUser, CourierUser, ManagerUser}
import com.portal.service.AuthService
import dev.profunktor.auth.AuthHeaders
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final case class LogoutRoutes[F[_]: Monad: Defer](
  auth: AuthService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/auth"

  private val httpRoutes: AuthedRoutes[ClientUser, F] =
    AuthedRoutes.of { case ar @ POST -> Root / "logout" as user =>
      logout(ar, user)
    }

  private def logout[A](ar: AuthedRequest[F, A], user: CommonUser) = {
    AuthHeaders
      .getBearerToken(ar.req)
      .traverse_(auth.logout(_, user.value.name)) *> NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, ClientUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
