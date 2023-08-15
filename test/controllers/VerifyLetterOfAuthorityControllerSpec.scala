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

import java.time.Instant

import play.api.Configuration
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import forms.UploadAnotherSupportingDocumentFormProvider
import models._
import navigation.{FakeNavigator, Navigator}
import org.scalatestplus.mockito.MockitoSugar
import queries.DraftAttachmentAt
import views.html.UploadAnotherSupportingDocumentView

class VerifyLetterOfAuthorityControllerSpec extends SpecBase with MockitoSugar {

  "VerifyLetterOfAuthority Controller" - {

    "must return OK and the correct view for page load" in {
      fail() //TODO.
    }

    "must redirect to the next page when valid data is submitted" in {
      fail() //TODO.
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      fail() //TODO.
    }

    "must redirect to Journey Recovery if no document is found during page load" in {
      fail() //TODO.
    }

    "must redirect to Journey Recovery if no document is found during page submit" in {
      fail() //TODO.
    }
  }
}
