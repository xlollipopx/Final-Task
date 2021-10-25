package com.portal.modules

import cats.effect.Async
import com.portal.http.routes._
import com.portal.http.routes.auth._

import com.portal.service.{AuthService, ProductItemService}
import org.http4s.HttpRoutes
import org.http4s._
import dev.profunktor.auth.JwtAuthMiddleware
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, CORS}
import cats.syntax.semigroupk._

object HttpApi {
  def make[F[_]: Async](productItemService: ProductItemService[F], authService: AuthService[F]): HttpApi[F] =
    new HttpApi[F](productItemService, authService) {}
}

sealed abstract class HttpApi[F[_]: Async](
  productItemService: ProductItemService[F],
  authService:        AuthService[F]
) {

  val employeeRoutes = ProductItemRoutes[F](productItemService).routes
  val userRoutes     = UserRoutes[F](authService).routes

  private val openRoutes: HttpRoutes[F] =
    employeeRoutes <+> userRoutes

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http)
    }
  }

  val httpApp: HttpApp[F] = middleware(openRoutes).orNotFound

}
