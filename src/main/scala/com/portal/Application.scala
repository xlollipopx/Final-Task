package com.portal

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect._
import com.portal.conf.app._
import com.portal.context.AppContext
import org.http4s.server.blaze.BlazeServerBuilder
import io.circe.config.parser
import org.http4s.server.Server
import scala.concurrent.ExecutionContext

object Application extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = serverResource[IO]
    .use(_ => IO.never)
    .as(ExitCode.Success)

  private def serverResource[F[_]: ContextShift: ConcurrentEffect: Timer: Async]: Resource[F, Server[F]] =
    for {

      conf    <- Resource.eval(parser.decodePathF[F, AppConf]("app"))
      httpApp <- AppContext.setUp[F](conf)
      server <- BlazeServerBuilder[F](ExecutionContext.global)
        .bindHttp(port = 9001, host = "localhost")
        .withHttpApp(httpApp)
        .resource

    } yield server
}
