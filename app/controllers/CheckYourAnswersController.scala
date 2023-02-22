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

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.ValuationMethod
import pages.ValuationMethodPage
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView
import pages.DescriptionOfGoodsPage

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val answers          = request.userAnswers
      val applicantSummary = ApplicantSummary(answers)

      val valuationMethod = ValuationMethodSummary.row(answers).toSeq

      val methodAnswers = answers.get(ValuationMethodPage) match {
        case Some(ValuationMethod.Method1) =>
          Seq(
            IsThereASaleInvolvedSummary.row(answers),
            IsSaleBetweenRelatedPartiesSummary.row(answers),
            AreThereRestrictionsOnTheGoodsSummary.row(answers),
            DescribeTheRestrictionsSummary.row(answers),
            IsTheSaleSubjectToConditionsSummary.row(answers),
            DescribeTheConditionsSummary.row(answers)
          ).flatten
        case _                             => Seq()
      }

      val wrapUpAnswers = Seq(
        DescriptionOfGoodsSummary.row(answers),
        HasCommodityCodeSummary.row(answers),
        CommodityCodeSummary.row(answers),
        HaveTheGoodsBeenSubjectToLegalChallengesSummary.row(answers),
        DescribeTheLegalChallengesSummary.row(answers),
        HasConfidentialInformationSummary.row(answers),
        ConfidentialInformationSummary.row(answers),
        DoYouWantToUploadDocumentsSummary.row(answers)
      ).flatten

      val goodsSummary = SummaryListViewModel(
        rows = (valuationMethod ++ methodAnswers ++ wrapUpAnswers)
      )
      Ok(view(applicantSummary.rows, goodsSummary))
  }
}
