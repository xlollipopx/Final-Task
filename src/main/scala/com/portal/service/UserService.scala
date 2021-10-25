package com.portal.service

import com.portal.dto.user.UserDto
import com.portal.validation.UserValidator

import java.util.UUID

//trait UserService[F[_]] {
//  def all: F[List[UserDto]]
//  def create(item: ProductItemWithCategoriesDto): F[Either[ProductValidationError, ProductItemWithCategoriesDto]]
//}
//object UserService {
//  def of[F[_]: Sync](
//                      productItemRepository: ProductItemRepository[F],
//                      validator:             UserValidator
//                    ): ProductItemService[F] =
//    new ProductItemServiceImpl[F](productItemRepository, validator)
//}
//object UserService {}
