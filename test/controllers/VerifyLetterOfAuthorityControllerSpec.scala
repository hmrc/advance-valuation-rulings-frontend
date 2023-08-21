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

package controllers

import java.time.Instant

import scala.concurrent.Future

import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, route, status, writeableOf_AnyContentAsEmpty, GET}

import base.SpecBase
import models.{UploadedFile, UserAnswers}
import models.WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader
import org.scalatestplus.mockito.MockitoSugar
import pages.{UploadLetterOfAuthorityPage, WhatIsYourRoleAsImporterPage}
import views.html.VerifyLetterOfAuthorityView

class VerifyLetterOfAuthorityControllerSpec extends SpecBase with MockitoSugar {

  private lazy val verifyLetterOfAuthorityRoute =
    routes.VerifyLetterOfAuthorityController.onPageLoad(draftId).url

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

    "must return OK and the correct view for page load" in {

      val ua: UserAnswers = userAnswersAsOrgAdmin
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

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(uploadedFile, draftId)(
        request,
        messages(application)
      ).toString

    }

    "must redirect to Journey Recovery if no document is found during page load" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     =
        FakeRequest(GET, verifyLetterOfAuthorityRoute)
      val result      = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }
  }

}
