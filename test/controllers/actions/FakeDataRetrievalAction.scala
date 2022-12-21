package controllers.actions

import scala.concurrent.{ExecutionContext, Future}

import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}

class FakeDataRetrievalAction(dataToReturn: Option[UserAnswers]) extends DataRetrievalAction {

  override protected def transform[A](
    request: IdentifierRequest[A]
  ): Future[OptionalDataRequest[A]] =
    Future(OptionalDataRequest(request.request, request.userId, dataToReturn))

  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
