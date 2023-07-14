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

package userrole

import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat

import models.{CDSEstablishmentAddress, DraftId, NormalMode, TraderDetailsWithCountryCode}
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import views.html.EmployeeCheckRegisteredDetailsView

class UserRoleTest extends AnyFreeSpec with Matchers {

  val employeeCheckRegisteredDetailsView = mock[EmployeeCheckRegisteredDetailsView]

  val employee = Employee(employeeCheckRegisteredDetailsView)

  "Employee" - {
    "should return the correct view for CheckRegisteredDetails" in {
      val cDSEstablishmentAddress: CDSEstablishmentAddress = new CDSEstablishmentAddress(
        "",
        "",
        "",
        None
      )

      val mockReturnedView: HtmlFormat.Appendable = mock[HtmlFormat.Appendable]

      val traderDetailsWithCountryCode =
        TraderDetailsWithCountryCode("", true, "", cDSEstablishmentAddress, None)
      when(employeeCheckRegisteredDetailsView.apply(any(), any(), any(), any())(any(), any()))
        .thenReturn(mockReturnedView)

      val returnedView: HtmlFormat.Appendable = employee.selectViewForCheckRegisteredDetails(
        mock[Form[Boolean]],
        traderDetailsWithCountryCode,
        NormalMode,
        DraftId(1L)
      )(mock[DataRequest[AnyContent]], mock[Messages])

      returnedView mustBe mockReturnedView
    }
  }

}
