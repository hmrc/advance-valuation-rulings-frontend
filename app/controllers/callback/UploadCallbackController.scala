/*
 * Copyright 2025 HM Revenue & Customs
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

import cats.implicits._
import models.{DraftId, UploadedFile}
import play.api.mvc.{Action, MessagesControllerComponents}
import services.fileupload.FileService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UploadCallbackController @Inject() (
  override val controllerComponents: MessagesControllerComponents,
  fileService: FileService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {

  def callback(
    draftId: DraftId,
    isLetterOfAuthority: Boolean
  ): Action[UploadedFile] =
    Action.async(parse.json[UploadedFile]) { implicit request =>
      fileService.update(draftId, request.body, isLetterOfAuthority).as(Ok)
    }
}
