package com.portal.repository.impl.doobie

import com.portal.domain.product._
import com.portal.domain.supplier.Supplier
import com.portal.domain.category.{Category, CategoryId}
import com.portal.repository.ProductItemRepository
import cats.Functor
import cats.effect.Bracket
import doobie.{ConnectionIO, Fragment, Transactor}
import doobie.implicits._
import cats.implicits._
import doobie.postgres.implicits._
import doobie.implicits.javasql._
import doobie.implicits.javatime._
import meta.implicits._
import cats.effect._
import com.portal.domain.product
import com.portal.repository.impl.doobie.parser.SearchParser
import doobie.postgres.pgisimplicits._

import java.util.UUID

class DoobieProductRepository[F[_]: Functor: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends ProductItemRepository[F] {

  private val selectProduct: Fragment =
    fr"SELECT p.uuid, p.name, p.description, p.cost, p.currency," ++
      fr" p.publication_date, p.status, p.supplier_id, s.name FROM products" ++
      fr"AS p" ++
      fr"INNER JOIN suppliers AS s ON p.supplier_id = s.uuid"

  private val createProduct: Fragment = fr"INSERT INTO products(" ++
    fr"uuid, name, description, cost, currency, publication_date, status, supplier_id)"

  private val updateProduct: Fragment = fr"UPDATE products"
  private val deleteProduct: Fragment = fr"DELETE FROM products"

  private val createProductCategories: Fragment = fr"INSERT INTO product_categories(" ++
    fr"category_id, product_id)"

  private val updateProductCategories: Fragment = fr"UPDATE product_categories"

  private val updateSupplier: Fragment = fr"UPDATE suppliers"

  override def all(): F[List[ProductItemWithCategories]] = for {
    list <- (selectProduct ++ fr"WHERE p.status != 'IN_PROCESSING'")
      .query[ProductItem]
      .to[List]
      .transact(tx)
    res <- fetchCategories(list)
  } yield res

  def fetchCategories(list: List[ProductItem]): F[List[ProductItemWithCategories]] = {
    val res = list.map(x =>
      for {
        c <- getCategoriesById(x.id.value)
        pc = ProductItemWithCategories(x, c)
      } yield pc
    )
    res.traverse(identity)
  }

  override def create(item: ProductItemWithCategoriesModify): F[Int] = {
    val product = item.product
    val res = for {
      a <- {
        (createProduct ++ fr"VALUES(${product.id}, ${product.name}," ++
          fr" ${product.description}, ${product.cost.amount.toInt}, ${product.cost.currency.toString}, " ++
          fr"${product.publicationDate}, ${product.status}, ${product.supplier.id.value})").update.run
      }
      _ <- createCategoriesForProduct(product.id.value, item.categories)
    } yield a
    res.transact(tx)
  }

  def createCategoriesForProduct(id: UUID, list: List[CategoryId]): ConnectionIO[List[Int]] = for {
    l <- list
      .map(x => (createProductCategories ++ fr"VALUES(${x.value}, $id)").update.run)
      .traverse(identity)

  } yield l

  override def findById(id: UUID): F[Option[ProductItemWithCategories]] = for {
    item <- (selectProduct ++ fr"WHERE p.uuid = ${id}").query[ProductItem].option.transact(tx)
    res <- item match {
      case Some(x) =>
        for {
          c <- getCategoriesById(x.id.value)
          pc = ProductItemWithCategories(x, c)
        } yield Option(pc)
      case None => None.pure[F]
    }
  } yield res

  override def setStatus(id: UUID, status: ProductStatus): F[Int] = {
    (updateProduct ++
      fr"SET status = $status " ++
      fr"WHERE uuid = $id").update.run.transact(tx)
  }

  override def update(item: ProductItemWithCategoriesModify): F[Int] = {
    val res = for {
      a <- {
        (updateProduct ++
          fr"SET name = ${item.product.name}, " ++
          fr"description = ${item.product.description}, " ++
          fr"cost = ${item.product.cost.amount.toInt}, " ++
          fr"currency = ${item.product.cost.currency.toString}, " ++
          fr"publication_date = ${item.product.publicationDate}, " ++
          fr"status = ${item.product.status}, " ++
          fr"supplier_id = ${item.product.supplier.id.value} " ++
          fr"WHERE uuid = ${item.product.id}").update.run
      }
      _ <- updateCategoriesForProduct(item.product.id.value, item.categories)
    } yield a
    res.transact(tx)
  }

  def updateCategoriesForProduct(id: UUID, list: List[CategoryId]): ConnectionIO[List[Int]] = for {
    _ <- fr"DELETE FROM product_categories WHERE product_id = ${id}".update.run
    l <- createCategoriesForProduct(id, list)
  } yield l

  override def delete(id: UUID): F[Int] =
    (deleteProduct ++ fr"WHERE uuid = $id").update.run.transact(tx)

  def getCategoriesById(id: UUID): F[List[Category]] = {
    (fr"SELECT c.uuid, c.name, c.description FROM product_categories AS pc" ++
      fr"INNER JOIN categories AS c ON c.uuid = pc.category_id WHERE pc.product_id = ${id}")
      .query[Category]
      .to[List]
      .transact(tx)
  }

  def updateSuppliers(supplier: Supplier): ConnectionIO[Int] = {
    (updateSupplier ++ fr"SET name = ${supplier.name}  WHERE uuid = ${supplier.id.value}").update.run
  }

  override def searchByCriteria(item: ProductItemSearch): F[List[ProductItemWithCategories]] = {
    val searchQuery = SearchParser.parseSearch(item)
    for {
      list <- (selectProduct ++ fr"WHERE p.status != 'IN_PROCESSING' " ++
        (if (searchQuery.nonEmpty) Fragment.const("AND " + searchQuery)
         else Fragment.const("")))
        .query[ProductItem]
        .to[List]
        .transact(tx)
      res <- fetchCategories(list)
      ans  = filterCategories(res, item.categoriesId)
    } yield ans
  }

  def filterCategories(
    list:       List[ProductItemWithCategories],
    categories: Option[List[UUID]]
  ): List[ProductItemWithCategories] = {
    categories match {
      case Some(l) => list.filter(x => l.equals(x.categories.map(y => y.id.value)))
      case None    => list
    }
  }

}
