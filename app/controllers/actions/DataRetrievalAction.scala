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

import play.api.mvc.ActionTransformer
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import models.{AuthUserType, DraftId}
import models.requests.{IdentifierRequest, OptionalDataRequest}
import pages.AccountHomePage
import services.UserAnswersService

class DataRetrievalAction @Inject() (
  draftId: DraftId,
  val userAnswersService: UserAnswersService
)(implicit val executionContext: ExecutionContext)
    extends ActionTransformer[IdentifierRequest, OptionalDataRequest] {

  override protected def transform[A](
    request: IdentifierRequest[A]
  ): Future[OptionalDataRequest[A]] = {

    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    AuthUserType(request) match {
      case None               => throw InsufficientEnrolments("Auth user type could not be created from request")
      case Some(authUserType) =>
        for {
          maybeUserAnswers       <- userAnswersService.get(draftId)(hc)
          userAnswersWithAuthType =
            maybeUserAnswers.flatMap(_.set(AccountHomePage, authUserType).toOption)
        } yield OptionalDataRequest(
          request.request,
          request.userId,
          request.eoriNumber,
          request.affinityGroup,
          request.credentialRole,
          userAnswersWithAuthType
        )
    }
  }
}

class DataRetrievalActionProvider @Inject() (userAnswersService: UserAnswersService)(implicit
  ec: ExecutionContext
) {

  def apply(draftId: DraftId): ActionTransformer[IdentifierRequest, OptionalDataRequest] =
    new DataRetrievalAction(draftId, userAnswersService)
}
