package service

import cats.effect.IO
import com.portal.dto.product.{CategoryIdDto, ProductItemDto, ProductItemWithCategoriesDtoModify}
import com.portal.repository.ProductItemRepository
import com.portal.service.ProductItemService
import com.portal.validation.ProductItemValidator
import com.portal.validation.ProductValidationError.{InvalidDateFormat, InvalidMoneyFormat, InvalidStatus}
import org.scalamock.scalatest.MockFactory
import org.scalatest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.LocalDate
import java.util.UUID

class ProductItemSpec extends AnyFreeSpec with Matchers with MockFactory {

  val mockProductItem = stub[ProductItemRepository[IO]]

  object DataSetUp {
    val supplierIdTwo = "999e4267-e89b-12d3-a456-656642440000"
    val categoryIdOne = "133e4267-e89b-12d3-a456-623642419999"
    val id            = "133e4267-e89b-12d3-a456-623642419999"
    val productDtoUpdate =
      ProductItemDto("Tea", "Green", "120", "USD", LocalDate.now().toString, "Available", supplierIdTwo, "BBB")
    val productDtoInvalidCost =
      ProductItemDto("Tea", "Green", "12i0", "USD", LocalDate.now().toString, "Available", supplierIdTwo, "BBB")
    val productDtoInvalidDate =
      ProductItemDto("Tea", "Green", "120", "USD", "131-44-1", "Available", supplierIdTwo, "BBB")
    val productDtoInvalidStatus =
      ProductItemDto("Tea", "Green", "120", "USD", LocalDate.now().toString, "Avaiable", supplierIdTwo, "BBB")
    val productWithCategoriesDtoUpdate =
      ProductItemWithCategoriesDtoModify(productDtoUpdate, List(CategoryIdDto(categoryIdOne)))

    val productWithCategoriesDtoInvalidCost =
      ProductItemWithCategoriesDtoModify(productDtoInvalidCost, List(CategoryIdDto(categoryIdOne)))

    val productWithCategoriesDtoInvalidDate =
      ProductItemWithCategoriesDtoModify(productDtoInvalidDate, List(CategoryIdDto(categoryIdOne)))
    val productWithCategoriesDtoInvalidStatus =
      ProductItemWithCategoriesDtoModify(productDtoInvalidStatus, List(CategoryIdDto(categoryIdOne)))

    (mockProductItem.create _).when(*).returns(IO(1))
    (mockProductItem.update _).when(*).returns(IO(1))
  }

  import DataSetUp._

  def assertIO[A](ioOne: IO[A], ioTwo: IO[A]): IO[scalatest.Assertion] = {
    for {
      one <- ioOne
      two <- ioTwo
    } yield (assert(one == two))
  }

  "create product" - {

    "when data is valid" in {
      val actual = for {
        service <- IO(ProductItemService.of[IO](mockProductItem, new ProductItemValidator))
        res     <- service.create(productWithCategoriesDtoUpdate)
      } yield res
      val expected = IO(Right(productWithCategoriesDtoUpdate))
      assertIO(actual, expected).unsafeRunSync()
    }

    "when cost is invalid" in {
      val actual = for {
        service <- IO(ProductItemService.of[IO](mockProductItem, new ProductItemValidator))
        res     <- service.create(productWithCategoriesDtoInvalidCost)
      } yield res
      val expected = IO(Left(InvalidMoneyFormat))
      assertIO(actual, expected).unsafeRunSync()
    }

    "when date is invalid" in {
      val actual = for {
        service <- IO(ProductItemService.of[IO](mockProductItem, new ProductItemValidator))
        res     <- service.create(productWithCategoriesDtoInvalidDate)
      } yield res
      val expected = IO(Left(InvalidDateFormat))
      assertIO(actual, expected).unsafeRunSync()
    }

    "when status is invalid" in {
      val actual = for {
        service <- IO(ProductItemService.of[IO](mockProductItem, new ProductItemValidator))
        res     <- service.create(productWithCategoriesDtoInvalidStatus)
      } yield res
      val expected = IO(Left(InvalidStatus))
      assertIO(actual, expected).unsafeRunSync()
    }

  }

  "update product" - {

    "when data is valid" in {
      val actual = for {
        service <- IO(ProductItemService.of[IO](mockProductItem, new ProductItemValidator))
        res     <- service.update(UUID.fromString(id), productWithCategoriesDtoUpdate)
      } yield res
      val expected = IO(Right(productWithCategoriesDtoUpdate))
      assertIO(actual, expected).unsafeRunSync()
    }

    "when cost is invalid" in {
      val actual = for {
        service <- IO(ProductItemService.of[IO](mockProductItem, new ProductItemValidator))
        res     <- service.update(UUID.fromString(id), productWithCategoriesDtoInvalidCost)
      } yield res
      val expected = IO(Left(InvalidMoneyFormat))
      assertIO(actual, expected).unsafeRunSync()
    }

    "when date is invalid" in {
      val actual = for {
        service <- IO(ProductItemService.of[IO](mockProductItem, new ProductItemValidator))
        res     <- service.update(UUID.fromString(id), productWithCategoriesDtoInvalidDate)
      } yield res
      val expected = IO(Left(InvalidDateFormat))
      assertIO(actual, expected).unsafeRunSync()
    }

    "when status is invalid" in {
      val actual = for {
        service <- IO(ProductItemService.of[IO](mockProductItem, new ProductItemValidator))
        res     <- service.update(UUID.fromString(id), productWithCategoriesDtoInvalidStatus)
      } yield res
      val expected = IO(Left(InvalidStatus))
      assertIO(actual, expected).unsafeRunSync()
    }

  }

}
