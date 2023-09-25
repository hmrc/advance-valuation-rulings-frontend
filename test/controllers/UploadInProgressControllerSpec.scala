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

import play.api.Application
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import controllers.common.FileUploadHelper
import models.{NormalMode, UploadedFile, UserAnswers}
import models.requests.DataRequest
import models.upscan.UpscanInitiateResponse
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import org.mockito.MockitoSugar.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import pages.{UploadLetterOfAuthorityPage, UploadSupportingDocumentPage}
import services.UserAnswersService
import services.fileupload.FileService
import userrole.{UserRole, UserRoleProvider}
import views.html.{UploadInProgressView, UploadLetterOfAuthorityView, UploadSupportingDocumentsView}

class UploadInProgressControllerSpec
    extends SpecBase
    with MockitoSugar
    with BeforeAndAfterEach
    with TableDrivenPropertyChecks {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFileService, mockFileUploadHelper)
  }

  private val parameterisedCases = Table("Is Letter of Authority", true, false)

  private val mockFileService = mock[FileService]

  private val mockFileUploadHelper = mock[FileUploadHelper]

  private val reference: String = "reference"

  private val initiatedFile = UploadedFile.Initiated(reference = reference)

  private val mockUserRole           = mock[UserRole]
  private val mockUserRoleProvider   = mock[UserRoleProvider]
  private val mockUserAnswersService = mock[UserAnswersService]

  private def setMockUserRole(userAnswers: UserAnswers): Unit = {
    when(mockUserAnswersService.get(any())(any()))
      .thenReturn(Future.successful(Some(userAnswers)))

    when(mockUserRoleProvider.getUserRole(any()))
      .thenReturn(mockUserRole)

    when(mockUserRole.getMaxSupportingDocuments).thenReturn(3)
  }

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

  private def getUserAnswers(file: UploadedFile, isLetterOfAuthority: Boolean): UserAnswers =
    if (isLetterOfAuthority) {
      userAnswersAsIndividualTrader
        .set(UploadLetterOfAuthorityPage, file)
        .success
        .value
    } else {
      userAnswersAsIndividualTrader
        .set(UploadSupportingDocumentPage, file)
        .success
        .value
    }

  private def getPostRequest(isLetterOfAuthority: Boolean): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(
      POST,
      controllers.routes.UploadInProgressController
        .checkProgress(draftId, Some(reference), isLetterOfAuthority)
        .url
    )

  private def getRedirectPath(isLetterOfAuthority: Boolean): String =
    if (isLetterOfAuthority) {
      controllers.routes.UploadLetterOfAuthorityController
        .onPageLoad(NormalMode, draftId, None, None, false)
        .url
    } else {
      controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(NormalMode, draftId, None, None)
        .url
    }

  private def expectedContentForView(
    isLetterOfAuthority: Boolean,
    application: Application,
    request: FakeRequest[AnyContentAsEmpty.type]
  ) =
    if (isLetterOfAuthority) {
      val view = application.injector.instanceOf[UploadLetterOfAuthorityView]
      view(
        draftId = draftId,
        upscanInitiateResponse = Some(upscanInitiateResponse),
        errorMessage = None
      )(messages(application), request).toString
    } else {
      val view = application.injector.instanceOf[UploadSupportingDocumentsView]
      view(
        draftId = draftId,
        upscanInitiateResponse = Some(upscanInitiateResponse),
        errorMessage = None,
        expectedSupportedDocumentLimit
      )(messages(application), request).toString
    }

  private def expectedUrlForView(isLetterOfAuthority: Boolean) = {
    val mode      = models.NormalMode
    val id        = draftId
    val errorCode = Some("Quarantine")
    val key       = Some(reference)

    if (isLetterOfAuthority) {
      routes.UploadLetterOfAuthorityController.onPageLoad(mode, id, errorCode, key, false).url
    } else {
      routes.UploadSupportingDocumentsController.onPageLoad(mode, id, errorCode, key).url
    }
  }

  private def testFallbackPageIsShown(file: UploadedFile): Unit =
    forAll(parameterisedCases) {
      (isLetterOfAuthority: Boolean) =>
        val userAnswers = getUserAnswers(file, isLetterOfAuthority)
        setMockUserRole(userAnswers)
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileService].toInstance(mockFileService),
            bind[UserRoleProvider].toInstance(mockUserRoleProvider),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

        lazy val redirectPath: String = getRedirectPath(isLetterOfAuthority)

        when(
          mockFileService.initiate(
            eqTo(draftId),
            eqTo(redirectPath),
            eqTo(isLetterOfAuthority)
          )(any())
        )
          .thenReturn(Future.successful(upscanInitiateResponse))

        val request = FakeRequest(
          POST,
          controllers.routes.UploadInProgressController
            .checkProgress(draftId, Some("otherReference"), isLetterOfAuthority)
            .url
        )

        val expectedContent = expectedContentForView(isLetterOfAuthority, application, request)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedContent
    }

  private val expectedSupportedDocumentLimit = 3
  private def setUpUserRoleProviderMock()    = {
    when(mockUserRole.getMaxSupportingDocuments).thenReturn(expectedSupportedDocumentLimit)
    when(mockUserRoleProvider.getUserRole(any[UserAnswers]))
      .thenReturn(mockUserRole)
  }

  "UploadInProgress Controller" - {

    "On page load" - {

      "must return OK and the correct view by default" in {

        val isLetterOfAuthority = false // Page redirection is not tested in this test.
        val application         =
          applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

        running(application) {
          val request =
            FakeRequest(
              GET,
              routes.UploadInProgressController.onPageLoad(draftId, None, isLetterOfAuthority).url
            )

          val result = route(application, request).value

          val view = application.injector.instanceOf[UploadInProgressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(NormalMode, draftId, None, isLetterOfAuthority)(
            messages(application),
            request
          ).toString
        }
      }

      "when there is a successful file and the user has unexpectedly navigated back to this page" - {

        "must delete file using FileUploadHelper" in {
          val isLetterOfAuthority = false // Page redirection is not tested in this test.
          val userAnswers         = getUserAnswers(successfulFile, isLetterOfAuthority)
          val application         = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[FileService].toInstance(mockFileService),
              bind[FileUploadHelper].toInstance(mockFileUploadHelper)
            )
            .build()

          when(
            mockFileUploadHelper.removeFile(
              eqTo(NormalMode),
              eqTo(draftId),
              eqTo(fileUrl),
              eqTo(isLetterOfAuthority)
            )(any(), any())
          )
            .thenReturn(Future.successful(play.api.mvc.Results.Ok("")))
          when(
            mockFileUploadHelper.checkForStatus(
              eqTo(userAnswers),
              eqTo(isLetterOfAuthority)
            )
          )
            .thenReturn(Some(successfulFile))

          val request = FakeRequest(
            GET,
            controllers.routes.UploadInProgressController
              .onPageLoad(draftId, None, isLetterOfAuthority)
              .url
          )

          val result = route(application, request).value
          status(result) mustEqual OK

          verify(mockFileUploadHelper).removeFile(
            eqTo(NormalMode),
            eqTo(draftId),
            eqTo(fileUrl),
            eqTo(isLetterOfAuthority)
          )(any(), any())
        }

        "Parameterised: must redirect to fallback page" in {
          forAll(parameterisedCases) {
            (isLetterOfAuthority: Boolean) =>
              val mockDataRequest = mock[DataRequest[Any]]
              val userAnswers     = getUserAnswers(successfulFile, isLetterOfAuthority)
              setMockUserRole(userAnswers)
              val application     = applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(
                  bind[DataRequest[Any]].toInstance(mockDataRequest),
                  bind[FileService].toInstance(mockFileService),
                  bind[UserRoleProvider].toInstance(mockUserRoleProvider),
                  bind[UserAnswersService].toInstance(mockUserAnswersService)
                )
                .build()

              lazy val redirectPath: String = getRedirectPath(isLetterOfAuthority)

              when(mockDataRequest.userAnswers).thenReturn(userAnswers)
              when(
                mockFileService.initiate(
                  eqTo(draftId),
                  eqTo(redirectPath),
                  eqTo(isLetterOfAuthority)
                )(any())
              )
                .thenReturn(Future.successful(upscanInitiateResponse))

              running(application) {
                val request = FakeRequest(
                  GET,
                  routes.UploadInProgressController
                    .onPageLoad(draftId, None, isLetterOfAuthority)
                    .url
                )

                val expectedContent =
                  expectedContentForView(isLetterOfAuthority, application, request)

                val result = route(application, request).value

                status(result) mustEqual OK
                contentAsString(result) mustEqual expectedContent
              }
          }
        }
      }
    }

    "Check Progress" - {
      "when there is an initiated file (upload in progress)" - {

        "when the key matches the file" - {

          "must remain on the loading page" in {
            val isLetterOfAuthority = false // Page redirection is not tested in this test.
            val userAnswers         = getUserAnswers(initiatedFile, isLetterOfAuthority)

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[FileService].toInstance(mockFileService),
                bind[UserRoleProvider].toInstance(mockUserRoleProvider),
                bind[UserAnswersService].toInstance(mockUserAnswersService)
              )
              .build()

            val result = route(application, getPostRequest(isLetterOfAuthority)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.UploadInProgressController
              .onPageLoad(draftId, Some(reference), isLetterOfAuthority)
              .url
          }
        }

        "when the key does not match the file" - {

          "Parameterised: must show fallback page" in {
            testFallbackPageIsShown(initiatedFile)
          }
        }
      }

      "when there is a successful file" - {

        "when the key matches the file" - {

          "must redirect to the next page" in {

            // Since the onward route is a fake URL,
            // this test is not concerned with the value of isLetterOfAuthority.
            val isLetterOfAuthority = false

            val userAnswers = getUserAnswers(successfulFile, isLetterOfAuthority)

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[FileService].toInstance(mockFileService),
                bind[Navigator].to(new FakeNavigator(onwardRoute))
              )
              .build()

            val result = route(application, getPostRequest(isLetterOfAuthority)).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "when the key does not match the file" - {

          "Parameterised: must show fallback page" in {
            testFallbackPageIsShown(successfulFile)
          }
        }
      }

      "when there is a failed file" - {

        "Parameterised: must redirect back to the page with the relevant error code" in {
          forAll(parameterisedCases) {
            (isLetterOfAuthority: Boolean) =>
              val userAnswers = getUserAnswers(failedFile, isLetterOfAuthority)

              val application = applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(
                  bind[FileService].toInstance(mockFileService)
                )
                .build()

              lazy val redirectPath: String = getRedirectPath(isLetterOfAuthority)

              when(
                mockFileService.initiate(
                  eqTo(draftId),
                  eqTo(redirectPath),
                  eqTo(isLetterOfAuthority)
                )(any())
              )
                .thenReturn(Future.successful(upscanInitiateResponse))

              val expectedUrl = expectedUrlForView(isLetterOfAuthority)

              val result = route(application, getPostRequest(isLetterOfAuthority)).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual expectedUrl
          }
        }
      }
    }
  }
}
