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

import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat

import base.SpecBase
import models.WhatIsYourRoleAsImporter.{AgentOnBehalfOfOrg, EmployeeOfOrg}
import models.requests.DataRequest
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.WhatIsYourRoleAsImporterPage
import userrole.{UserRole, UserRoleProvider}
import views.html.{AgentForOrgRequiredInformationView, IndividualInformationRequiredView}

class RequiredInformationControllerSpec extends SpecBase with MockitoSugar {

  lazy val requiredInformationRoute =
    routes.RequiredInformationController.onPageLoad(draftId).url

  "RequiredInformation Controller" - {

    "must return OK and the correct view for individual user type" in {
      val ua          = userAnswersAsIndividualTrader.set(WhatIsYourRoleAsImporterPage, EmployeeOfOrg).get
      val application =
        applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, requiredInformationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndividualInformationRequiredView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(draftId)(
          request,
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view for non individual user type" in {
      val ua = userAnswersAsOrgAdmin.set(WhatIsYourRoleAsImporterPage, AgentOnBehalfOfOrg).get

      val application =
        applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, requiredInformationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentForOrgRequiredInformationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(draftId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for userRole where agentcreds flag is set" in {

      val userRoleProvider: UserRoleProvider = mock[UserRoleProvider]

      val userRole: UserRole = mock[UserRole]
      val expectedView       = HtmlFormat.raw("expected View")
      when(
        userRole.selectViewForRequiredInformation(ArgumentMatchers.eq(draftId))(
          any[DataRequest[AnyContent]],
          any[Messages]
        )
      ).thenReturn(expectedView)
      when(userRoleProvider.getUserRole(userAnswersAsOrgAdmin)).thenReturn(userRole)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsOrgAdmin))
          .overrides(
            bind[UserRoleProvider].toInstance(userRoleProvider)
          )
          .build()

      // THEN the actual rendered view equals this expected one
      running(application) {

        val request = FakeRequest(GET, requiredInformationRoute)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedView.toString()
      }
    }
  }
}
