package com.portal.repository.impl.doobie.meta
import com.portal.domain.auth.{Email, EncryptedPassword, UserId, UserName, UserRole}
import com.portal.domain.category.CategoryId
import com.portal.domain.money.Money
import com.portal.domain.product.{ProductStatus, _}
import com.portal.domain.supplier.{Supplier, SupplierId}
import doobie.{Meta, Read, Write}
import doobie.postgres.implicits.UuidType

import java.time.LocalDate
import java.util.{Currency, UUID}

object implicits {
//Product
  implicit val productStatusMeta: Meta[ProductStatus] =
    Meta[String]
      .timap(s => ProductStatus.withNameInsensitive(snakeToCamel(s.toLowerCase)))(g => normalizedSnakeCase(g.toString))

  implicit val dateMeta: Meta[LocalDate] =
    Meta[String]
      .timap(s => LocalDate.parse(s))(g => g.toString)

  implicit val moneyMeta: Meta[Money] =
    Meta[String]
      .timap(s => toMoney(s))(g => g.amount.toString() + " " + g.currency.toString)

  def toMoney(s: String): Money = {
    val list = s.split("\\s").toList
    Money(BigDecimal(list.head), Currency.getInstance(list.last))
  }

  implicit val pointRead: Read[Supplier] =
    Read[(UUID, String)].map { case (x, y) => Supplier(SupplierId(x), y) }

  implicit val pointWrite: Write[Supplier] =
    Write[(UUID, String)].contramap(p => (p.id.value, p.name))

  implicit val categoryIdMeta: Meta[CategoryId] =
    Meta[String]
      .timap(s => CategoryId(UUID.fromString(s)))(g => g.value.toString)
  //////User

  implicit val userIdMeta: Meta[UserId] =
    Meta[UUID]
      .timap(s => UserId(s))(g => g.value)

  implicit val userNameMeta: Meta[UserName] =
    Meta[String]
      .timap(s => UserName(s))(g => g.value)

  implicit val encryptedPasswordMeta: Meta[EncryptedPassword] =
    Meta[String]
      .timap(s => EncryptedPassword(s))(g => g.value)

  implicit val emailMeta: Meta[Email] =
    Meta[String]
      .timap(s => Email(s))(g => g.value)

  implicit val userRoleMeta: Meta[UserRole] =
    Meta[String]
      .timap(s => UserRole.withNameInsensitive(snakeToCamel(s.toLowerCase)))(g => normalizedSnakeCase(g.toString))

  def snakeToCamel(name: String): String =
    "_([a-z\\d])".r
      .replaceAllIn(name, _.group(1).toUpperCase())

  def camelToSnake(name: String): String =
    "[A-Z\\d]".r
      .replaceAllIn(name, m => "_" + m.group(0).toLowerCase())

  def normalizedSnakeCase(str: String): String = {
    val firstChar      = str.charAt(0).toLower
    val remainingChars = str.substring(1)
    val pureCamelCase  = s"$firstChar$remainingChars"

    camelToSnake(pureCamelCase).toUpperCase
  }

}
