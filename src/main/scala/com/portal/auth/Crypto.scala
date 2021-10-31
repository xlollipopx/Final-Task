package com.portal.auth

import cats.effect.Sync
import cats.syntax.all._
import eu.timepit.refined.auto._

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.spec.{IvParameterSpec, PBEKeySpec, SecretKeySpec}
import javax.crypto.{Cipher, SecretKeyFactory}
import com.portal.domain.auth._
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import javax.crypto.{Cipher, KeyGenerator}

@JsonCodec
case class PasswordSalt(value: String)

trait Crypto {
  def encrypt(value: Password):          EncryptedPassword
  def decrypt(value: EncryptedPassword): Password
}

object Crypto {
  def make[F[_]: Sync](passwordSalt: PasswordSalt): Crypto = {
    val secretKey = {
      val cryptoSecret = passwordSalt.value
      val keyGenerator = KeyGenerator.getInstance("AES")
      keyGenerator.init(128)
      keyGenerator.generateKey()
    }

    new Crypto {
      def encrypt(password: Password): EncryptedPassword = {
        val plainText = password.value
//        val plainTextBytes = plainText.getBytes
//        val cipher         = Cipher.getInstance("AES")
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
//        val encryptedBytes = cipher.doFinal(plainTextBytes)
//        val result         = Base64.getEncoder.encodeToString(encryptedBytes)
        EncryptedPassword(plainText)
      }

      def decrypt(password: EncryptedPassword): Password = {
        val encryptedText = password.value
//        val encryptedTextBytes = Base64.getDecoder.decode(encryptedText)
//        val cipher             = Cipher.getInstance("AES")
//        cipher.init(Cipher.DECRYPT_MODE, secretKey)
//        val decryptedBytes = cipher.doFinal(encryptedTextBytes)
        Password(new String(encryptedText))
      }
    }
  }

}
