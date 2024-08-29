/*
 * Copyright 2024 HM Revenue & Customs
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

package userrole

import base.SpecBase
import forms.EmployeeCheckRegisteredDetailsFormProvider
import models.requests.DataRequest
import models._
import org.mockito.Mockito.{mock, when}
import org.scalatest.matchers.must.Matchers
import pages.{ApplicationContactDetailsPage, CheckRegisteredDetailsPage}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import viewmodels.checkAnswers.summary._
import views.html._

class EmployeeSpec extends SpecBase with Matchers {

  private val employeeCheckRegisteredDetailsView  = mock(classOf[EmployeeCheckRegisteredDetailsView])
  private val formProvider                        = mock(classOf[EmployeeCheckRegisteredDetailsFormProvider])
  private val employeeEORIBeUpToDateView          = mock(classOf[EmployeeEORIBeUpToDateView])
  private val requiredInformationView             = mock(classOf[IndividualInformationRequiredView])
  private val checkYourAnswersView                = mock(classOf[CheckYourAnswersView])
  private val individualApplicantSummaryCreator   = mock(classOf[IndividualApplicantSummaryCreator])
  private val individualEoriDetailsSummaryCreator = mock(classOf[IndividualEoriDetailsSummaryCreator])

  private val employee = Employee(
    employeeCheckRegisteredDetailsView,
    formProvider,
    checkYourAnswersView,
    employeeEORIBeUpToDateView,
    requiredInformationView,
    individualApplicantSummaryCreator,
    individualEoriDetailsSummaryCreator
  )

  private val mockMessages    = mock(classOf[Messages])
  private val mockDataRequest = mock(classOf[DataRequest[AnyContent]])

  "Employee" - {

    "should return the correct ApplicationSummary" in {

      val individualApplicantSummary   = mock(classOf[IndividualApplicantSummary])
      val individualEoriDetailsSummary = mock(classOf[IndividualEoriDetailsSummary])

      when(individualApplicantSummaryCreator.summaryRows(emptyUserAnswers)(mockMessages))
        .thenReturn(individualApplicantSummary)
      when(
        individualEoriDetailsSummaryCreator.summaryRows(
          traderDetailsWithCountryCode,
          draftId,
          emptyUserAnswers
        )(mockMessages)
      ).thenReturn(individualEoriDetailsSummary)

      val (applicantSummary, eoriDetailsSummary): (ApplicantSummary, EoriDetailsSummary) =
        employee.getApplicationSummary(emptyUserAnswers, traderDetailsWithCountryCode)(
          mockMessages
        )
      applicantSummary mustBe individualApplicantSummary
      eoriDetailsSummary mustBe individualEoriDetailsSummary
    }

    "should return the correct ContactDetails for Application Request" in {
      val expected = ApplicationContactDetails.apply(
        "test name",
        "name@domain.com",
        "01702123123",
        "CEO"
      )
      val ua       = emptyUserAnswers.setFuture(ApplicationContactDetailsPage, expected).futureValue
      val details  =
        employee.getContactDetailsForApplicationRequest(ua)

      details.toString mustEqual "Valid(ContactDetails(test name,name@domain.com,Some(01702123123),None,Some(CEO)))"
    }

    "should return the correct view for CheckYourAnswers" in {
      val expectedView: HtmlFormat.Appendable = mock(classOf[HtmlFormat.Appendable])

      val appSummary = mock(classOf[ApplicationSummary])

      when(
        checkYourAnswersView.apply(
          appSummary,
          draftId
        )(mockDataRequest, mockMessages)
      ).thenReturn(expectedView)

      val actualView =
        employee.selectViewForCheckYourAnswers(appSummary, draftId)(mockDataRequest, mockMessages)

      actualView mustBe expectedView
    }

    "should return the correct view for CheckRegisteredDetails" in {
      val cDSEstablishmentAddress: CDSEstablishmentAddress = new CDSEstablishmentAddress(
        "",
        "",
        "",
        None
      )

      val expectedView: HtmlFormat.Appendable = mock(classOf[HtmlFormat.Appendable])
      val form                                = mock(classOf[Form[Boolean]])
      val request                             = mock(classOf[DataRequest[AnyContent]])
      val draftId                             = DraftId(1L)
      val messages                            = mock(classOf[Messages])

      val traderDetailsWithCountryCode =
        TraderDetailsWithCountryCode(
          "",
          consentToDisclosureOfPersonalData = true,
          "",
          cDSEstablishmentAddress,
          None
        )
      when(
        employeeCheckRegisteredDetailsView.apply(
          form,
          traderDetailsWithCountryCode,
          NormalMode,
          draftId
        )(request, messages)
      ).thenReturn(expectedView)

      val actualView: HtmlFormat.Appendable = employee.selectViewForCheckRegisteredDetails(
        form,
        traderDetailsWithCountryCode,
        NormalMode,
        draftId
      )(request, messages)

      actualView mustBe expectedView
    }

    "should return the correct form for CheckRegisteredDetails" in {
      val expectedForm = new EmployeeCheckRegisteredDetailsFormProvider().apply()

      when(formProvider.apply()).thenReturn(expectedForm)

      val actualForm = employee.getFormForCheckRegisteredDetails

      actualForm mustBe expectedForm
    }
  }

  "should return the correct view for EORIBeUpToDate" in {

    val expectedView: HtmlFormat.Appendable = mock(classOf[HtmlFormat.Appendable])

    val request  = mock(classOf[DataRequest[AnyContent]])
    val draftId  = DraftId(1L)
    val messages = mock(classOf[Messages])

    when(
      employeeEORIBeUpToDateView.apply(
        draftId
      )(request, messages)
    ).thenReturn(expectedView)

    val actualView: HtmlFormat.Appendable = employee.selectViewForEoriBeUpToDate(
      draftId
    )(request, messages)

    actualView mustBe expectedView
  }

  "should return the correct view for selectViewForRequiredInformation" in {

    val expectedView: HtmlFormat.Appendable = mock(classOf[HtmlFormat.Appendable])

    val request  = mock(classOf[DataRequest[AnyContent]])
    val draftId  = DraftId(1L)
    val messages = mock(classOf[Messages])

    when(
      requiredInformationView.apply(
        draftId
      )(request, messages)
    ).thenReturn(expectedView)

    val actualView: HtmlFormat.Appendable = employee.selectViewForRequiredInformation(
      draftId
    )(request, messages)

    actualView mustBe expectedView
  }

  "getEORIDetailsJourney" - {
    "should return CheckRegisteredDetails page" in {
      employee
        .getEORIDetailsJourney(NormalMode, draftId)
        .url mustBe controllers.routes.CheckRegisteredDetailsController
        .onPageLoad(NormalMode, draftId)
        .url
    }
  }

  "getContactDetailsJourney should return" - {
    "should return ApplicationContactDetails page" in {

      employee
        .getContactDetailsJourney(draftId)
        .url mustBe controllers.routes.ApplicationContactDetailsController
        .onPageLoad(NormalMode, draftId)
        .url

    }
  }

  "selectGetRegisteredDetailsPage" in {
    employee.selectGetRegisteredDetailsPage() mustBe CheckRegisteredDetailsPage
  }

}
