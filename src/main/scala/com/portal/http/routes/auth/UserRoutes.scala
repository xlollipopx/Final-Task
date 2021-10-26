package com.portal.http.routes.auth

import cats.MonadThrow
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl

class UserRoutes[F[_]: JsonDecoder: MonadThrow](
  //auth: Auth[F]
) extends Http4sDsl[F] {}
