package com.portal.service.impl

import com.portal.auth.{Crypto, TokenExpiration, Tokens}
import com.portal.domain.auth.{
  Email,
  InvalidPassword,
  Password,
  UserId,
  UserName,
  UserNameInUse,
  UserNotFound,
  UserRole
}
import com.portal.http.auth.users.User
import com.portal.repository.UserRepository
import com.portal.service.AuthService
import com.portal.validation.{UserValidationError, UserValidator}
import cats._
import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import com.portal.dto.user.{LoginUserDto, UserWithPasswordDto}
import com.portal.effects.GenUUID
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.RedisCommands
import io.circe.generic.encoding.DerivedAsObjectEncoder.deriveEncoder
import io.circe.syntax._

class AuthServiceImpl[F[_]: Sync: Monad](
  userRepository:  UserRepository[F],
  tokenExpiration: TokenExpiration,
  tokens:          Tokens[F],
  crypto:          Crypto,
  redis:           RedisCommands[F, String, String],
  validator:       UserValidator
) extends AuthService[F] {
  private val TokenExpiration = tokenExpiration.value

  override def newUser(userDto: UserWithPasswordDto, role: UserRole): F[Either[UserValidationError, JwtToken]] = {
    val result: EitherT[F, UserValidationError, JwtToken] = for {
      x <- EitherT(validator.validate(userDto).pure[F])

      (username, mail, password) = x
      token <- EitherT.liftF(userRepository.findByName(username).flatMap {
        case Some(_) => UserNameInUse(username).raiseError[F, JwtToken]
        case None =>
          for {

            uuid <- GenUUID.forSync[F].make
            _    <- userRepository.createUser(User(UserId(uuid), username, mail, role), crypto.encrypt(password))
            t    <- tokens.create
            user  = User(UserId(uuid), username, mail, role).asJson.noSpaces
            _    <- redis.setEx(t.value, user, TokenExpiration)
            _    <- redis.setEx(username.value, t.value, TokenExpiration)
          } yield t
      })

    } yield token

    result.value
  }

  override def login(userDto: LoginUserDto): F[JwtToken] =
    userRepository.findByName(UserName(userDto.name)).flatMap {
      case None => UserNotFound(UserName(userDto.name)).raiseError[F, JwtToken]
      case Some(user) if user.password.value != crypto.encrypt(Password(userDto.password)).value => {
        print(crypto.encrypt(Password(userDto.password)).value + " " + user.password)
        InvalidPassword(user.name).raiseError[F, JwtToken]
      }
      case Some(user) =>
        redis.get(userDto.name).flatMap {
          case Some(t) => JwtToken(t).pure[F]
          case None =>
            tokens.create.flatTap { t =>
              redis.setEx(t.value, user.asJson.noSpaces, TokenExpiration) *>
                redis.setEx(userDto.name, t.value, TokenExpiration)
            }
        }
    }

  override def logout(token: JwtToken, username: UserName): F[Unit] =
    redis.del(token.value) *> redis.del(username.value).void

}
