/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import base.SpecBase
import models.WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader
import models.{NormalMode, UploadedFile, UserAnswers}
import pages.{UploadLetterOfAuthorityPage, WhatIsYourRoleAsImporterPage}
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.VerifyLetterOfAuthorityView

import java.time.Instant
import scala.concurrent.Future

class VerifyLetterOfAuthorityControllerSpec extends SpecBase {

  private lazy val verifyLetterOfAuthorityRoute =
    routes.VerifyLetterOfAuthorityController.onPageLoad(NormalMode, draftId).url

  private val uploadedFile = UploadedFile.Success(
    reference = "reference",
    downloadUrl = "downloadUrl",
    uploadDetails = UploadedFile.UploadDetails(
      fileName = "fileName",
      fileMimeType = "fileMimeType",
      uploadTimestamp = Instant.now(),
      checksum = "checksum",
      size = 1337
    )
  )

  "VerifyLetterOfAuthority Controller" - {

    "when .onPageLoad" - {

      "must return OK and the correct view for page load" in {

        val ua: UserAnswers = userAnswersAsOrgUser
          .set(UploadLetterOfAuthorityPage, uploadedFile)
          .success
          .value
          .set(WhatIsYourRoleAsImporterPage, AgentOnBehalfOfTrader)
          .success
          .value

        val application =
          applicationBuilder(userAnswers = Some(ua)).build()

        val request                = FakeRequest(GET, verifyLetterOfAuthorityRoute)
        val result: Future[Result] = route(application, request).value
        val view                   = application.injector.instanceOf[VerifyLetterOfAuthorityView]

        status(result) mustBe OK
        contentAsString(result) mustBe view(uploadedFile, draftId, NormalMode)(
          request,
          messages(application)
        ).toString

      }

      "must redirect to Upload Letter Of Authority when no fileName is present" in {
        val ua: UserAnswers = userAnswersAsOrgUser
          .set(UploadLetterOfAuthorityPage, UploadedFile.Initiated(reference = "reference"))
          .success
          .value
          .set(WhatIsYourRoleAsImporterPage, AgentOnBehalfOfTrader)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        val request     =
          FakeRequest(GET, verifyLetterOfAuthorityRoute)
        val result      = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UploadLetterOfAuthorityController
          .onPageLoad(
            mode = NormalMode,
            draftId = draftId,
            errorCode = None,
            key = None,
            redirectedFromChangeButton = false
          )
          .url
      }

      "must redirect to Journey Recovery if no document is found during page load" in {

        val application = applicationBuilder(userAnswers = None).build()
        val request     =
          FakeRequest(GET, verifyLetterOfAuthorityRoute)
        val result      = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "when .onSubmit" - {
      "must redirect to the next page 'Business Contact Details'" in {

        val ua: UserAnswers = userAnswersAsOrgUser
          .set(UploadLetterOfAuthorityPage, uploadedFile)
          .success
          .value
          .set(WhatIsYourRoleAsImporterPage, AgentOnBehalfOfTrader)
          .success
          .value

        val application =
          applicationBuilder(userAnswers = Some(ua)).build()

        val request                = FakeRequest(POST, verifyLetterOfAuthorityRoute)
        val result: Future[Result] = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.BusinessContactDetailsController
          .onPageLoad(NormalMode, draftId)
          .url
      }
    }
  }
}
