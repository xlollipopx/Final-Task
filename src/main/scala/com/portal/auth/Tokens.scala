package com.portal.auth

import cats.Monad
import cats.effect.Sync
import cats.syntax.all._
import com.portal.conf.app.TokenConfig
import com.portal.domain.auth.UserRole
import com.portal.domain.auth.UserRole.{Client, Courier, Manager}
import com.portal.effects.GenUUID
import dev.profunktor.auth.jwt._
import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.syntax._
import pdi.jwt._

import scala.concurrent.duration.FiniteDuration

trait Tokens[F[_]] {
  def create(role: UserRole): F[JwtToken]
}

@JsonCodec
case class JwtAccessClientTokenKeyConfig(value: String)
case class TokenExpiration(value: FiniteDuration)

object Tokens {
  def make[F[_]: Sync: Monad](
    config: TokenConfig,
  ): Tokens[F] =
    new Tokens[F] {
      def create(role: UserRole): F[JwtToken] =
        for {
          uuid <- GenUUID[F].make
          claim = JwtClaim(uuid.asJson.noSpaces)
          secretKey = role match {
            case Client  => JwtSecretKey(config.jwtAccessClientTokenKeyConfig.value)
            case Courier => JwtSecretKey(config.jwtAccessCourierTokenKeyConfig.value)
            case Manager => JwtSecretKey(config.adminTokenKeyConfig.value)
          }
          token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
        } yield token
    }

}
