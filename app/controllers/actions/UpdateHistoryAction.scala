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

package controllers.actions

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import models.requests.DataRequest
import pages.QuestionPage
import queries.LastQuestionViewed
import services.UserAnswersService

class UpdateHistoryAction(
  page: QuestionPage[_],
  val userAnswersService: UserAnswersService
)(implicit val executionContext: ExecutionContext)
    extends HistoryRefiner {

  override def refine[A](
    request: DataRequest[A]
  ): Future[Either[Result, DataRequest[A]]] = {
    implicit val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    for {
      userAnswers <- request.userAnswers.setFuture(LastQuestionViewed, page)
      _           <- userAnswersService.set(userAnswers)
      updated      = request.copy(userAnswers = userAnswers)
    } yield (Right(updated))
  }
}

trait HistoryRefiner extends ActionRefiner[DataRequest, DataRequest]

class UserAnswersHistoryActionProvider @Inject() (userAnswersService: UserAnswersService)(implicit
  val executionContext: ExecutionContext
) {
  def apply(page: QuestionPage[_]): UpdateHistoryAction =
    new UpdateHistoryAction(page, userAnswersService)
}
