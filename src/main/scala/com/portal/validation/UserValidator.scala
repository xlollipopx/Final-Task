package com.portal.validation

import com.portal.domain.auth.UserRole.{Client, Courier, Manager}
import com.portal.domain.auth.{Email, Password, UserName, UserRole}
import com.portal.dto.user.{CourierWithPasswordDto, UserWithPasswordDto}
import com.portal.validation.UserValidationError.{InvalidEmailFormat, InvalidPasswordFormat, InvalidUserRole}
import eu.timepit.refined.refineV

sealed trait UserValidationError

object UserValidationError {

  final case object InvalidEmailFormat extends UserValidationError {
    override def toString: String = "Wrong email format!"
  }

  final case object InvalidPasswordFormat extends UserValidationError {
    override def toString: String = "Wrong password format!"
  }

  final case object InvalidUserRole extends UserValidationError {
    override def toString: String = "Impossible role!"
  }
}

class UserValidator {

  def validateClient(userDto: UserWithPasswordDto) = for {
    mail     <- validateMail(userDto.mail)
    password <- validatePassword(userDto.password)
  } yield (UserName(userDto.name), mail, password)

  def validateCourier(userDto: CourierWithPasswordDto) =
    validateClient(UserWithPasswordDto(userDto.name, userDto.mail, userDto.password))

  def validateMail(mail: String): Either[UserValidationError, Email] = {
    Either.cond(
      mail.matches("^(.+)@(.+)$"),
      Email(mail),
      InvalidEmailFormat
    )
  }

  def validatePassword(password: String): Either[UserValidationError, Password] = {
    Either.cond(
      password.length > 3,
      Password(password),
      InvalidPasswordFormat
    )
  }

//  def validateRole(role: String): Either[UserValidationError, UserRole] = {
//    role match {
//      case "Manager" => Right(Manager)
//      case "Client"  => Right(Client)
//      case "Courier" => Right(Courier)
//      case _         => Left(InvalidUserRole)
//    }
//  }

}
