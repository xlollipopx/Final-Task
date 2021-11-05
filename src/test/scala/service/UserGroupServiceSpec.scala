package service

import cats.effect.IO
import com.portal.domain.group.{UserGroup, UserGroupCreate}
import com.portal.repository.GroupRepository
import com.portal.service.GroupService
import org.scalamock.scalatest.MockFactory
import org.scalatest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.util.UUID

class UserGroupServiceSpec extends AnyFreeSpec with Matchers with MockFactory {
  val mockGroupRepository = stub[GroupRepository[IO]]

  object DataSetUp {
    val userGroup    = UserGroupCreate("Vip")
    val id           = UUID.fromString("07b2a5b3-2ecf-4f61-92ee-92fabc7fa794")
    val userGroupOne = UserGroup(id, "Vip")
    val userGroupTwo = UserGroup(id, "Vip-two")

  }

  def assertIO[A](ioOne: IO[A], ioTwo: IO[A]): IO[scalatest.Assertion] = {
    for {
      one <- ioOne
      two <- ioTwo
    } yield (assert(one == two))
  }

  import DataSetUp._

  "group" - {
    "new group" in {
      val actual = for {
        service <- IO(GroupService.of[IO](mockGroupRepository))
        _        = (mockGroupRepository.create _).when(*).returns(IO(1))
        res     <- service.create(userGroup)
      } yield res
      val expected = IO(true)
      assertIO(actual, expected).unsafeRunSync()
    }

    "all groups" in {
      val actual = for {
        service <- IO(GroupService.of[IO](mockGroupRepository))
        _        = (mockGroupRepository.all _).when().returns(IO(List(userGroupOne, userGroupTwo)))
        res     <- service.all()
      } yield res
      val expected = IO(List(userGroupOne, userGroupTwo))
      assertIO(actual, expected).unsafeRunSync()
    }
  }

}
