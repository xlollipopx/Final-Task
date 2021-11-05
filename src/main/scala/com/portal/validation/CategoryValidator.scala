package com.portal.validation

sealed trait CategoryValidatorError extends ValidationError
object CategoryValidatorError {

  final case object InvalidId extends CategoryValidatorError {
    override def toString: String = "Wrong id!"
  }

  final case object EmptyName extends CategoryValidatorError {
    override def toString: String = "Name must be nonempty!"
  }

}
