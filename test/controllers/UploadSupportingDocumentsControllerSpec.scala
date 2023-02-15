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

import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.MockitoSugar.{mock, when}

class UploadSupportingDocumentsControllerSpec extends SpecBase {

  private val mockAppConf = mock[FrontendAppConfig]

  "UploadSupportingDocuments Controller" - {
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        when(mockAppConf.host).thenReturn("any-url")

        val request = FakeRequest(
          GET,
          controllers.fileupload.routes.UploadSupportingDocumentsController
            .onPageLoad(None, None, None)
            .url
        )

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }
  }
}
