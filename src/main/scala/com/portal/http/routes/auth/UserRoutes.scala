package com.portal.http.routes.auth

import com.portal.domain.auth.UserRole
import com.portal.domain.auth.UserRole.{Client, Courier}
import com.portal.service.AuthService
import com.portal.dto.user.{CourierWithPasswordDto, UserWithPasswordDto}
import cats.Monad
import cats.effect.Sync
import org.http4s.{HttpRoutes, Request, Response}
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import com.portal.http.routes.Marshaller.marshalResponse
import org.http4s.server.Router

final case class UserRoutes[F[_]: Monad: Sync](
  authService: AuthService[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    //localhost:9001/auth/client
    case req @ POST -> Root / "client" =>
      req.as[UserWithPasswordDto].flatMap { dto =>
        val res = for {
          token <- authService.newClient(dto, Client)
        } yield token
        marshalResponse(res)
      }

    //localhost:9001/auth/courier
    case req @ POST -> Root / "courier" =>
      req.as[CourierWithPasswordDto].flatMap { dto =>
        val res = for {
          token <- authService.newCourier(dto, Courier)
        } yield token
        marshalResponse(res)
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
