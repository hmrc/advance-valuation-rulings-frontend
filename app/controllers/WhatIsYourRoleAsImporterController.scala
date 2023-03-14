package controllers

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import forms.WhatIsYourRoleAsImporterFormProvider
import models.Mode
import navigation.Navigator
import pages.WhatIsYourRoleAsImporterPage
import repositories.SessionRepository
import views.html.WhatIsYourRoleAsImporterView

class WhatIsYourRoleAsImporterController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhatIsYourRoleAsImporterFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatIsYourRoleAsImporterView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(WhatIsYourRoleAsImporterPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsYourRoleAsImporterPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(WhatIsYourRoleAsImporterPage, mode, updatedAnswers))
          )
    }
}
