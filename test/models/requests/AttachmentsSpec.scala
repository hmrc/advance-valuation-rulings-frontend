/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.requests

import cats.data.NonEmptyList
import cats.data.Validated._

import uk.gov.hmrc.auth.core.AffinityGroup

import generators._
import models._
import models.fileupload.UploadId
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class AttachmentsSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ApplicationRequestGenerator {

  import AttachmentsSpec._

  "Attachments" should {
    "succeed when has no files to upload" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, false)
      } yield ua).success.get

      val result = Attachment(userAnswers)

      result shouldBe Valid(Seq.empty)
    }

    "succeed when has has files to upload" in {
      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(DoYouWantToUploadDocumentsPage, true)
        ua <- ua.set(UploadSupportingDocumentPage, files)
      } yield ua).success.get

      val result = Attachment(userAnswers)

      result shouldBe Valid(
        Seq(
          Attachment(
            id = "1",
            name = "file1",
            url = "http://localhost:9000/download/1",
            public = false,
            "application/pdf",
            100
          )
        )
      )
    }

    "ignore previous uploaded files when user does not want to upload" in {
      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(DoYouWantToUploadDocumentsPage, false)
        ua <- ua.set(UploadSupportingDocumentPage, files)
      } yield ua).success.get

      val result = Attachment(userAnswers)

      result shouldBe Valid(Seq.empty)
    }

    "return invalid for empty UserAnswers" in {
      val result = Attachment(emptyUserAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(
          DoYouWantToUploadDocumentsPage
        )
      )
    }
  }
}

object AttachmentsSpec extends Generators {
  val randomString: String      = stringsWithMaxLength(8).sample.get
  val applicationNumber: String = ApplicationNumber("GBAVR", 1).render

  val emptyUserAnswers: UserAnswers = UserAnswers("a", applicationNumber, AffinityGroup.Individual)

  val files = UploadedFiles(
    lastUpload = None,
    files = Map(
      UploadId("1") -> UploadedFile(
        "file1",
        "http://localhost:9000/download/1",
        true,
        "application/pdf",
        100
      )
    )
  )

}
