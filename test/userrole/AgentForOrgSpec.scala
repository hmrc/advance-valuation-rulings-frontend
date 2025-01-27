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
import controllers.routes.CheckRegisteredDetailsController
import forms.AgentForOrgCheckRegisteredDetailsFormProvider
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

class AgentForOrgSpec extends SpecBase with Matchers {

  private val agentForOrgCheckRegisteredDetailsView = mock(classOf[AgentForOrgCheckRegisteredDetailsView])
  private val formProvider                          = mock(classOf[AgentForOrgCheckRegisteredDetailsFormProvider])
  private val agentForOrgEORIBeUpToDateView         = mock(classOf[AgentForOrgEORIBeUpToDateView])
  private val requiredInformationView               = mock(classOf[AgentForOrgRequiredInformationView])
  private val checkYourAnswersView                  = mock(classOf[AgentForOrgCheckYourAnswersView])
  private val agentSummaryCreator                   = mock(classOf[AgentSummaryCreator])
  private val businessEoriDetailsSummaryCreator     = mock(classOf[BusinessEoriDetailsSummaryCreator])

  private val agentForOrg = AgentForOrg(
    agentForOrgCheckRegisteredDetailsView,
    formProvider,
    checkYourAnswersView,
    agentForOrgEORIBeUpToDateView,
    requiredInformationView,
    agentSummaryCreator,
    businessEoriDetailsSummaryCreator
  )

  private val mockMessages    = mock(classOf[Messages])
  private val mockDataRequest = mock(classOf[DataRequest[AnyContent]])

  "AgentForOrg" - {

    "should return the correct ApplicationSummary" in {

      val userAnswers = emptyUserAnswers
        .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)
        .get

      val populatedAgentSummary       = mock(classOf[AgentSummary])
      val populatedEoriDetailsSummary = mock(classOf[BusinessEoriDetailsSummary])

      when(agentSummaryCreator.summaryRows(userAnswers)(mockMessages))
        .thenReturn(populatedAgentSummary)
      when(
        businessEoriDetailsSummaryCreator.summaryRows(traderDetailsWithCountryCode, draftId)(
          mockMessages
        )
      ).thenReturn(populatedEoriDetailsSummary)

      val (applicantSummary, eoriDetailsSummary): (ApplicantSummary, EoriDetailsSummary) =
        agentForOrg.getApplicationSummary(userAnswers, traderDetailsWithCountryCode)(
          mockMessages
        )

      applicantSummary mustBe populatedAgentSummary
      eoriDetailsSummary mustBe populatedEoriDetailsSummary

    }

    "should return the correct ContactDetails for Application Request" in {
      val expected = BusinessContactDetails.apply(
        "test name",
        "name@domain.com",
        "01702123123",
        None,
        "CEO"
      )
      val ua       = emptyUserAnswers.setFuture(BusinessContactDetailsPage, expected).futureValue
      val details  =
        agentForOrg.getContactDetailsForApplicationRequest(ua)

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

    "should return the correct form for CheckRegisteredDetails" in {
      val expectedForm = new AgentForOrgCheckRegisteredDetailsFormProvider().apply()

      when(formProvider.apply()).thenReturn(expectedForm)

      val actualForm = agentForOrg.getFormForCheckRegisteredDetails

      actualForm mustBe expectedForm
    }
  }

  "should return the correct view for EORIBeUpToDate" in {

    val expectedView: HtmlFormat.Appendable = mock(classOf[HtmlFormat.Appendable])

    val request  = mock(classOf[DataRequest[AnyContent]])
    val draftId  = DraftId(1L)
    val messages = mock(classOf[Messages])

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

    val expectedView: HtmlFormat.Appendable = mock(classOf[HtmlFormat.Appendable])

    val request  = mock(classOf[DataRequest[AnyContent]])
    val draftId  = DraftId(1L)
    val messages = mock(classOf[Messages])

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
      agentForOrg
        .getEORIDetailsJourney(NormalMode, draftId)
        .url mustBe CheckRegisteredDetailsController
        .onPageLoad(NormalMode, draftId)
        .url
    }
  }

  "getContactDetailsJourney should return" - {
    "should return BusinessContactDetails page" in {
      agentForOrg
        .getContactDetailsJourney(draftId)
        .url mustBe controllers.routes.BusinessContactDetailsController
        .onPageLoad(NormalMode, draftId)
        .url

    }
  }

  "sourceFromUA" in {
    agentForOrg.sourceFromUA mustBe false
  }

  "contactDetailsIncludeCompanyName" in {
    agentForOrg.contactDetailsIncludeCompanyName mustBe false
  }

  "selectBusinessContactDetailsPage" in {
    agentForOrg.selectBusinessContactDetailsPage() mustBe AgentForOrgApplicationContactDetailsPage
  }

  "selectGetRegisteredDetailsPage" in {
    agentForOrg.selectGetRegisteredDetailsPage() mustBe AgentForOrgCheckRegisteredDetailsPage
  }

}
