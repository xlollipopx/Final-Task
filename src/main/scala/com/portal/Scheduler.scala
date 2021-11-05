package com.portal

import cats.effect.{ExitCode, IO, IOApp}
import com.portal.context.AppContext.setUpScheduler

object Scheduler extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = setUpScheduler().use(_ => IO.never).as(ExitCode.Success)

}
