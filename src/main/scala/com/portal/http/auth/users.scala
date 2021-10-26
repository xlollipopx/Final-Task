package com.portal.http.auth

import com.portal.domain.auth.{Email, EncryptedPassword, UserId, UserName, UserRole}
import dev.profunktor.auth.jwt.JwtSymmetricAuth

object users {

  case class ManagerJwtAuth(value: JwtSymmetricAuth)
  case class UserJwtAuth(value: JwtSymmetricAuth)
  case class CourierJwtAuth(value: JwtSymmetricAuth)

  case class User(id: UserId, name: UserName, mail: Email, role: UserRole)

  case class UserWithPassword(id: UserId, name: UserName, mail: Email, role: UserRole, password: EncryptedPassword)

  abstract class CommonUser {
    def value: User
  }

  case class ClientUser(value: User) extends CommonUser

  case class ManagerUser(value: User) extends CommonUser

  case class CourierUser(value: User) extends CommonUser

}
