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
import com.portal.http.auth.users.{ClientUser, CourierUser, ManagerUser}
import com.portal.http.routes.admin.{AdminCategoryRoutes, AdminProductRoutes, AdminSupplierRoutes}
import com.portal.http.routes.secured.{CourierRoutes, OrderRoutes}
import org.http4s.server.Router

object HttpApi {
  def make[F[_]: Async](services: Services[F], security: Security[F]): HttpApi[F] =
    new HttpApi[F](services, security) {}
}

sealed abstract class HttpApi[F[_]: Async](
  services: Services[F],
  security: Security[F]
) {

  private val managerMiddleware =
    JwtAuthMiddleware[F, ManagerUser](security.adminJwtAuth.value, security.adminAuth.findUser)
  private val usersMiddleware =
    JwtAuthMiddleware[F, ClientUser](security.userJwtAuth.value, security.usersAuth.findUser)

  private val courierMiddleware =
    JwtAuthMiddleware[F, CourierUser](security.courierJwtAuth.value, security.courierAuth.findUser)

  //auth
  val userRoutes   = UserRoutes[F](security.authService).routes
  val loginRoutes  = LoginRoutes[F](security.authService).routes
  val logoutRoutes = LogoutRoutes[F](security.authService).routes(usersMiddleware)

  //open
  val productRoutes  = ProductItemRoutes[F](services.productItemService).routes
  val categoryRoutes = CategoryRoutes[F](services.categoryService).routes
  val supplierRoutes = SupplierRoutes[F](services.supplierService).routes

  //admin
  val adminProductRoutes  = AdminProductRoutes[F](services.productItemService).routes(managerMiddleware)
  val adminCategoryRoutes = AdminCategoryRoutes[F](services.categoryService).routes(managerMiddleware)
  val adminSupplierRoutes = AdminSupplierRoutes[F](services.supplierService).routes(managerMiddleware)

  //secured
  val orderRoutes   = OrderRoutes[F](services.orderService).routes(usersMiddleware)
  val courierRoutes = CourierRoutes[F]().routes(courierMiddleware)

  private val openRoutes: HttpRoutes[F] =
    productRoutes <+> userRoutes <+> loginRoutes <+> categoryRoutes <+> supplierRoutes <+>
      courierRoutes <+> logoutRoutes <+> orderRoutes

  private val adminRoutes: HttpRoutes[F] =
    adminProductRoutes <+> adminCategoryRoutes <+> adminSupplierRoutes

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
