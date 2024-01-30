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

package views

import forms.ChangeYourRoleImporterForm
import models.DraftId
import models.requests.DataRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.{AffinityGroup, User}
import views.behaviours.ViewBehaviours
import views.html.ChangeYourRoleImporterView

class ChangeYourRoleImporterViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "changeYourRoleImporter"

  val fakeDataRequest: DataRequest[_] = DataRequest(
    request = FakeRequest(),
    userId = userAnswersId,
    eoriNumber = EoriNumber,
    affinityGroup = AffinityGroup.Individual,
    credentialRole = Option(User),
    userAnswers = userAnswersAsIndividualTrader
  )

  val form = new ChangeYourRoleImporterForm()()

  private val view: ChangeYourRoleImporterView =
    app.injector.instanceOf[ChangeYourRoleImporterView]

  private val viewViaApply: HtmlFormat.Appendable  =
    view(form, DraftId(1L), onwardRoute)(fakeRequest, messages)
  private val viewViaRender: HtmlFormat.Appendable =
    view.render(form, DraftId(1L), onwardRoute, fakeRequest, messages)
  private val viewViaF: HtmlFormat.Appendable      =
    view.f(form, DraftId(1L), onwardRoute)(fakeRequest, messages)

  "ChangeYourRoleImporterView" - {

    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" - {
        behave like normalPage(view, messageKeyPrefix, "")()
      }

    val input: Seq[(String, HtmlFormat.Appendable)] =
      Seq(
        ".apply"  -> viewViaApply,
        ".render" -> viewViaRender,
        ".f"      -> viewViaF
      )

    input.foreach { case (method, view) =>
      test(method, view)
    }

    object Selectors extends BaseSelectors {
      val yesNoRadioLabel = "#main-content > div > div > form > div > fieldset > legend"
      val continueButton  = "#continue-button"
    }

    "should have the correct content" - {

      val expectedContent =
        Seq(
          Selectors.h1              -> "Change your role as importer",
          Selectors.subheading      -> "About the applicant",
          Selectors.p(1)            ->
            (
              "If you change your role as an importer you will lose all " +
                "information you have added to this application."
            ),
          Selectors.yesNoRadioLabel -> "Do you want to change your role as an importer?",
          Selectors.continueButton  -> "Continue"
        )

      behave like pageWithExpectedMessages(viewViaApply, expectedContent)
    }
  }
}
