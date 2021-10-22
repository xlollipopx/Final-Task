package com.portal.modules

import cats.effect.Async
import com.portal.http.routes.ProductItemRoutes
import com.portal.service.ProductItemService
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.implicits._

import org.http4s.server.middleware.{AutoSlash, CORS}

object HttpApi {
  def make[F[_]: Async](productItemService: ProductItemService[F]): HttpApi[F] =
    new HttpApi[F](productItemService) {}
}

sealed abstract class HttpApi[F[_]: Async](
  productItemService: ProductItemService[F]
) {

  val employeeRoutes = ProductItemRoutes[F](productItemService).routes

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http)
    }
  }

  val httpApp: HttpApp[F] = middleware(employeeRoutes).orNotFound

}
