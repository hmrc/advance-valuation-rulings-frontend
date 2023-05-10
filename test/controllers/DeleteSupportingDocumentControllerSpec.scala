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

import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import base.SpecBase
import controllers.ViewApplicationControllerSpec.hc
import models.{Done, DraftId, Index, NormalMode, UploadedFile, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{IsThisFileConfidentialPage, UploadSupportingDocumentPage}
import services.UserAnswersService

class DeleteSupportingDocumentControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute = Call("GET", "/foo")

  private val successfulFile = UploadedFile.Success(
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

  private lazy val deleteSupportingDocumentRoute =
    routes.DeleteSupportingDocumentController.onDelete(Index(0), NormalMode, DraftId(0))

  "must delete the given file and redirect" in {

    val userAnswersCaptor: ArgumentCaptor[UserAnswers] =
      ArgumentCaptor.forClass(classOf[UserAnswers])

    val mockUserAnswersService = mock[UserAnswersService]
    val osClient               = mock[PlayObjectStoreClient]

    when(mockUserAnswersService.set(any())(any()))
      .thenReturn(Future.successful(Done))
    when(osClient.deleteObject(any(), any())(any()))
      .thenReturn(Future.successful(()))

    val answers = userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage(Index(0)), successfulFile)
      .success
      .value
      .set(IsThisFileConfidentialPage(Index(0)), false)
      .success
      .value

    val application = applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[UserAnswersService].toInstance(mockUserAnswersService),
        bind[PlayObjectStoreClient].toInstance(osClient),
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
      )
      .build()

    val request = FakeRequest(deleteSupportingDocumentRoute)
    val result  = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual onwardRoute.url

    verify(mockUserAnswersService).set(userAnswersCaptor.capture())(any())
    verify(osClient).deleteObject(eqTo(Path.File("downloadUrl")), any())(any())

    val updatedAnswers = userAnswersCaptor.getValue
    updatedAnswers.get(UploadSupportingDocumentPage(Index(0))) mustBe empty
    updatedAnswers.get(IsThisFileConfidentialPage(Index(0))) mustBe empty
  }

  "does not call object store if the file does not exist" in {

    val mockUserAnswersService = mock[UserAnswersService]
    val osClient               = mock[PlayObjectStoreClient]

    val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
      .overrides(
        bind[UserAnswersService].toInstance(mockUserAnswersService),
        bind[PlayObjectStoreClient].toInstance(osClient),
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
      )
      .build()

    val request = FakeRequest(deleteSupportingDocumentRoute)
    val result  = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual onwardRoute.url

    verify(osClient, times(0)).deleteObject(any(), any())(any())
  }

  "does not call object store if the file has no download url" in {

    val answers = userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage(Index(0)), UploadedFile.Initiated("reference"))
      .success
      .value

    val mockUserAnswersService = mock[UserAnswersService]
    val osClient               = mock[PlayObjectStoreClient]

    val application = applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[UserAnswersService].toInstance(mockUserAnswersService),
        bind[PlayObjectStoreClient].toInstance(osClient),
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
      )
      .build()

    val request = FakeRequest(deleteSupportingDocumentRoute)
    val result  = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual onwardRoute.url

    verify(osClient, times(0)).deleteObject(any(), any())(any())
  }

  "must redirect to Journey Recovery page when there are no user answers" in {

    val application = applicationBuilder(userAnswers = None).build()
    val request     = FakeRequest(deleteSupportingDocumentRoute)
    val result      = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
  }
}
