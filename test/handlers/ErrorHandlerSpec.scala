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

package handlers

import base.SpecBase
import play.api.Application
import play.api.test.FakeRequest

class ErrorHandlerSpec extends SpecBase {

  val app: Application = applicationBuilder().build()

  val errorHandler = app.injector.instanceOf[ErrorHandler]

  "ErrorHandler" - {

    "return an error page" in {
      val result = errorHandler
        .standardErrorTemplate(
          pageTitle = "pageTitle",
          heading = "heading",
          message = "message"
        )(FakeRequest())
        .futureValue

      result.body must include("pageTitle")
      result.body must include("heading")
      result.body must include("message")
    }
  }
}
