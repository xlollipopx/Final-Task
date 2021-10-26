package com.portal.dto

import com.portal.domain.auth.PhoneNumber

object user {
  final case class UserDto(name: String, mail: String, role: String)
  final case class UserWithPasswordDto(name: String, mail: String, password: String)
  final case class LoginUserDto(name: String, password: String)
}
