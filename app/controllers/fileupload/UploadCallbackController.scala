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

package controllers.fileupload

import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext

import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import models.fileupload.CallbackBody
import services.fileupload.UpscanCallbackDispatcher

@Singleton
class UploadCallbackController @Inject() (
  upscanCallbackDispatcher: UpscanCallbackDispatcher,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) {

  val callback = Action.async(parse.json) {
    implicit request =>
      withJsonBody[CallbackBody] {
        feedback: CallbackBody => upscanCallbackDispatcher.handleCallback(feedback).map(_ => Ok)
      }
  }
}
