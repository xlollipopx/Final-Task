package com.portal.repository
import com.portal.domain.auth.{Password, UserName}
import dev.profunktor.auth.jwt.JwtToken

trait AuthRepository[F[_]] {
  def newUser(username: UserName, password: Password): F[JwtToken]

  def login(username: UserName, password: Password): F[JwtToken]

  def logout(token: JwtToken, username: UserName): F[Unit]
}
