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

package viewmodels.application

import models.requests.ContactDetails
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}

class ContactDetailsSummarySpec extends AnyFreeSpec with Matchers {

  private implicit val m: Messages = stubMessages()

  ".rows" - {

    "must contain rows for name, email and phone when a phone number is present" in {

      val contact = ContactDetails(
        "name",
        "email",
        Some("phone")
      )

      ContactDetailsSummary.rows(contact) must contain theSameElementsInOrderAs Seq(
        SummaryListRow(
          Key(Text(m("checkYourAnswers.applicant.name.label"))),
          Value(Text(contact.name))
        ),
        SummaryListRow(
          Key(Text(m("checkYourAnswers.applicant.email.label"))),
          Value(Text(contact.email))
        ),
        SummaryListRow(
          Key(Text(m("checkYourAnswers.applicant.phone.label"))),
          Value(Text(contact.phone.get))
        )
      )
    }

    "must contain rows for name and email when no phone number is present" in {

      val contact = ContactDetails(
        "name",
        "email",
        None
      )

      ContactDetailsSummary.rows(contact) must contain theSameElementsInOrderAs  Seq(
        SummaryListRow(
          Key(Text(m("checkYourAnswers.applicant.name.label"))),
          Value(Text(contact.name))
        ),
        SummaryListRow(
          Key(Text(m("checkYourAnswers.applicant.email.label"))),
          Value(Text(contact.email))
        )
      )
    }
  }
}
