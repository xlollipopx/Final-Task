package com.portal.http.auth

import com.portal.domain.auth.{Email, EncryptedPassword, UserId, UserName, UserRole}
import dev.profunktor.auth.jwt.JwtSymmetricAuth

object users {

  case class AdminJwtAuth(value: JwtSymmetricAuth)
  case class UserJwtAuth(value: JwtSymmetricAuth)

  case class User(id: UserId, name: UserName, mail: Email, role: UserRole)

  case class UserWithPassword(id: UserId, name: UserName, mail: Email, role: UserRole, password: EncryptedPassword)

  case class CommonUser(value: User)

  case class ManagerUser(value: User)

  case class CourierUser(value: User)

}
