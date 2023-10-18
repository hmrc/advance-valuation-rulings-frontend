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

package userrole

import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat

import base.SpecBase
import forms.EmployeeCheckRegisteredDetailsFormProvider
import models.{ApplicationContactDetails, CDSEstablishmentAddress, DraftId, NormalMode, TraderDetailsWithCountryCode}
import models.requests.DataRequest
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.matchers.must.Matchers
import pages.ApplicationContactDetailsPage
import viewmodels.checkAnswers.summary._
import views.html.{CheckYourAnswersView, EmployeeCheckRegisteredDetailsView, EmployeeEORIBeUpToDateView, IndividualInformationRequiredView}

class EmployeeSpec extends SpecBase with Matchers {

  private val employeeCheckRegisteredDetailsView = mock[EmployeeCheckRegisteredDetailsView]
  private val formProvider                       = mock[EmployeeCheckRegisteredDetailsFormProvider]
  private val employeeEORIBeUpToDateView         = mock[EmployeeEORIBeUpToDateView]
  private val requiredInformationView            = mock[IndividualInformationRequiredView]
  private val checkYourAnswersView               = mock[CheckYourAnswersView]

  private val employee = Employee(
    employeeCheckRegisteredDetailsView,
    formProvider,
    checkYourAnswersView,
    employeeEORIBeUpToDateView,
    requiredInformationView
  )

  private val mockMessages    = mock[Messages]
  private val mockDataRequest = mock[DataRequest[AnyContent]]

  "Employee" - {

    "should return the correct ApplicationSummary" in {
      val summary: (ApplicantSummary, EoriDetailsSummary) =
        employee.getApplicationSummary(emptyUserAnswers, traderDetailsWithCountryCode)(
          mockMessages
        )
      summary.isInstanceOf[(IndividualApplicantSummary, IndividualEoriDetailsSummary)] mustBe true
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
      val expectedView: HtmlFormat.Appendable = mock[HtmlFormat.Appendable]

      val appSummary = ApplicationSummary(
        IndividualEoriDetailsSummary(traderDetailsWithCountryCode, draftId, emptyUserAnswers)(
          stubMessages()
        ),
        IndividualApplicantSummary(emptyUserAnswers)(stubMessages()),
        DetailsSummary(emptyUserAnswers)(stubMessages()),
        MethodSummary(emptyUserAnswers)(stubMessages())
      )

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

      val expectedView: HtmlFormat.Appendable = mock[HtmlFormat.Appendable]
      val form                                = mock[Form[Boolean]]
      val request                             = mock[DataRequest[AnyContent]]
      val draftId                             = DraftId(1L)
      val messages                            = mock[Messages]

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
  }

  "should return the correct view for EORIBeUpToDate" in {

    val expectedView: HtmlFormat.Appendable = mock[HtmlFormat.Appendable]

    val request  = mock[DataRequest[AnyContent]]
    val draftId  = DraftId(1L)
    val messages = mock[Messages]

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

    val expectedView: HtmlFormat.Appendable = mock[HtmlFormat.Appendable]

    val request  = mock[DataRequest[AnyContent]]
    val draftId  = DraftId(1L)
    val messages = mock[Messages]

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
        .getEORIDetailsJourney(draftId)
        .url mustBe controllers.routes.CheckRegisteredDetailsController
        .onPageLoad(NormalMode, draftId)
        .url
    }

  }
}
