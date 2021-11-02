package com.portal

import cats.{Defer, Monad}
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect._
import cats.implicits._
import akka.actor.{Actor, ActorSystem, Cancellable, Props}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxApplicativeId
import com.portal.domain.auth.Email
import com.portal.domain.supplier.{Supplier, SupplierId, SupplierWithUsers, UserMails}
import com.portal.effects.EmailNotification
import com.portal.repository.SubscriptionRepository
import com.portal.service.SubscriptionService

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.duration.{Duration, DurationInt}

object Scheduler extends IOApp {

  class SchedulerMail extends Actor {

    override def receive: Receive = { case UserMails(list: List[SupplierWithUsers]) =>
      val emailVerification = new EmailNotification();
      list.map(s => s.mails.map(m => emailVerification.send(m.value, s.supplier.name)))
    }

  }

  object SchedulerMail {
    def startSchedule[F[_]: Sync: Monad](): F[Cancellable] = for {
      system <- (ActorSystem("System")).pure[F]
      actor  <- (system.actorOf(Props(classOf[SchedulerMail]), "Actor")).pure[F]
      users = UserMails(
        List(SupplierWithUsers(Supplier(SupplierId(UUID.randomUUID()), "Nyc"), List(Email("anton.stelmax@bk.ru"))))
      )
      _ <- (actor ! users).pure[F]
      res <- {
        import system.dispatcher
        system.scheduler.scheduleWithFixedDelay(3.seconds, 10.second, actor, users)
      }.pure[F]
    } yield res
  }

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      res <- SchedulerMail.startSchedule[IO]()
    } yield res
  }.as(ExitCode.Success)
}
