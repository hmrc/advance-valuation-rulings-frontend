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

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc.ActionTransformer

import models.{DraftId, UserAnswers}
import models.requests.{DataRequest, IdentifierRequest}
import org.mockito.MockitoSugar.mock
import services.UserAnswersService

class FakeDataRetrievalActionProvider(dataToReturn: UserAnswers)
    extends DataRetrievalActionProvider(mock[UserAnswersService]) {

  override def apply(draftId: DraftId): ActionTransformer[IdentifierRequest, DataRequest] =
    new FakeDataRetrievalAction(dataToReturn)
}

class FakeDataRetrievalAction(dataToReturn: UserAnswers)
    extends DataRetrievalAction(DraftId(0), mock[UserAnswersService]) {

  override protected def transform[A](
    request: IdentifierRequest[A]
  ): Future[DataRequest[A]] =
    Future(
      DataRequest(
        request.request,
        request.userId,
        request.eoriNumber,
        dataToReturn,
        request.affinityGroup,
        request.credentialRole
      )
    )(executionContext)

  override implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
