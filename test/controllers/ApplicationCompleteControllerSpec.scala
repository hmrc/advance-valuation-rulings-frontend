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

import scala.concurrent.Future

import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup

import base.SpecBase
import connectors.BackendConnector
import generators.ApplicationGenerator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import org.mockito.MockitoSugar.{reset, times}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewmodels.checkAnswers.summary.ApplicationCompleteSummary
import views.html.ApplicationCompleteView

class ApplicationCompleteControllerSpec
    extends SpecBase
    with MockitoSugar
    with ApplicationGenerator
    with TableDrivenPropertyChecks {

  private val mockBackendConnector = mock[BackendConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockBackendConnector)
  }

  "ApplicationComplete Controller" - {

    val affinityGroupScenarios = Table(
      ("affinityGroup", "applicationBuilder", "isIndividual"),
      (AffinityGroup.Individual, applicationBuilder(), true),
      (AffinityGroup.Organisation, applicationBuilderAsOrg(), false),
      (AffinityGroup.Agent, applicationBuilderAsAgent(), false)
    )

    forAll(affinityGroupScenarios) {
      (affinityGroup, applicationBuilder, isIndividual) =>
        s"must return OK and the correct view for a GET when affinity group is $affinityGroup" in {

          ScalaCheckPropertyChecks.forAll(arbitraryApplication.arbitrary) {
            rulingsApplication =>
              val applicationId = rulingsApplication.id.toString
              val email         = rulingsApplication.contact.email
              val application   = applicationBuilder
                .overrides(bind[BackendConnector].toInstance(mockBackendConnector))
                .build()

              implicit val msgs: Messages = messages(application)

              when(mockBackendConnector.getApplication(any())(any()))
                .thenReturn(Future.successful(rulingsApplication))

              running(application) {
                val request =
                  FakeRequest(
                    GET,
                    routes.ApplicationCompleteController.onPageLoad(applicationId).url
                  )

                val result = route(application, request).value

                val view    = application.injector.instanceOf[ApplicationCompleteView]
                val summary = ApplicationCompleteSummary(rulingsApplication, affinityGroup)
                  .removeActions()

                status(result) mustEqual OK
                contentAsString(result) mustEqual view(
                  isIndividual,
                  applicationId,
                  email,
                  summary
                )(
                  request,
                  messages(application)
                ).toString
              }

              verify(mockBackendConnector, times(1)).getApplication(eqTo(applicationId))(any())
          }
        }
    }

    "must redirect to Journey Recovery for a GET when unable to retrieve submitted application" in {
      ScalaCheckPropertyChecks.forAll(arbitraryApplication.arbitrary) {
        rulingsApplication =>
          val applicationId = rulingsApplication.id.toString
          val application   = applicationBuilder()
            .overrides(bind[BackendConnector].toInstance(mockBackendConnector))
            .build()

          when(mockBackendConnector.getApplication(any())(any()))
            .thenReturn(Future.failed(new Exception("Backend error")))

          running(application) {
            val request =
              FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad(applicationId).url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController
              .onPageLoad()
              .url
          }
      }
    }
  }
}
