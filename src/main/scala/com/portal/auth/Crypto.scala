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

case class PasswordSalt(secret: String)

trait Crypto {
  def encrypt(value: Password):          EncryptedPassword
  def decrypt(value: EncryptedPassword): Password
}

object Crypto {
  def make[F[_]: Sync](passwordSalt: PasswordSalt): Crypto = {

    val random  = new SecureRandom()
    val ivBytes = new Array[Byte](16)
    random.nextBytes(ivBytes)
    val iv       = new IvParameterSpec(ivBytes);
    val salt     = passwordSalt.secret.getBytes("UTF-8")
    val keySpec  = new PBEKeySpec("password".toCharArray(), salt, 65536, 256)
    val factory  = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val bytes    = factory.generateSecret(keySpec).getEncoded
    val sKeySpec = new SecretKeySpec(bytes, "AES")
    val eCipher  = EncryptCipher(Cipher.getInstance("AES/CBC/PKCS5Padding"))
    eCipher.value.init(Cipher.ENCRYPT_MODE, sKeySpec, iv)
    val dCipher = DecryptCipher(Cipher.getInstance("AES/CBC/PKCS5Padding"))
    dCipher.value.init(Cipher.DECRYPT_MODE, sKeySpec, iv)

    new Crypto {
      def encrypt(password: Password): EncryptedPassword = {
        val base64 = Base64.getEncoder()
        val bytes  = password.value.getBytes("UTF-8")
        val result = new String(base64.encode(eCipher.value.doFinal(bytes)), "UTF-8")
        EncryptedPassword(result)
      }

      def decrypt(password: EncryptedPassword): Password = {
        val base64 = Base64.getDecoder()
        val bytes  = base64.decode(password.value.getBytes("UTF-8"))
        val result = new String(dCipher.value.doFinal(bytes), "UTF-8")
        Password(result)
      }
    }
  }

}
