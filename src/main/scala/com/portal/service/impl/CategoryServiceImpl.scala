package com.portal.service.impl

import cats.Monad
import cats.data.EitherT
import cats.effect.Sync
import com.portal.dto.product
import cats.implicits._
import com.portal.domain.category.{Category, CategoryId}
import com.portal.dto.product.{CategoryDto, CategoryDtoModify}
import com.portal.effects.GenUUID
import com.portal.repository.CategoryRepository
import com.portal.service.CategoryService
import com.portal.util.ModelMapper.CategoryDomainToDto
import com.portal.validation.ProductValidationError

import java.util.UUID

class CategoryServiceImpl[F[_]: Sync: Monad](categoryRepository: CategoryRepository[F]) extends CategoryService[F] {

  override def all(): F[List[CategoryDto]] =
    categoryRepository.all().flatMap(x => x.map(CategoryDomainToDto).pure[F])

  override def create(category: CategoryDtoModify): F[CategoryDto] =
    for {
      id <- GenUUID.forSync[F].make
      dto = CategoryDto(id.toString, category.name, category.description)
      _  <- categoryRepository.create(Category(CategoryId(id), category.name, category.description))
    } yield dto

  override def update(id: UUID, category: CategoryDtoModify): F[CategoryDto] =
    for {
      dto <- CategoryDto(id.toString, category.name, category.description).pure[F]
      _   <- categoryRepository.update(Category(CategoryId(id), category.name, category.description))
    } yield dto
}
