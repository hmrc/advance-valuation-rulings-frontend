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

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import forms.IsThisFileConfidentialFormProvider
import models.{IsThisFileConfidential, Mode}
import models.fileupload.FileConfidentiality
import navigation.Navigator
import pages.{IsThisFileConfidentialPage, UploadSupportingDocumentPage}
import repositories.SessionRepository
import views.html.IsThisFileConfidentialView

class IsThisFileConfidentialController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: IsThisFileConfidentialFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IsThisFileConfidentialView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val userAnswers = request.userAnswers

        val isConfidentialAndId = for {
          uploads           <- userAnswers.get(UploadSupportingDocumentPage)
          fileInQuestion    <- uploads.lastUploadId
          confidentialityMap = userAnswers.get(IsThisFileConfidentialPage)
        } yield (fileInQuestion, confidentialityMap.flatMap(_.files.get(fileInQuestion)))

        isConfidentialAndId match {
          case Some((uploadId, isConfidential)) =>
            val formData: Seq[(String, String)] =
              isConfidential.toSeq.map(value => ("value" -> value.toString)) ++ Seq(
                ("uploadId" -> uploadId.value)
              )
            val preparedForm                    = form.bind(Map.from(formData)).discardingErrors
            Ok(view(preparedForm, mode))
          case None                             => // could send them to 'uploaded files'
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            (value: IsThisFileConfidential) =>
              for {
                updatedAnswers <-
                  request.userAnswers.upsertFuture(
                    IsThisFileConfidentialPage,
                    (files: FileConfidentiality) => files.setConfidentiality(value),
                    FileConfidentiality(value)
                  )
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(IsThisFileConfidentialPage, mode, updatedAnswers))
          )

    }
}
