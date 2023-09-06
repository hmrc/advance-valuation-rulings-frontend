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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import base.SpecBase
import controllers.common.FileUploadHelper
import models.{NormalMode, UploadedFile, UserAnswers}
import models.upscan.UpscanInitiateResponse
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.UploadSupportingDocumentPage
import services.fileupload.FileService
import views.html.{UploadInProgressView, UploadSupportingDocumentsView}

class UploadInProgressControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockFileService = mock[FileService]

  private val mockFileUploadHelper = mock[FileUploadHelper]

  private val reference: String = "reference"

  private val initiatedFile = UploadedFile.Initiated(reference = reference)

  private val fileUrl        = "some/path/for/the/download/url"
  private val successfulFile = UploadedFile.Success(
    reference = reference,
    downloadUrl = fileUrl,
    uploadDetails = UploadedFile.UploadDetails(
      fileName = "fileName",
      fileMimeType = "fileMimeType",
      uploadTimestamp = Instant.now(),
      checksum = "checksum",
      size = 1337
    )
  )
  private val failedFile     = UploadedFile.Failure(
    reference = reference,
    failureDetails = UploadedFile.FailureDetails(
      failureReason = UploadedFile.FailureReason.Quarantine,
      failureMessage = Some("failureMessage")
    )
  )

  private val upscanInitiateResponse = UpscanInitiateResponse(
    reference = reference,
    uploadRequest = UpscanInitiateResponse.UploadRequest(
      href = "href",
      fields = Map(
        "field1" -> "value1",
        "field2" -> "value2"
      )
    )
  )

  private def getPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(
      POST,
      controllers.routes.UploadInProgressController
        .checkProgress(draftId, Some(reference))
        .url
    )

  private def testFallbackPageIsShown(userAnswers: UserAnswers): Unit = {
    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[FileService].toInstance(mockFileService)
      )
      .build()

    val view = application.injector.instanceOf[UploadSupportingDocumentsView]

    when(mockFileService.initiate(any(), any(), any())(any()))
      .thenReturn(Future.successful(upscanInitiateResponse))

    val request = FakeRequest(
      POST,
      controllers.routes.UploadInProgressController
        .checkProgress(draftId, Some("otherReference"))
        .url
    )

    val result = route(application, request).value

    status(result) mustEqual OK
    contentAsString(result) mustEqual view(
      draftId = draftId,
      upscanInitiateResponse = Some(upscanInitiateResponse),
      errorMessage = None
    )(messages(application), request).toString
  }

  "UploadInProgress Controller" - {

    "On page load" - {

      "must return OK and the correct view by default" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

        running(application) {
          val request =
            FakeRequest(GET, routes.UploadInProgressController.onPageLoad(draftId, None).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UploadInProgressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(NormalMode, draftId, None)(
            messages(application),
            request
          ).toString
        }
      }

      "when there is a successful file and the user has unexpectedly navigated back to this page" - {

        val userAnswers = userAnswersAsIndividualTrader
          .set(UploadSupportingDocumentPage, successfulFile)
          .success
          .value

        "must delete file using FileUploadHelper" in {
          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[FileService].toInstance(mockFileService),
              bind[FileUploadHelper].toInstance(mockFileUploadHelper)
            )
            .build()

          when(mockFileUploadHelper.removeFile(eqTo(NormalMode), eqTo(draftId), eqTo(fileUrl))(any()))
            .thenReturn(Future.successful(play.api.mvc.Results.Ok("")))
          when(
            mockFileUploadHelper.checkForStatus(
              eqTo(userAnswers),
              eqTo(UploadSupportingDocumentPage)
            )
          )
            .thenReturn(Some(successfulFile))

          val request = FakeRequest(
            GET,
            controllers.routes.UploadInProgressController
              .onPageLoad(draftId, None)
              .url
          )

          val result = route(application, request).value
          status(result) mustEqual OK

          verify(mockFileUploadHelper).removeFile(
            eqTo(NormalMode),
            eqTo(draftId),
            eqTo(fileUrl)
          )(any())
        }
      }
    }

    "Check Progress" - {
      "when there is an initiated file (upload in progress)" - {

        val userAnswers = userAnswersAsIndividualTrader
          .set(UploadSupportingDocumentPage, initiatedFile)
          .success
          .value

        "when the key matches the file" - {

          "must redirect to the next page" in {
            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[FileService].toInstance(mockFileService)
              )
              .build()

            val result = route(application, getPostRequest).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.UploadInProgressController
              .onPageLoad(draftId, Some(reference))
              .url
          }
        }

        "when the key does not match the file" - {

          "must show fallback page" in {
            testFallbackPageIsShown(userAnswers)
          }
        }
      }

      "when there is a successful file" - {

        val userAnswers = userAnswersAsIndividualTrader
          .set(UploadSupportingDocumentPage, successfulFile)
          .success
          .value

        "when the key matches the file" - {

          "must redirect to the next page" in {

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[FileService].toInstance(mockFileService),
                bind[Navigator].to(new FakeNavigator(onwardRoute))
              )
              .build()

            val result = route(application, getPostRequest).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "when the key does not match the file" - {

          "must show fallback page" in {
            testFallbackPageIsShown(userAnswers)
          }
        }
      }

      "when there is a failed file" - {

        val userAnswers =
          userAnswersAsIndividualTrader
            .set(UploadSupportingDocumentPage, failedFile)
            .success
            .value

        "must redirect back to the page with the relevant error code" in {

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[FileService].toInstance(mockFileService)
            )
            .build()

          when(mockFileService.initiate(any(), any(), any())(any()))
            .thenReturn(Future.successful(upscanInitiateResponse))

          val result = route(application, getPostRequest).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.UploadSupportingDocumentsController
            .onPageLoad(models.NormalMode, draftId, Some("Quarantine"), Some(reference))
            .url
        }
      }
    }
  }
}
