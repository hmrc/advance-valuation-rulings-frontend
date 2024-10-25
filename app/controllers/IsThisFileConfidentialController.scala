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

package controllers

import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import forms.IsThisFileConfidentialFormProvider
import models._
import navigation.Navigator
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.AllDocuments
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IsThisFileConfidentialView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsThisFileConfidentialController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: IsThisFileConfidentialFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IsThisFileConfidentialView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) { implicit request =>
      lazy val isSuccessful =
        UploadSupportingDocumentPage.get().exists(_.isSuccessful)

      if (isSuccessful) {
        val preparedForm = IsThisFileConfidentialPage.fill(form)
        val fileName     = UploadSupportingDocumentPage.get().get.fileName.get
        Ok(view(preparedForm, mode, draftId, fileName))
      } else {
        Redirect(
          routes.UploadSupportingDocumentsController
            .onPageLoad(mode, request.userAnswers.draftId, None, None)
        )
      }
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val fileName = UploadSupportingDocumentPage.get() match {
              case Some(file) =>
                file.fileName.get
              case None       =>
                ""
            }
            Future.successful(BadRequest(view(formWithErrors, mode, draftId, fileName)))
          },
          value =>
            UploadSupportingDocumentPage.get() match {
              case Some(file: UploadedFile.Success) =>
                val allDocuments = AllDocuments.get().getOrElse(List.empty[DraftAttachment])

                val draft     = DraftAttachment(file, Some(value))
                val documents = allDocuments :+ draft

                for {
                  ua <- AllDocuments.set(documents)
                  ua <- ua.removeFuture(UploadSupportingDocumentPage)
                  ua <- ua.removeFuture(IsThisFileConfidentialPage)
                  _  <- userAnswersService.set(ua)
                } yield Redirect(
                  navigator.nextPage(IsThisFileConfidentialPage, mode, ua)
                )

              case _ =>
                Future.successful(
                  Redirect(
                    routes.UploadSupportingDocumentsController
                      .onPageLoad(mode, request.userAnswers.draftId, None, None)
                  )
                )
            }
        )
    }
}
