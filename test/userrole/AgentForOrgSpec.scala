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
import play.twirl.api.HtmlFormat

import models.{CDSEstablishmentAddress, DraftId, NormalMode, TraderDetailsWithCountryCode}
import models.requests.DataRequest
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import views.html.{AgentForOrgCheckRegisteredDetailsView, AgentForOrgEORIBeUpToDateView, AgentForOrgRequiredInformationView}

class AgentForOrgSpec extends AnyFreeSpec with Matchers {

  private val agentForOrgCheckRegisteredDetailsView = mock[AgentForOrgCheckRegisteredDetailsView]
  private val agentForOrgEORIBeUpToDateView         = mock[AgentForOrgEORIBeUpToDateView]
  private val requiredInformationView               = mock[AgentForOrgRequiredInformationView]

  private val agentForOrg = AgentForOrg(
    agentForOrgCheckRegisteredDetailsView,
    agentForOrgEORIBeUpToDateView,
    requiredInformationView
  )

  "AgentForOrg" - {
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

    val actualView: HtmlFormat.Appendable = agentForOrg.selectViewForEoriBeUpToDate(
      draftId
    )(request, messages)

    actualView mustBe expectedView
  }

}
