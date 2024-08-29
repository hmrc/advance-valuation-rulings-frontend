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

package viewmodels.checkAnswers

import base.SpecBase
import generators.ApplicationGenerator
import models.requests.Application
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessagesApi
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateSubmittedSummarySpec extends SpecBase with ApplicationGenerator {

  ".row" - {

    Seq(
      ("en", Locale.forLanguageTag("en")),
      ("cy", Locale.getDefault)
    ).foreach { case (lang, locale) =>
      s"must create row for DateSubmittedSummary and format the date using the appropriate locale when the language is $lang" in {

        implicit val messages: Messages = stubMessagesApi().preferred(Seq(Lang(lang)))

        val application: Application = arbitraryApplication.arbitrary.sample.value

        val expectedFormattedDate: String = DateTimeFormatter
          .ofPattern("dd MMMM yyyy")
          .withZone(ZoneId.systemDefault())
          .withLocale(locale)
          .format(application.created)

        DateSubmittedSummary.row(application) mustBe SummaryListRowViewModel(
          key = "viewApplication.dateSubmitted",
          value = ValueViewModel(expectedFormattedDate)
        )
      }
    }
  }
}
