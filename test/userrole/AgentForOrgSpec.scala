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
import controllers.routes.CheckRegisteredDetailsController
import models.{ApplicationContactDetails, BusinessContactDetails, CDSEstablishmentAddress, DraftId, NormalMode, TraderDetailsWithCountryCode}
import models.requests.DataRequest
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.matchers.must.Matchers
import pages.{ApplicationContactDetailsPage, BusinessContactDetailsPage}
import viewmodels.checkAnswers.summary.{AgentSummary, ApplicantSummary, ApplicationSummary, BusinessEoriDetailsSummary, DetailsSummary, EoriDetailsSummary, IndividualApplicantSummary, IndividualEoriDetailsSummary, MethodSummary}
import views.html.{AgentForOrgCheckRegisteredDetailsView, AgentForOrgCheckYourAnswersView, AgentForOrgEORIBeUpToDateView, AgentForOrgRequiredInformationView}

class AgentForOrgSpec extends SpecBase with Matchers {

  private val agentForOrgCheckRegisteredDetailsView = mock[AgentForOrgCheckRegisteredDetailsView]
  private val agentForOrgEORIBeUpToDateView         = mock[AgentForOrgEORIBeUpToDateView]
  private val requiredInformationView               = mock[AgentForOrgRequiredInformationView]
  private val checkYourAnswersView                  = mock[AgentForOrgCheckYourAnswersView]

  private val agentForOrg = AgentForOrg(
    agentForOrgCheckRegisteredDetailsView,
    checkYourAnswersView,
    agentForOrgEORIBeUpToDateView,
    requiredInformationView
  )

  private val mockMessages    = mock[Messages]
  private val mockDataRequest = mock[DataRequest[AnyContent]]

  "AgentForOrg" - {

    "should return the correct ApplicationSummary" in {
      val summary: (ApplicantSummary, EoriDetailsSummary) =
        agentForOrg.getApplicationSummary(emptyUserAnswers, traderDetailsWithCountryCode)(
          mockMessages
        )
      summary.isInstanceOf[(AgentSummary, BusinessEoriDetailsSummary)] mustBe true
      // TODO check that we're getting the right stuff back
    }

    "should return the correct ContactDetails for Application Request" in {
      val expected = BusinessContactDetails.apply(
        "test name",
        "name@domain.com",
        "01702123123",
        None
      )
      val ua       = emptyUserAnswers.setFuture(BusinessContactDetailsPage, expected).futureValue
      val details  =
        agentForOrg.getContactDetailsForApplicationRequest(ua)

      details.toString mustEqual "Valid(ContactDetails(test name,name@domain.com,Some(01702123123),None))"
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
        agentForOrg.selectViewForCheckYourAnswers(appSummary, draftId)(
          mockDataRequest,
          mockMessages
        )

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
        agentForOrgCheckRegisteredDetailsView.apply(
          form,
          traderDetailsWithCountryCode,
          NormalMode,
          draftId
        )(request, messages)
      ).thenReturn(expectedView)

      val actualView: HtmlFormat.Appendable = agentForOrg.selectViewForCheckRegisteredDetails(
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
      agentForOrgEORIBeUpToDateView.apply(
        draftId
      )(request, messages)
    ).thenReturn(expectedView)

    val actualView: HtmlFormat.Appendable = agentForOrg.selectViewForEoriBeUpToDate(
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

    val actualView: HtmlFormat.Appendable = agentForOrg.selectViewForRequiredInformation(
      draftId
    )(request, messages)

    actualView mustBe expectedView
  }

  "getEORIDetailsJourney" - {
    "should return CheckRegisteredDetails page" in {
      agentForOrg.getEORIDetailsJourney(draftId).url mustBe CheckRegisteredDetailsController
        .onPageLoad(NormalMode, draftId)
        .url
    }

  }
}
