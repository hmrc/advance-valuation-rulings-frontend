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

package userrole

import base.SpecBase
import forms.AgentForTraderCheckRegisteredDetailsFormProvider
import models.requests.DataRequest
import models._
import org.mockito.Mockito.{mock, when}
import org.scalatest.matchers.must.Matchers
import pages._
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import viewmodels.checkAnswers.summary._
import views.html._

import java.time.Instant

class AgentForTraderSpec extends SpecBase with Matchers {

  private val agentForTraderCheckRegisteredDetailsView =
    mock(classOf[VerifyPublicTraderDetailView])

  private val formProvider =
    mock(classOf[AgentForTraderCheckRegisteredDetailsFormProvider])

  private val agentForTraderPrivateEORIBeUpToDateView =
    mock(classOf[AgentForTraderPrivateEORIBeUpToDateView])

  private val agentForTraderPublicEORIBeUpToDateView =
    mock(classOf[AgentForTraderPublicEORIBeUpToDateView])

  private val requiredInformationView =
    mock(classOf[AgentForTraderRequiredInformationView])

  private val checkYourAnswersView = mock(classOf[AgentForTraderCheckYourAnswersView])

  private val agentSummaryCreator = mock(classOf[AgentSummaryCreator])

  private val traderEoriDetailsSummaryCreator = mock(classOf[TraderEoriDetailsSummaryCreator])

  private val agentForTrader = AgentForTrader(
    agentForTraderCheckRegisteredDetailsView,
    formProvider,
    checkYourAnswersView,
    agentForTraderPublicEORIBeUpToDateView,
    agentForTraderPrivateEORIBeUpToDateView,
    requiredInformationView,
    agentSummaryCreator,
    traderEoriDetailsSummaryCreator
  )

  private val mockMessages    = mock(classOf[Messages])
  private val mockDataRequest = mock(classOf[DataRequest[AnyContent]])

  "AgentForTrader" - {

    "should return the correct ApplicationSummary" in {
      val loaFilename = "totally_legit_authority_filename"
      val ua          = emptyUserAnswers
        .setFuture(
          UploadLetterOfAuthorityPage,
          UploadedFile.Success.apply(
            "",
            "",
            UploadedFile.UploadDetails.apply(loaFilename, "", Instant.now(), "", 1L)
          )
        )
        .futureValue
        .setFuture(
          VerifyTraderDetailsPage,
          TraderDetailsWithConfirmation(traderDetailsWithCountryCode)
        )
        .futureValue

      val agentSummary             = mock(classOf[AgentSummary])
      val traderEoriDetailsSummary = mock(classOf[TraderEoriDetailsSummary])

      when(agentSummaryCreator.summaryRows(ua)(mockMessages)).thenReturn(agentSummary)
      when(
        traderEoriDetailsSummaryCreator.summaryRows(
          traderDetailsWithCountryCode,
          draftId,
          loaFilename
        )(mockMessages)
      ).thenReturn(traderEoriDetailsSummary)

      val (applicantSummary, eoriDetailsSummary): (ApplicantSummary, EoriDetailsSummary) =
        agentForTrader.getApplicationSummary(ua, traderDetailsWithCountryCode)(
          mockMessages
        )
      applicantSummary mustBe agentSummary
      eoriDetailsSummary mustBe traderEoriDetailsSummary
    }

    "should throw exception when no data" in {
      val loaFilename = "totally_legit_authority_filename"
      val ua          = emptyUserAnswers
        .setFuture(
          UploadLetterOfAuthorityPage,
          UploadedFile.Success.apply(
            "",
            "",
            UploadedFile.UploadDetails.apply(loaFilename, "", Instant.now(), "", 1L)
          )
        )
        .futureValue

      val agentSummary             = mock(classOf[AgentSummary])
      val traderEoriDetailsSummary = mock(classOf[TraderEoriDetailsSummary])

      when(agentSummaryCreator.summaryRows(ua)(mockMessages)).thenReturn(agentSummary)
      when(
        traderEoriDetailsSummaryCreator.summaryRows(
          traderDetailsWithCountryCode,
          draftId,
          loaFilename
        )(mockMessages)
      ).thenReturn(traderEoriDetailsSummary)

      val exception = intercept[Exception] {
        agentForTrader.getApplicationSummary(ua, traderDetailsWithCountryCode)(
          mockMessages
        )
      }

      exception.getMessage mustBe "VerifyTraderDetailsPage needs to be answered(getApplicationSummary)"
    }

    "should return the correct ContactDetails for Application Request" in {
      val expected = BusinessContactDetails.apply(
        "test name",
        "name@domain.com",
        "01702123123",
        Some("company name"),
        "CEO"
      )
      val ua       = emptyUserAnswers.setFuture(BusinessContactDetailsPage, expected).futureValue
      val details  =
        agentForTrader.getContactDetailsForApplicationRequest(ua)

      details.toString mustEqual "Valid(ContactDetails(test name,name@domain.com,Some(01702123123),Some(company name),Some(CEO)))"
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
        agentForTraderCheckRegisteredDetailsView.apply(
          form,
          NormalMode,
          draftId,
          TraderDetailsWithConfirmation(traderDetailsWithCountryCode)
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

    "should return the correct form for CheckRegisteredDetails" in {
      val expectedForm = new AgentForTraderCheckRegisteredDetailsFormProvider().apply()

      when(formProvider.apply()).thenReturn(expectedForm)

      val actualForm = agentForTrader.getFormForCheckRegisteredDetails

      actualForm mustBe expectedForm
    }

    "should return the correct view for EORIBeUpToDate (Public)" in {

      val expectedView: HtmlFormat.Appendable = mock(classOf[HtmlFormat.Appendable])

      val request  = mock(classOf[DataRequest[AnyContent]])
      val draftId  = DraftId(1L)
      val messages = mock(classOf[Messages])

      when(
        agentForTraderPublicEORIBeUpToDateView.apply(
          draftId
        )(request, messages)
      ).thenReturn(expectedView)

      val actualView: HtmlFormat.Appendable = agentForTrader.selectViewForEoriBeUpToDate(
        draftId
      )(request, messages)

      actualView mustBe expectedView
    }

    "should return the correct view for EORIBeUpToDate (Private)" in {

      val expectedView: HtmlFormat.Appendable = mock(classOf[HtmlFormat.Appendable])

      val request  = mock(classOf[DataRequest[AnyContent]])
      val draftId  = DraftId(1L)
      val messages = mock(classOf[Messages])

      when(
        agentForTraderPrivateEORIBeUpToDateView.apply(
          draftId
        )(request, messages)
      ).thenReturn(expectedView)

      val actualView: HtmlFormat.Appendable = agentForTrader.selectViewForEoriBeUpToDate(
        draftId,
        isPrivate = true
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

      val actualView: HtmlFormat.Appendable = agentForTrader.selectViewForRequiredInformation(
        draftId
      )(request, messages)

      actualView mustBe expectedView
    }

    "getEORIDetailsJourney" - {
      "should return ProvideEoriNumber page" in {
        agentForTrader
          .getEORIDetailsJourney(NormalMode, draftId)
          .url mustBe controllers.routes.ProvideTraderEoriController
          .onPageLoad(NormalMode, draftId)
          .url
      }
    }

    "getContactDetailsJourney should return" - {
      "should return BusinessContactDetails page" in {

        agentForTrader
          .getContactDetailsJourney(draftId)
          .url mustBe controllers.routes.BusinessContactDetailsController
          .onPageLoad(NormalMode, draftId)
          .url

      }
    }

    "sourceFromUA" in {
      agentForTrader.sourceFromUA mustBe true
    }

    "selectGetRegisteredDetailsPage" in {
      agentForTrader.selectGetRegisteredDetailsPage() mustBe AgentForTraderCheckRegisteredDetailsPage
    }

    "selectBusinessContactDetailsPage" in {
      agentForTrader.selectBusinessContactDetailsPage() mustBe AgentForTraderContactDetailsPage
    }

  }
}
