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

import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat

import com.google.inject.Inject
import models.{DraftId, Mode, TraderDetailsWithCountryCode}
import models.requests.DataRequest

package userrole {

  import play.twirl.api.HtmlFormat

  import pages.{CheckRegisteredDetailsPage, Page}
  import views.html.{EmployeeCheckRegisteredDetailsView, EmployeeEORIBeUpToDateView}

  case class Employee @Inject() (
    view: EmployeeCheckRegisteredDetailsView,
    eoriBeUpToDateView: EmployeeEORIBeUpToDateView
  ) extends UserRole {
    override def selectViewForCheckRegisteredDetails(
      form: Form[Boolean],
      details: TraderDetailsWithCountryCode,
      mode: Mode,
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      view(
        form,
        details,
        mode,
        draftId
      )

    override def selectViewForEoriBeUpToDate(
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      eoriBeUpToDateView(draftId)

    override def selectGetRegisteredDetailsPage(): Page = CheckRegisteredDetailsPage
  }

}
