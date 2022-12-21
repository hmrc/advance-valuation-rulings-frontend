package controllers.actions

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.mvc._

import models.requests.IdentifierRequest

class FakeIdentifierAction @Inject() (bodyParsers: PlayBodyParsers) extends IdentifierAction {

  override def invokeBlock[A](
    request: Request[A],
    block: IdentifierRequest[A] => Future[Result]
  ): Future[Result] =
    block(IdentifierRequest(request, "id"))

  override def parser: BodyParser[AnyContent] =
    bodyParsers.default

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
