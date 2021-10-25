package com.portal.domain

import enumeratum.{CirceEnum, Enum, EnumEntry}
import eu.timepit.refined.api.Refined

import java.util.UUID
import javax.crypto.Cipher
import scala.util.control.NoStackTrace
import eu.timepit.refined.auto._
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.types.string.NonEmptyString
import io.circe._

object auth {

  case class UserId(value: UUID)

  case class UserName(value: String)

  case class Password(value: String)

  //type Email = String Refined MatchesRegex["""(\w)+@([\w\.]+)"""]
  case class Email(value: String)

  case class EncryptedPassword(value: String)

  case class EncryptCipher(value: Cipher)

  case class DecryptCipher(value: Cipher)

  case class UserNameParam(value: NonEmptyString) {
    def toDomain: UserName = UserName(value.toLowerCase)
  }

  case class PasswordParam(value: NonEmptyString) {
    def toDomain: Password = Password(value)
  }

  sealed trait UserRole extends EnumEntry

  object UserRole extends Enum[UserRole] with CirceEnum[UserRole] {
    val values: IndexedSeq[UserRole] = findValues
    final case object Manager extends UserRole
    final case object Courier extends UserRole
    final case object Client extends UserRole
  }

  case class CreateUser(
    username: UserNameParam,
    password: PasswordParam
  )

  case class UserNotFound(username: UserName) extends NoStackTrace
  case class UserNameInUse(username: UserName) extends NoStackTrace
  case class InvalidPassword(username: UserName) extends NoStackTrace
  case object UnsupportedOperation extends NoStackTrace

  case object TokenNotFound extends NoStackTrace

  case class LoginUser(
    username: UserNameParam,
    password: PasswordParam
  )

  case class ClaimContent(uuid: UUID)

  object ClaimContent {
    implicit val jsonDecoder: Decoder[ClaimContent] =
      Decoder.forProduct1("uuid")(ClaimContent.apply)
  }

}
