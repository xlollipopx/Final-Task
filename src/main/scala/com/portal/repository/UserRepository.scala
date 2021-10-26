package com.portal.repository

import cats.effect.Sync
import com.portal.domain.auth.{EncryptedPassword, PhoneNumber, UserId, UserName}
import com.portal.domain.product.ProductItemWithCategories
import com.portal.http.auth.users.{User, UserWithPassword}
import com.portal.repository.impl.doobie.DoobieUserRepository
import doobie.Transactor

import java.util.UUID

trait UserRepository[F[_]] {
  def all(): F[List[User]]
  def createUser(user:    User, password: EncryptedPassword): F[Int]
  def createCourier(user: User, phoneNumber: PhoneNumber, password: EncryptedPassword): F[Int]
  def findByName(name:    UserName): F[Option[UserWithPassword]]
}

object UserRepository {
  def of[F[_]: Sync](tx: Transactor[F]): DoobieUserRepository[F] =
    new DoobieUserRepository[F](tx)
}
