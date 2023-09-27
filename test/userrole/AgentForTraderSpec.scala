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

import java.time.Instant

import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat

import base.SpecBase
import models.{BusinessContactDetails, CDSEstablishmentAddress, DraftId, NormalMode, TraderDetailsWithConfirmation, TraderDetailsWithCountryCode, UploadedFile}
import models.requests.DataRequest
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.matchers.must.Matchers
import pages.{BusinessContactDetailsPage, UploadLetterOfAuthorityPage, VerifyTraderDetailsPage}
import viewmodels.checkAnswers.summary.{AgentSummary, ApplicantSummary, ApplicationSummary, DetailsSummary, EoriDetailsSummary, IndividualApplicantSummary, MethodSummary, TraderEoriDetailsSummary}
import views.html.{AgentForOrgCheckYourAnswersView, AgentForTraderCheckRegisteredDetailsView, AgentForTraderCheckYourAnswersView, AgentForTraderPrivateEORIBeUpToDateView, AgentForTraderPublicEORIBeUpToDateView, AgentForTraderRequiredInformationView}

class AgentForTraderSpec extends SpecBase with Matchers {

  private val agentForTraderCheckRegisteredDetailsView =
    mock[AgentForTraderCheckRegisteredDetailsView]

  private val agentForTraderPrivateEORIBeUpToDateView =
    mock[AgentForTraderPrivateEORIBeUpToDateView]

  private val agentForTraderPublicEORIBeUpToDateView =
    mock[AgentForTraderPublicEORIBeUpToDateView]

  private val requiredInformationView =
    mock[AgentForTraderRequiredInformationView]

  private val checkYourAnswersView = mock[AgentForTraderCheckYourAnswersView]

  private val agentForTrader = AgentForTrader(
    agentForTraderCheckRegisteredDetailsView,
    checkYourAnswersView,
    agentForTraderPublicEORIBeUpToDateView,
    agentForTraderPrivateEORIBeUpToDateView,
    requiredInformationView
  )

  private val mockMessages    = mock[Messages]
  private val mockDataRequest = mock[DataRequest[AnyContent]]

  "AgentForTrader" - {

    "should return the correct ApplicationSummary" in {
      val ua                                              = emptyUserAnswers
        .setFuture(
          UploadLetterOfAuthorityPage,
          UploadedFile.Success.apply(
            "",
            "",
            UploadedFile.UploadDetails.apply("", "", Instant.now(), "", 1L)
          )
        )
        .futureValue
        .setFuture(
          VerifyTraderDetailsPage,
          TraderDetailsWithConfirmation(traderDetailsWithCountryCode)
        )
        .futureValue
      val summary: (ApplicantSummary, EoriDetailsSummary) =
        agentForTrader.getApplicationSummary(ua, traderDetailsWithCountryCode)(
          mockMessages
        )
      summary.isInstanceOf[(AgentSummary, TraderEoriDetailsSummary)] mustBe true
    }

    "should return the correct ContactDetails for Application Request" in {
      val expected = BusinessContactDetails.apply(
        "test name",
        "name@domain.com",
        "01702123123",
        Some("company name")
      )
      val ua       = emptyUserAnswers.setFuture(BusinessContactDetailsPage, expected).futureValue
      val details  =
        agentForTrader.getContactDetailsForApplicationRequest(ua)

      details.toString mustEqual "Valid(ContactDetails(test name,name@domain.com,Some(01702123123),Some(company name)))"
    }

    "should return the correct view for CheckYourAnswers" in {
      val expectedView: HtmlFormat.Appendable = mock[HtmlFormat.Appendable]

      val appSummary = ApplicationSummary(
        TraderEoriDetailsSummary(traderDetailsWithCountryCode, draftId, "nofile.jpg")(mockMessages),
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
        agentForTrader.selectViewForCheckYourAnswers(appSummary, draftId)(
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
        agentForTraderCheckRegisteredDetailsView.apply(
          form,
          traderDetailsWithCountryCode,
          NormalMode,
          draftId
        )(request, messages)
      ).thenReturn(expectedView)

      val actualView: HtmlFormat.Appendable = agentForTrader.selectViewForCheckRegisteredDetails(
        form,
        traderDetailsWithCountryCode,
        NormalMode,
        draftId
      )(request, messages)

      actualView mustBe expectedView
    }

    "should return the correct view for EORIBeUpToDate (Public)" in {

      val expectedView: HtmlFormat.Appendable = mock[HtmlFormat.Appendable]

      val request  = mock[DataRequest[AnyContent]]
      val draftId  = DraftId(1L)
      val messages = mock[Messages]

      when(
        agentForTraderPublicEORIBeUpToDateView.apply(
          draftId
        )(request, messages)
      ).thenReturn(expectedView)

      val actualView: HtmlFormat.Appendable = agentForTrader.selectViewForEoriBeUpToDate(
        draftId,
        false
      )(request, messages)

      actualView mustBe expectedView
    }

    "should return the correct view for EORIBeUpToDate (Private)" in {

      val expectedView: HtmlFormat.Appendable = mock[HtmlFormat.Appendable]

      val request  = mock[DataRequest[AnyContent]]
      val draftId  = DraftId(1L)
      val messages = mock[Messages]

      when(
        agentForTraderPrivateEORIBeUpToDateView.apply(
          draftId
        )(request, messages)
      ).thenReturn(expectedView)

      val actualView: HtmlFormat.Appendable = agentForTrader.selectViewForEoriBeUpToDate(
        draftId,
        true
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

      val actualView: HtmlFormat.Appendable = agentForTrader.selectViewForRequiredInformation(
        draftId
      )(request, messages)

      actualView mustBe expectedView
    }

    "getEORIDetailsJourney" - {
      "should return ProvideEoriNumber page" in {
        agentForTrader
          .getEORIDetailsJourney(draftId)
          .url mustBe controllers.routes.ProvideTraderEoriController
          .onPageLoad(draftId)
          .url
      }

    }

  }
}
