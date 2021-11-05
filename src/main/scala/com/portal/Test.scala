package com.portal

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import com.portal.TestClientDto._
import com.portal.domain.group.{AddProductToGroupParams, AddUserToGroupParams, UserGroup, UserGroupCreate}
import com.portal.domain.order.UserAddress
import com.portal.dto.order.OrderWithProductsDto
import com.portal.dto.product._
import com.portal.dto.user.{CourierWithPasswordDto, UserWithPasswordDto}
import dev.profunktor.auth.jwt.JwtToken
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.Method.{GET, PUT}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.http4s.dsl.io.POST
import org.http4s.headers.Authorization
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{AuthScheme, Credentials, Request}
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext

object TestClient extends IOApp {
  private val createClientUri      = uri"http://localhost:9001/auth/client"
  private val showCartUri          = uri"http://localhost:9001/shopping-cart"
  private val addProductToOrderUri = uri"http://localhost:9001/products/all/add-product"
  private val makeOrderUri         = uri"http://localhost:9001/shopping-cart/make-order"
  private val subscribeUri         = uri"http://localhost:9001/products/all/supplier/subscribe"

  private def printLine(string: String = ""): IO[Unit] = IO(println(string))

  def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .parZip(Blocker[IO])
      .use { case (client, blocker) =>
        for {
          logger <- Slf4jLogger.create[IO]
          clientToken <- {
            val req = POST(UserWithPasswordDto("Tip", "anton.stelmax@bk.ru", "qwerty1234").asJson, createClientUri)
            client.expect(req)(jsonOf[IO, JwtToken])
          }
          _ <- logger.info(clientToken.value)

          //add product to order
          reqAddProduct <- POST(QuantityDto(11), addProductToOrderUri / productIdOne)
          isAdded =
            reqAddProduct
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, clientToken.value))
              )

          res <- client.expect(isAdded)(jsonOf[IO, Boolean])

          _ <- logger.info("add product: " + res.toString)

          //see order
          showOrder =
            Request[IO](GET, showCartUri)
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, clientToken.value))
              )

          listOne <- client.expect(showOrder)(jsonOf[IO, OrderWithProductsDto])
          _       <- logger.info("Orders: " + listOne.toString)

          //make order
          reqMakeOrder <- PUT(UserAddress("Minsk"), makeOrderUri)
          isOrdered =
            reqMakeOrder
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, clientToken.value))
              )

          made <- client.expect(isOrdered)(jsonOf[IO, Boolean])

          _ <- logger.info("make order: " + made.toString)

          //subscribe
          subscribeReq <- POST(subscribeUri / supplierIdOne)
          isSubscribed =
            subscribeReq
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, clientToken.value))
              )

          subs <- client.expect(isSubscribed)(jsonOf[IO, Boolean])

          _ <- logger.info("subscribe: " + subs.toString)

        } yield ()
      }
      .as(ExitCode.Success)
}

object TestAdmin extends IOApp {
  private val createCategory    = uri"http://localhost:9001/admin/categories/create"
  private val createSupplier    = uri"http://localhost:9001/admin/suppliers/create"
  private val updateSupplier    = uri"http://localhost:9001/admin/suppliers/update"
  private val createProduct     = uri"http://localhost:9001/admin/products/create"
  private val updateProduct     = uri"http://localhost:9001/admin/products/update"
  private val deleteProduct     = uri"http://localhost:9001/admin/products/delete"
  private val createGroup       = uri"http://localhost:9001/admin/groups/create"
  private val allGroup          = uri"http://localhost:9001/admin/groups/all"
  private val addUserToGroup    = uri"http://localhost:9001/admin/groups/add-user"
  private val addProductToGroup = uri"http://localhost:9001/admin/groups/add-product-to-group"
  private val deleteGroup       = uri"http://localhost:9001/admin/groups/delete"

  def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .parZip(Blocker[IO])
      .use { case (client, blocker) =>
        for {
          logger <- Slf4jLogger.create[IO]

          //create category
          reqAddCategory <- POST(categoryCreate, createCategory)
          resOne =
            reqAddCategory
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, adminToken))
              )
          res1 <- client.expect(resOne)(jsonOf[IO, CategoryDto])
          _    <- logger.info("Category create: " + res1.toString)

          //create supplier
          reqAddSupplier <- POST(supplierCreate, createSupplier)
          resTwo =
            reqAddSupplier
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, adminToken))
              )
          res2 <- client.expect(resTwo)(jsonOf[IO, SupplierDto])
          _    <- logger.info(res2.toString)

          //update supplier
          reqUpdateSupplier <- POST(supplierUpdate, updateSupplier / supplierIdOne)
          resThree =
            reqUpdateSupplier
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, adminToken))
              )
          res3 <- client.expect(resThree)(jsonOf[IO, SupplierDto])
          _    <- logger.info("Supplier updated:" + res3.toString)

          //create product
          reqCreateProduct <- POST(productWihCategoriesDto, createProduct)
          resFour =
            reqCreateProduct
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, adminToken))
              )
          res4 <- client.expect(resFour)(jsonOf[IO, ProductItemWithCategoriesDtoModify])

          _ <- logger.info("Product created:" + res4.toString)

          //update product
          reqUpdateProduct <- POST(productWithCategoriesDtoUpdate, updateProduct / productIdOne)
          resFive =
            reqUpdateProduct
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, adminToken))
              )

          res5 <- client.expect(resFive)(jsonOf[IO, ProductItemWithCategoriesDtoModify])
          _    <- logger.info("Product updated: " + res5.toString)

          //create group
          reqCreateGroup <- POST(UserGroupCreate("Vip-3"), createGroup)
          resSeven =
            reqCreateGroup
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, adminToken))
              )

          res7 <- client.expect(resSeven)(jsonOf[IO, Boolean])
          _    <- logger.info("Group created:" + res7.toString)

          //all groups
          reqAllGroup =
            Request[IO](GET, allGroup)
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, adminToken))
              )

          res8 <- client.expect(reqAllGroup)(jsonOf[IO, List[UserGroup]])
          _    <- logger.info("All groups: " + res8.toString)

          //add user to group
          reqAddUserToGroup <- POST(addUserToGroupParams, addUserToGroup)
          resEight =
            reqAddUserToGroup
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, adminToken))
              )

          res9 <- client.expect(resEight)(jsonOf[IO, Boolean])
          _    <- logger.info("User added to group: " + res9.toString)

          //add product to group
          reqAddProductToGroup <- POST(addProductToGroupParams, addProductToGroup)
          resNine =
            reqAddProductToGroup
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, adminToken))
              )

          res10 <- client.expect(resNine)(jsonOf[IO, Boolean])
          _     <- logger.info("Product added to group: " + res10.toString)

        } yield ()
      }
      .as(ExitCode.Success)
}

object TestCourier extends IOApp {
  private val createCourierUri = uri"http://localhost:9001/auth/courier"
  private val availableOrders  = uri"http://localhost:9001/courier/available-orders"
  private val takeOrderUri     = uri"http://localhost:9001/courier/available-orders/take-order"
  private val myOrdersUri      = uri"http://localhost:9001/courier/my-orders"
  private val deliveredUri     = uri"http://localhost:9001/courier/my-orders/set-delivered"

  def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .parZip(Blocker[IO])
      .use { case (client, blocker) =>
        for {
          logger <- Slf4jLogger.create[IO]

          //create courier
          courierToken <- {
            val req = POST(courierDto.asJson, createCourierUri)
            client.expect(req)(jsonOf[IO, JwtToken])
          }
          _ <- logger.info(courierToken.value)

          //all available order
          reqAllOrder <- GET(availableOrders)
          resOne =
            reqAllOrder
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, courierToken.value))
              )

          res1 <- client.expect(resOne)(jsonOf[IO, List[OrderWithProductsDto]])
          _    <- logger.info("Available orders: " + res1.toString)

          //take order
          reqTakeOrders <- POST(takeOrderUri / res1.head.order.id)
          resTwo =
            reqTakeOrders
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, courierToken.value))
              )

          res2 <- client.expect(resTwo)(jsonOf[IO, Boolean])
          _    <- logger.info("Order taken: " + res2.toString)

          //all my orders
          reqMyOrders <- GET(myOrdersUri)
          resThree =
            reqMyOrders
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, courierToken.value))
              )

          res3 <- client.expect(resThree)(jsonOf[IO, List[OrderWithProductsDto]])
          _    <- logger.info("My orders: " + res3.toString)

          // set status delivered
          reqDeliver <- POST(deliveredUri / res3.head.order.id)
          resFour =
            reqDeliver
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, courierToken.value))
              )

          res4 <- client.expect(resFour)(jsonOf[IO, Boolean])
          _    <- logger.info("Delivered: " + res4.toString)

        } yield ()
      }
      .as(ExitCode.Success)
}

object TestClientDto {

  val adminToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
    "ezRkMmE5NWY5LTdiNjQtNDhlZi05MWNlLWQ3YzUwYjU3NzFkZH0.JosXbBNajlNxWGksSiB-AjOGkI8UMX2m_MFP7r3UUAY"
  val productIdOne   = "123e4267-e89b-12d3-a456-556642440000"
  val supplierIdOne  = "123e4267-e89b-12d3-a456-656642440000"
  val supplierIdTwo  = "999e4267-e89b-12d3-a456-656642440000"
  val categoryIdOne  = "133e4267-e89b-12d3-a456-623642419999"
  val categoryIdTwo  = "133e4267-e89b-12d3-a456-653642440000"
  val groupId        = "987e4267-e89b-12d3-a456-656642440001"
  val clientId       = "9240843d-1e12-4210-8027-f751895ee80a"
  val category       = CategoryDto(categoryIdOne, "clothes", "Something to wear")
  val categoryCreate = CategoryDtoModify("Jeans", "For legs")
  val supplierCreate = SupplierDtoModify("BMW")
  val supplierUpdate = SupplierDtoModify("WWW")
  val supplier       = SupplierDto(supplierIdOne, "...")
  val productDto =
    ProductItemDto("Tea", "Black", "19", "USD", LocalDate.now().toString, "Available", supplierIdTwo, "BBB")

  val categoriesDto =
    List(CategoryDto(categoryIdOne, "name", "description"), CategoryDto(categoryIdTwo, "name", "description"))

  val productWihCategoriesDto = ProductItemWithCategoriesDto(productDto, categoriesDto)

  val productDtoUpdate =
    ProductItemDto("Tea", "Green", "120", "USD", LocalDate.now().toString, "Available", supplierIdTwo, "BBB")
  val productWithCategoriesDtoUpdate =
    ProductItemWithCategoriesDtoModify(productDtoUpdate, List(CategoryIdDto(categoryIdTwo)))

  val addUserToGroupParams    = AddUserToGroupParams(UUID.fromString(groupId), UUID.fromString(clientId))
  val addProductToGroupParams = AddProductToGroupParams(UUID.fromString(groupId), UUID.fromString(productIdOne))

  val courierDto = CourierWithPasswordDto("Mel", "www@mail.ru", "+375294758476", "qwer123")

}
