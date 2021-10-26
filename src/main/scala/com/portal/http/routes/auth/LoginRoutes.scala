package com.portal.http.routes.auth

import com.portal.domain.auth.LoginUser
import com.portal.dto.user.LoginUserDto
import com.portal.service.AuthService
import cats.Monad
import cats.effect.Sync
import org.http4s.{HttpRoutes, Request, Response}
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import org.http4s.server.Router

final case class LoginRoutes[F[_]: Monad: Sync](
  authService: AuthService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    //localhost:9001/auth/login
    case req @ POST -> Root / "login" =>
      req.as[LoginUserDto].flatMap { dto =>
        for {
          token <- authService.login(dto)
          res   <- Ok(token)
        } yield res
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
