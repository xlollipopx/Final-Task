package com.portal.repository.impl.doobie

import cats.Functor
import cats.effect.Bracket
import com.portal.domain.product.{ProductItem, ProductItemWithCategories, ProductStatus}
import com.portal.repository.ProductItemRepository
import doobie.{ConnectionIO, Fragment, Transactor}
import doobie.implicits._
import cats.implicits._
import com.portal.domain.category.Category
import doobie.postgres.implicits._
import meta.implicits._
import cats.effect._
import com.portal.domain.supplier.Supplier

import java.util.UUID

class DoobieProductRepository[F[_]: Functor: Bracket[*[_], Throwable]](
  tx: Transactor[F]
) extends ProductItemRepository[F] {

  private val selectProduct: Fragment =
    fr"SELECT p.uuid, p.name, p.description, p.cost, p.publication_date, p.status, s.uuid, s.name FROM products" ++
      fr"AS p" ++
      fr"INNER JOIN suppliers AS s ON p.supplier_id = s.uuid"

  private val createProduct: Fragment = fr"INSERT INTO products(" ++
    fr"uuid, name, description, cost, publication_date, status, supplier_id)"

  private val updateProduct: Fragment = fr"UPDATE products"
  private val deleteProduct: Fragment = fr"DELETE FROM products"

  private val createProductCategories: Fragment = fr"INSERT INTO product_categories(" ++
    fr"category_id, product_id)"

  private val updateCategory: Fragment = fr"UPDATE categories"

  private val updateSupplier: Fragment = fr"UPDATE suppliers"

  override def all(): F[List[ProductItemWithCategories]] = for {
    list <- selectProduct.query[ProductItem].to[List].transact(tx)
    res = list.map(x =>
      for {
        c <- getCategoriesById(x.id.value)
        pc = ProductItemWithCategories(x, c)
      } yield pc
    )
    ans <- res.traverse(identity)
  } yield ans

  override def create(item: ProductItemWithCategories): F[Int] = {
    val product = item.product
    val res = for {
      a <- {
        (createProduct ++ fr"VALUES(${product.id}, ${product.name}, ${product.description}, ${product.cost}, " ++
          fr"${product.publicationDate}, ${product.status}, ${product.supplier.id.value})").update.run
      }
      _ <- createCategoriesForProduct(product.id.value, item.categories)
    } yield a
    res.transact(tx)
  }

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

  override def update(item: ProductItemWithCategories): F[Int] = {
    val res = for {
      a <- {
        (updateProduct ++
          fr"SET name = ${item.product.name}, " ++
          fr"description = ${item.product.description}, " ++
          fr"cost = ${item.product.cost}, " ++
          fr"publication_date = ${item.product.publicationDate}, " ++
          fr"status = ${item.product.status} " ++
          fr"WHERE uuid = ${item.product.id}").update.run
      }
      _ <- updateCategoriesForProduct(item.product.id.value, item.categories)
      _ <- updateSuppliers(item.product.supplier)
    } yield a
    res.transact(tx)
  }

  override def delete(id: UUID): F[Int] =
    (deleteProduct ++ fr"WHERE uuid = $id").update.run.transact(tx)

  def getCategoriesById(id: UUID): F[List[Category]] = {
    (fr"SELECT c.uuid, c.name, c.description FROM product_categories AS pc" ++
      fr"INNER JOIN categories AS c ON c.uuid = pc.category_id WHERE pc.product_id = ${id}")
      .query[Category]
      .to[List]
      .transact(tx)
  }

  def createCategoriesForProduct(id: UUID, list: List[Category]): ConnectionIO[List[Int]] = for {
    l <- list
      .map(x => (createProductCategories ++ fr"VALUES(${x.id.value}, $id)").update.run)
      .traverse(identity)

  } yield l

  def updateCategoriesForProduct(id: UUID, list: List[Category]): ConnectionIO[List[Int]] = for {
    l <- list
      .map(x =>
        (updateCategory ++ fr"SET name = ${x.name}, description = ${x.description} WHERE uuid = ${id}").update.run
      )
      .traverse(identity)

  } yield l

  def updateSuppliers(supplier: Supplier): ConnectionIO[Int] = {
    (updateSupplier ++ fr"SET name = ${supplier.name}  WHERE uuid = ${supplier.id.value}").update.run
  }

}
