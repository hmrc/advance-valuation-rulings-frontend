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

package models

import base.SpecBase
import play.api.i18n.Lang
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Locale

class ApplicationForAccountHomeSpec extends SpecBase {

  private val applicationForAccountHome: ApplicationForAccountHome = ApplicationForAccountHome(
    id = "id",
    goodsDescription = "goodsDescription",
    date = Instant.now,
    statusTag = Tag(),
    actions = Seq.empty
  )

  "ApplicationForAccountHome" - {

    "must format the date correctly in English" in {

      val expectedFormattedDate: String = DateTimeFormatter
        .ofPattern("dd MMMM yyyy")
        .withZone(ZoneId.systemDefault())
        .format(applicationForAccountHome.date)

      applicationForAccountHome.dateString(Lang("en")) mustBe expectedFormattedDate
    }

    "must format the date correctly in Welsh" in {

      val expectedFormattedDate: String = DateTimeFormatter
        .ofPattern("dd MMMM yyyy")
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.forLanguageTag("cy-GB"))
        .format(applicationForAccountHome.date)

      applicationForAccountHome.dateString(Lang("cy")) mustBe expectedFormattedDate
    }
  }
}
