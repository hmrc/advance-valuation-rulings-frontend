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

import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup

import base.SpecBase
import generators.Generators
import models.ValuationMethod
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import viewmodels.checkAnswers.summary.ApplicationSummary
import views.html.ApplicationCompleteView

class ApplicationCompleteControllerSpec extends SpecBase with Generators {

  "ApplicationComplete Controller" - {
    "when an Individual completes an application" - {
      "must return OK and the correct view for a GET" in {
        ScalaCheckPropertyChecks.forAll(arbitraryUserData.arbitrary) {
          ua =>
            val userAnswers   = ua.set(ValuationMethodPage, ValuationMethod.Method2).success.value
            val email         = "testEmail@mail.com"
            val name          = "Jonny"
            val applicationId = userAnswers.draftId

            val emailUpdate          =
              (__ \ ApplicationContactDetailsPage.toString \ "email").json.put(JsString(email))
            val nameUpdate           =
              (__ \ ApplicationContactDetailsPage.toString \ "name").json.put(JsString(name))
            val dataWithEmail        = userAnswers.data.transform(__.json.update(emailUpdate)).get
            val dataWithEmailAndName = dataWithEmail
              .transform(__.json.update(nameUpdate))
              .get

            val answers     = userAnswers.copy(data = dataWithEmailAndName)
            val application = applicationBuilder(userAnswers = Option(answers)).build()

            running(application) {
              val request       =
                FakeRequest(
                  GET,
                  routes.ApplicationCompleteController.onPageLoad(applicationId).url
                )
              implicit val msgs = messages(application)
              val result        = route(application, request).value

              val view    = application.injector.instanceOf[ApplicationCompleteView]
              val summary = ApplicationSummary(userAnswers, AffinityGroup.Individual).removeActions
              status(result) mustEqual OK
              contentAsString(result) mustEqual view(true, applicationId, email, summary)(
                request,
                messages(application)
              ).toString
            }
        }
      }

      "must redirect when the user has no contact email" in {
        ScalaCheckPropertyChecks.forAll(arbitraryUserData.arbitrary) {
          ua =>
            val userAnswers    = ua.set(ValuationMethodPage, ValuationMethod.Method2).success.value
            val applicationId  = userAnswers.draftId
            val updatedAnswers =
              userAnswers.remove(pages.ApplicationContactDetailsPage).success.value

            val application = applicationBuilder(userAnswers = Option(updatedAnswers)).build()

            running(application) {
              val request =
                FakeRequest(
                  GET,
                  routes.ApplicationCompleteController.onPageLoad(applicationId).url
                )
              val result  = route(application, request).value
              status(result) mustEqual SEE_OTHER
            }
        }
      }
    }

    "when an Organisation completes an application" - {
      "must return OK and the correct view for a GET" in {
        ScalaCheckPropertyChecks.forAll(arbitraryUserData.arbitrary) {
          ua =>
            val Email       = "testEmail@mail.com"
            val userAnswers = (for {
              ua <- ua.set(ValuationMethodPage, ValuationMethod.Method2)
              ua <- ua.set(
                      BusinessContactDetailsPage,
                      models.BusinessContactDetails("test", Email, "test", "test")
                    )
            } yield ua).success.value

            val applicationId = userAnswers.draftId

            val application = applicationBuilderAsOrg(userAnswers = Option(userAnswers)).build()

            running(application) {
              val request       =
                FakeRequest(
                  GET,
                  routes.ApplicationCompleteController.onPageLoad(applicationId).url
                )
              implicit val msgs = messages(application)
              val result        = route(application, request).value

              val view    = application.injector.instanceOf[ApplicationCompleteView]
              val summary =
                ApplicationSummary(userAnswers, AffinityGroup.Organisation).removeActions

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(false, applicationId, Email, summary)(
                request,
                messages(application)
              ).toString
            }
        }
      }

      "must redirect when the company has no contact email" in {
        ScalaCheckPropertyChecks.forAll(arbitraryUserData.arbitrary) {
          ua =>
            val userAnswers    = ua.set(ValuationMethodPage, ValuationMethod.Method2).success.value
            val applicationId  = userAnswers.draftId
            val updatedAnswers =
              userAnswers.remove(pages.BusinessContactDetailsPage).success.value

            val application = applicationBuilderAsOrg(userAnswers = Option(updatedAnswers)).build()

            running(application) {
              val request =
                FakeRequest(
                  GET,
                  routes.ApplicationCompleteController.onPageLoad(applicationId).url
                )
              val result  = route(application, request).value
              status(result) mustEqual SEE_OTHER
            }
        }
      }
    }
  }
}
