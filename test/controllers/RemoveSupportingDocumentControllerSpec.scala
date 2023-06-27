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

import scala.concurrent.Future

import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import base.SpecBase
import forms.RemoveSupportingDocumentFormProvider
import models.{Done, DraftId, Index, NormalMode, UploadedFile, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import queries.AllDocuments
import services.UserAnswersService
import views.html.RemoveSupportingDocumentView

class RemoveSupportingDocumentControllerSpec extends SpecBase {

  val userAnswers = userAnswersAsIndividualTrader

  "must return OK and the correct view for a GET" in new SpecSetup {
    val answers = (for {
      ua <- userAnswers.set(UploadedFilePage(Index(0)), successfulFile)
      ua <- ua.set(WasThisFileConfidentialPage(Index(0)), false)
    } yield ua).success.value

    val application = applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[UserAnswersService].toInstance(mockUserAnswersService),
        bind[PlayObjectStoreClient].toInstance(osClient),
        bind[Navigator].toInstance(mockNavigator)
      )
      .build()

    running(application) {
      val view = application.injector.instanceOf[RemoveSupportingDocumentView]

      val request = FakeRequest(confirmRemoveSupportingDocumentRoute)

      val result = route(application, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, NormalMode, draftId, Index(0), fileName)(
        request,
        messages(application)
      ).toString
    }
  }

  "must delete the given file and redirect" in new SpecSetup {

    when(mockUserAnswersService.set(any())(any()))
      .thenReturn(Future.successful(Done))
    when(osClient.deleteObject(any(), any())(any()))
      .thenReturn(Future.successful(()))
    when(mockNavigator.nextPage(any(), any(), any()))
      .thenReturn(onwardRoute)

    val answers = (for {
      ua <- userAnswers.set(UploadedFilePage(Index(0)), successfulFile)
      ua <- ua.set(WasThisFileConfidentialPage(Index(0)), false)
    } yield ua).success.value

    val application = applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[UserAnswersService].toInstance(mockUserAnswersService),
        bind[PlayObjectStoreClient].toInstance(osClient),
        bind[Navigator].toInstance(mockNavigator)
      )
      .build()

    val request = FakeRequest(removeSupportingDocumentRoute)
      .withFormUrlEncodedBody("value" -> "true")
    val result  = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual onwardRoute.url

    verify(mockUserAnswersService, times(1)).set(userAnswersCaptor.capture())(any())
    verify(osClient, times(1)).deleteObject(eqTo(Path.File("downloadUrl")), any())(any())
    verify(mockNavigator, times(1)).nextPage(
      eqTo(RemoveSupportingDocumentPage(Index(0))),
      eqTo(NormalMode),
      eqTo(userAnswers)
    )

    val updatedAnswers = userAnswersCaptor.getValue
    updatedAnswers.get(UploadedFilePage(Index(0))) mustBe empty
    updatedAnswers.get(WasThisFileConfidentialPage(Index(0))) mustBe empty
    updatedAnswers.get(AllDocuments) mustBe empty
  }

  "does not call the object store if the user answers 'No'" in new SpecSetup {
    val answers = (for {
      ua <- userAnswers.set(UploadedFilePage(Index(0)), successfulFile)
      ua <- ua.set(WasThisFileConfidentialPage(Index(0)), false)
    } yield ua).success.value

    val application = applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[UserAnswersService].toInstance(mockUserAnswersService),
        bind[PlayObjectStoreClient].toInstance(osClient),
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
      )
      .build()

    val request = FakeRequest(removeSupportingDocumentRoute)
      .withFormUrlEncodedBody("value" -> "false")
    val result  = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual onwardRoute.url

    verify(mockUserAnswersService, never()).set(userAnswersCaptor.capture())(any())
    verify(osClient, never()).deleteObject(eqTo(Path.File("downloadUrl")), any())(any())
  }

  "does not call object store if the file does not exist" in new SpecSetup {

    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[UserAnswersService].toInstance(mockUserAnswersService),
        bind[PlayObjectStoreClient].toInstance(osClient),
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
      )
      .build()

    val request = FakeRequest(removeSupportingDocumentRoute)
      .withFormUrlEncodedBody("value" -> "true")
    val result  = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual onwardRoute.url

    verify(osClient, never()).deleteObject(any(), any())(any())
  }

  "does not call object store if the file has no download url" in new SpecSetup {

    val answers = userAnswers
      .set(UploadedFilePage(Index(0)), UploadedFile.Initiated("reference"))
      .success
      .value

    val application = applicationBuilder(userAnswers = Some(answers))
      .overrides(
        bind[UserAnswersService].toInstance(mockUserAnswersService),
        bind[PlayObjectStoreClient].toInstance(osClient),
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
      )
      .build()

    val request = FakeRequest(removeSupportingDocumentRoute)
      .withFormUrlEncodedBody("value" -> "true")
    val result  = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual onwardRoute.url

    verify(osClient, never()).deleteObject(any(), any())(any())
  }

  "must redirect to Journey Recovery page when there are no user answers" in new SpecSetup {

    val application = applicationBuilder(userAnswers = None).build()
    val request     = FakeRequest(removeSupportingDocumentRoute)
      .withFormUrlEncodedBody("value" -> "true")
    val result      = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
  }
}

trait SpecSetup extends MockitoSugar {
  import java.time.Instant
  val fileName       = "fileName"
  val onwardRoute    = Call("GET", "/foo")
  val successfulFile = UploadedFile.Success(
    reference = "reference",
    downloadUrl = "downloadUrl",
    uploadDetails = UploadedFile.UploadDetails(
      fileName = fileName,
      fileMimeType = "fileMimeType",
      uploadTimestamp = Instant.now(),
      checksum = "checksum",
      size = 1337
    )
  )

  val draftId = DraftId(0)

  lazy val confirmRemoveSupportingDocumentRoute =
    routes.RemoveSupportingDocumentController.onPageLoad(NormalMode, draftId, Index(0))
  lazy val removeSupportingDocumentRoute        =
    routes.RemoveSupportingDocumentController.onSubmit(NormalMode, draftId, Index(0))

  val formProvider = new RemoveSupportingDocumentFormProvider()
  val form         = formProvider()

  val userAnswersCaptor: ArgumentCaptor[UserAnswers] =
    ArgumentCaptor.forClass(classOf[UserAnswers])

  val mockNavigator          = mock[Navigator]
  val mockUserAnswersService = mock[UserAnswersService]
  val osClient               = mock[PlayObjectStoreClient]
}
