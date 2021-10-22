package com.portal.domain

import java.util.UUID

object category {

  final case class CategoryId(value: UUID)

  final case class Category(id: CategoryId, name: String, description: String)

}
