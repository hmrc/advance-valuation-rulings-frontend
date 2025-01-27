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

package controllers

import base.SpecBase
import play.api.Application
import play.api.mvc.Result
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.Future

class LanguageSwitchControllerSpec extends SpecBase {

  val app: Application = applicationBuilder().build()

  def getLanguageCookies(of: Future[Result]): String =
    cookies(of).get("PLAY_LANG").get.value

  "when translation is enabled switching language" - {
    "set the language to Cymraeg" in {
      val request = FakeRequest(Helpers.GET, routes.LanguageSwitchController.switchToLanguage("cy").url)

      val result = route(app, request).get

      status(result) mustBe SEE_OTHER
      getLanguageCookies(result) mustBe "cy"
    }

    "set the language to English" in {
      val request = FakeRequest(Helpers.GET, routes.LanguageSwitchController.switchToLanguage("en").url)

      val result = route(app, request).get

      status(result) mustBe SEE_OTHER
      getLanguageCookies(result) mustBe "en"
    }
  }

}
