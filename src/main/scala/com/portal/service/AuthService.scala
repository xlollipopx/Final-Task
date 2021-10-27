package com.portal.service

import cats.effect.Sync
import com.portal.auth.{Crypto, TokenExpiration, Tokens}
import com.portal.domain.auth.{Password, UserName, UserRole}
import com.portal.dto.user.{CourierWithPasswordDto, LoginUserDto, UserWithPasswordDto}
import com.portal.repository.UserRepository
import com.portal.service.impl.AuthServiceImpl
import dev.profunktor.redis4cats.RedisCommands
import com.portal.validation.{UserValidationError, UserValidator}
import dev.profunktor.auth.jwt.JwtToken

trait AuthService[F[_]] {
  def newClient(userDto: UserWithPasswordDto, role: UserRole): F[Either[UserValidationError, JwtToken]]

  def newCourier(userDto: CourierWithPasswordDto, role: UserRole): F[Either[UserValidationError, JwtToken]]

  def login(user: LoginUserDto): F[JwtToken]

  def logout(token: JwtToken, username: UserName): F[Unit]
}

object AuthService {
  def of[F[_]: Sync](
    userRepository:  UserRepository[F],
    tokenExpiration: TokenExpiration,
    tokens:          Tokens[F],
    crypto:          Crypto,
    redis:           RedisCommands[F, String, String],
    validator:       UserValidator
  ): AuthServiceImpl[F] =
    new AuthServiceImpl[F](userRepository, tokenExpiration, tokens, crypto, redis, validator)
}
