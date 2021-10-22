package com.portal.domain

import scala.language.implicitConversions
import java.util.UUID

object supplier {

  final case class Supplier(id: SupplierId, name: String)

  final case class SupplierId(value: UUID)

}
