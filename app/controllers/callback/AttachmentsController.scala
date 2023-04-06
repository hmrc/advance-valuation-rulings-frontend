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

package controllers.callback

import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.Implicits._
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

@Singleton
class AttachmentsController @Inject() (
  override val controllerComponents: MessagesControllerComponents,
  objectStoreClient: PlayObjectStoreClient
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {

  override implicit protected def hc(implicit request: RequestHeader): HeaderCarrier =
    HeaderCarrierConverter.fromRequest(request)

  def get(path: String): Action[AnyContent] = Action.async {
    implicit request =>
      objectStoreClient.getObject(Path.File(path)).map {
        o => o.map(o => Ok.chunked(o.content)).getOrElse(NotFound)
      }
  }
}
