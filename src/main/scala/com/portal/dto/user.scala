package com.portal.dto

object user {
  final case class UserDto(name: String, mail: String, role: String)
  final case class UserWithPasswordDto(name: String, mail: String, role: String, password: String)

}
