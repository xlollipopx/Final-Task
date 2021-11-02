package com.portal.domain

import java.util.UUID

object group {

  final case class UserGroup(id: UUID, name: String)
  final case class UserGroupCreate(name: String)
  final case class AddUserToGroupParams(groupId: UUID, userId: UUID)
  final case class AddProductToGroupParams(groupId: UUID, productId: UUID)

}
