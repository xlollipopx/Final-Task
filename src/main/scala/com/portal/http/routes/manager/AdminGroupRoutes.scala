package com.portal.http.routes.manager

import com.portal.domain.group.{AddProductToGroupParams, AddUserToGroupParams, UserGroupCreate}
import com.portal.http.auth.users.ManagerUser
import com.portal.service.{GroupService, SpecificProductsService}
import cats.Monad
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes, Response}

case class AdminGroupRoutes[F[_]: Monad: Sync](
  groupService: GroupService[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/groups"

  private val httpRoutes: AuthedRoutes[ManagerUser, F] = AuthedRoutes.of {

    case GET -> Root / "all" as manager =>
      for {
        item <- groupService.all()
        res  <- Ok(item)
      } yield res

    case req @ POST -> Root / "create" as manager =>
      req.req.as[UserGroupCreate].flatMap { dto =>
        for {
          item <- groupService.create(dto)
          res  <- Ok(item)
        } yield res
      }

    case POST -> Root / "delete" / UUIDVar(id) as manager =>
      for {
        item <- groupService.delete(id)
        res  <- Ok(item)
      } yield res

    case req @ POST -> Root / "add-user" as manager =>
      req.req.as[AddUserToGroupParams].flatMap { dto =>
        for {
          item <- groupService.addUser(dto.groupId, dto.userId)
          res  <- Ok(item)
        } yield res
      }

    case req @ POST -> Root / "add-product-to-group" as manager =>
      req.req.as[AddProductToGroupParams].flatMap { dto =>
        for {
          item <- groupService.addProduct(dto.groupId, dto.productId)
          res  <- Ok(item)
        } yield res
      }
  }

  def routes(authMiddleware: AuthMiddleware[F, ManagerUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
