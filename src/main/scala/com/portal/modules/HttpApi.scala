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
import com.portal.http.auth.users.{ClientUser, ManagerUser}
import com.portal.http.routes.admin.AdminProductRoutes
import com.portal.http.routes.secured.OrderRoutes
import org.http4s.server.Router

object HttpApi {
  def make[F[_]: Async](productItemService: ProductItemService[F], security: Security[F]): HttpApi[F] =
    new HttpApi[F](productItemService, security) {}
}

sealed abstract class HttpApi[F[_]: Async](
  productItemService: ProductItemService[F],
  security:           Security[F]
) {

  private val managerMiddleware =
    JwtAuthMiddleware[F, ManagerUser](security.adminJwtAuth.value, security.adminAuth.findUser)
  private val usersMiddleware =
    JwtAuthMiddleware[F, ClientUser](security.userJwtAuth.value, security.usersAuth.findUser)

  //auth
  val userRoutes   = UserRoutes[F](security.authService).routes
  val loginRoutes  = LoginRoutes[F](security.authService).routes
  val logoutRoutes = LogoutRoutes[F](security.authService).routes(usersMiddleware)

  //open
  val productRoutes = ProductItemRoutes[F](productItemService).routes

  //admin
  val adminProductRoutes = AdminProductRoutes[F](productItemService).routes(managerMiddleware)

  //secured
  val orderRoutes = OrderRoutes[F]().routes(usersMiddleware)

  private val openRoutes: HttpRoutes[F] =
    productRoutes <+> userRoutes <+> loginRoutes <+> logoutRoutes <+> orderRoutes

  private val adminRoutes: HttpRoutes[F] =
    adminProductRoutes

  private val routes: HttpRoutes[F] = Router(
    ""       -> openRoutes,
    "/admin" -> adminRoutes
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http)
    }
  }

  val httpApp: HttpApp[F] = middleware(routes).orNotFound

}
