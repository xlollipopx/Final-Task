package com.portal.validation

import com.portal.dto.product.CategoryDto

sealed trait CategoryValidatorError extends ValidationError
object CategoryValidatorError {

  final case object InvalidId extends CategoryValidatorError {
    override def toString: String = "Wrong id!"
  }

  final case object EmptyName extends CategoryValidatorError {
    override def toString: String = "Name must be nonempty!"
  }

}
