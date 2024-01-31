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

import controllers.ViewApplicationControllerSpec.{applicationId, applicationRequest}
import models.requests._
import play.twirl.api.HtmlFormat
import viewmodels.ApplicationViewModel
import views.behaviours.ViewBehaviours
import views.html.ViewApplicationView

import java.time.{Clock, Instant, ZoneOffset}
import scala.collection.immutable.List.from

class ViewApplicationViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "viewApplication"

  val ruling: Application =
    Application(
      id = applicationId,
      lastUpdated = Instant.now(Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC)),
      created = Instant.now(Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC)),
      trader = applicationRequest.trader,
      agent = applicationRequest.agent,
      contact = applicationRequest.contact,
      requestedMethod = applicationRequest.requestedMethod,
      goodsDetails = applicationRequest.goodsDetails,
      attachments = from(Nil),
      whatIsYourRoleResponse = Some(WhatIsYourRole.AgentTrader),
      letterOfAuthority = Some(
        Attachment(0x4L, "bob", None, "the location", Privacy.Public, "application/jpg", 4532L)
      )
    )

  val view: ViewApplicationView = app.injector.instanceOf[ViewApplicationView]

  val viewViaApply: HtmlFormat.Appendable  =
    view(ApplicationViewModel(ruling), ApplicationId(1L).toString, Instant.now.toString)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(ApplicationViewModel(ruling), ApplicationId(1L).toString, Instant.now.toString, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      =
    view.f(ApplicationViewModel(ruling), ApplicationId(1L).toString, Instant.now.toString)(fakeRequest, messages)

  "ViewApplicationView" - {
    normalPage(messageKeyPrefix, "")()
  }
}
