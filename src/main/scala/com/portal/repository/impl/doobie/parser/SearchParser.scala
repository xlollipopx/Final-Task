package com.portal.repository.impl.doobie.parser
import doobie.{ConnectionIO, Fragment, Transactor}
import doobie.implicits._
import com.portal.domain.product.{CostInterval, DateInterval, ProductItemSearch, ProductStatus}

object SearchParser {

  def parseSearch(item: ProductItemSearch): String = {
    List(
      parseValueLike("p.name", item.name),
      parseValueLike("p.description", item.description),
      parseIntervalInt("p.cost", item.cost),
      parseIntervalDate("p.publication_date", item.publicationDate),
      parseValueEquals("p.status", item.status),
      parseValueEquals("p.supplier_id", item.supplierId)
    ).filter(_.nonEmpty)
      .reduce(_ + " AND " + _)
  }

  def parseValueLike(column: String, value: Option[String]): String = {
    value.map(x => s"$column LIKE '%$x%' ").getOrElse("")
  }

  def parseValueEquals[A](column: String, value: Option[A]): String = {
    value.map(x => s"$column = $x").getOrElse("")
  }

  def parseIntervalInt(column: String, value: Option[CostInterval]): String = {
    value.map(x => makeQuery(column, x.min, x.max)).getOrElse("")
  }

  def parseIntervalDate(column: String, value: Option[DateInterval]): String = {
    value.map(x => makeQuery(column, x.min, x.max)).getOrElse("")
  }

  def makeQuery[A](column: String, min: A, max: A): String = {
    s"$column BETWEEN $min AND $max"
  }

}
