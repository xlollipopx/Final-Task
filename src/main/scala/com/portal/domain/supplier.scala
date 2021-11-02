package com.portal.domain

import com.portal.domain.auth.Email

import scala.language.implicitConversions
import java.util.UUID

object supplier {

  final case class Supplier(id: SupplierId, name: String)

  final case class SupplierId(value: UUID)

  final case class SupplierWithUsers(supplier: Supplier, mails: List[Email])
  final case class UserMails(users: List[SupplierWithUsers])

}
