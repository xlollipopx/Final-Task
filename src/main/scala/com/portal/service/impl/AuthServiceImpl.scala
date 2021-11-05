package com.portal.service.impl

import com.portal.auth.{Crypto, TokenExpiration, Tokens}
import com.portal.domain.auth._
import com.portal.http.auth.users.User
import com.portal.repository.UserRepository
import com.portal.service.AuthService
import com.portal.validation.{UserValidationError, UserValidator}
import cats._
import cats.data.EitherT
import io.circe.parser.decode
import cats.effect.Sync
import cats.syntax.all._
import com.portal.domain.auth.UserRole.{Client, Courier}
import com.portal.dto.user.{CourierWithPasswordDto, LoginUserDto, UserWithPasswordDto}
import com.portal.effects.GenUUID
import com.portal.validation.UserValidationError.UserNameInUse
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

  override def newClient(userDto: UserWithPasswordDto, role: UserRole): F[Either[UserValidationError, JwtToken]] = {
    val result: EitherT[F, UserValidationError, JwtToken] = for {
      x <- EitherT(validator.validateClient(userDto).pure[F])

      (username, mail, password) = x
      token <- EitherT.liftF(userRepository.findByName(username).flatMap {
        case Some(_) => UserNameInUse.raiseError[F, JwtToken]
        case None =>
          for {
            uuid <- GenUUID.forSync[F].make
            _    <- userRepository.createUser(User(UserId(uuid), username, mail, Client), crypto.encrypt(password))
            t    <- setToken(User(UserId(uuid), username, mail, Client))
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
            tokens.create(user.role).flatTap { t =>
              redis.setEx(t.value, user.asJson.noSpaces, TokenExpiration) *>
                redis.setEx(userDto.name, t.value, TokenExpiration)
            }
        }
    }

  override def logout(token: JwtToken, username: UserName): F[Unit] =
    redis.del(token.value) *> redis.del(username.value).void

  override def newCourier(userDto: CourierWithPasswordDto, role: UserRole): F[Either[UserValidationError, JwtToken]] = {
    val result: EitherT[F, UserValidationError, JwtToken] = for {
      x <- EitherT(validator.validateCourier(userDto).pure[F])

      (username, mail, password) = x
      token <- EitherT.liftF(userRepository.findByName(username).flatMap {
        case Some(_) => UserNameInUse.raiseError[F, JwtToken]
        case None =>
          for {
            uuid <- GenUUID.forSync[F].make
            _ <- userRepository.createCourier(
              User(UserId(uuid), username, mail, Courier),
              PhoneNumber(userDto.phoneNumber),
              crypto.encrypt(password)
            )
            t <- setToken(User(UserId(uuid), username, mail, Courier))
          } yield t
      })

    } yield token

    result.value
  }

  private def setToken(user: User): F[JwtToken] = for {
    t <- tokens.create(user.role)
    u  = user.asJson.noSpaces
    _ <- redis.setEx(t.value, u, TokenExpiration)
    _ <- redis.setEx(user.name.value, t.value, TokenExpiration)
  } yield t

}
