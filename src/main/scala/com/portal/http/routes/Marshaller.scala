package com.portal.http.routes

import com.portal.validation.ValidationError

import cats.implicits._
import cats.effect.Sync
import org.http4s.{EntityEncoder, HttpRoutes, Response}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl

object Marshaller {

  def marshalResponse[F[_]: Sync, E <: ValidationError, T](
    result: F[Either[E, T]]
  )(
    implicit E: EntityEncoder[F, T]
  ): F[Response[F]] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    result
      .flatMap {
        case Left(error) => BadRequest(error.toString)
        case Right(dto)  => Ok(dto)
      }
      .handleErrorWith { ex =>
        InternalServerError(ex.getMessage)
      }
  }
}
