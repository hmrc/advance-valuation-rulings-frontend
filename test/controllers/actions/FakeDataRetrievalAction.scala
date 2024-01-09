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

package controllers.actions

import models.requests.{IdentifierRequest, OptionalDataRequest}
import models.{DraftId, UserAnswers}
import org.mockito.MockitoSugar.mock
import play.api.mvc.ActionTransformer
import services.UserAnswersService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalActionProvider(dataToReturn: Option[UserAnswers])
    extends DataRetrievalActionProvider(mock[UserAnswersService]) {

  override def apply(draftId: DraftId): ActionTransformer[IdentifierRequest, OptionalDataRequest] =
    new FakeDataRetrievalAction(dataToReturn)
}

class FakeDataRetrievalAction(dataToReturn: Option[UserAnswers])
    extends DataRetrievalAction(DraftId(0), mock[UserAnswersService]) {

  override protected def transform[A](
    request: IdentifierRequest[A]
  ): Future[OptionalDataRequest[A]] =
    Future(
      OptionalDataRequest(
        request.request,
        request.userId,
        request.eoriNumber,
        request.affinityGroup,
        request.credentialRole,
        dataToReturn
      )
    )(executionContext)

  override implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
