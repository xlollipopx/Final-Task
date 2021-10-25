package com.portal.service.impl

import com.portal.auth.{Crypto, TokenExpiration, Tokens}
import com.portal.domain.auth.{Email, Password, UserId, UserName, UserNameInUse, UserRole}
import com.portal.http.auth.users.User
import com.portal.repository.UserRepository
import com.portal.service.AuthService
import com.portal.validation.{UserValidationError, UserValidator}
import cats._
import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import com.portal.dto.user.UserWithPasswordDto
import com.portal.effects.GenUUID
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.RedisCommands
import io.circe.generic.encoding.DerivedAsObjectEncoder.deriveEncoder
import io.circe.parser.decode
import io.circe.syntax._
import org.http4s.circe._
import pdi.jwt.JwtClaim

class AuthServiceImpl[F[_]: Sync: Monad](
  userRepository:  UserRepository[F],
  tokenExpiration: TokenExpiration,
  tokens:          Tokens[F],
  crypto:          Crypto,
  redis:           RedisCommands[F, String, String],
  validator:       UserValidator
) extends AuthService[F] {
  private val TokenExpiration = tokenExpiration.value

  override def newUser(userDto: UserWithPasswordDto): F[Either[UserValidationError, JwtToken]] = {
    val result: EitherT[F, UserValidationError, JwtToken] = for {
      x <- EitherT(validator.validate(userDto).pure[F])

      (username, mail, role, password) = x
      token <- EitherT.liftF(userRepository.findByName(username).flatMap {
        case Some(_) => UserNameInUse(username).raiseError[F, JwtToken]
        case None =>
          for {
            uuid <- GenUUID.forSync[F].make
            _    <- userRepository.create(User(UserId(uuid), username, mail, role), crypto.encrypt(password))
            t    <- tokens.create
            user  = User(UserId(uuid), username, mail, role).asJson.noSpaces
            _    <- redis.setEx(t.value, user, TokenExpiration)
            _    <- redis.setEx(username.value, t.value, TokenExpiration)
          } yield t
      })

    } yield token

    result.value
  }

  override def login(username: UserName, password: Password): F[JwtToken] = ???

  override def logout(token: JwtToken, username: UserName): F[Unit] = ???
}
