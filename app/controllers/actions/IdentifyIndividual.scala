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

import play.api.mvc._
import play.api.mvc.Results._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.IdentifyEori.EnrolmentKey
import controllers.routes
import models.requests.IdentifierRequest

class IdentifyIndividual @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions {

  def redirectToEoriComponent: Result =
    Redirect(config.arsSubscribeUrl)

  private def authorise(): AuthorisedFunction =
    authorised(Enrolment(EnrolmentKey) and AuthProviders(GovernmentGateway))

  override def invokeBlock[A](
    request: Request[A],
    block: IdentifierRequest[A] => Future[Result]
  ): Future[Result] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorise()
      .retrieve(
        Retrievals.internalId and Retrievals.authorisedEnrolments and Retrievals.affinityGroup
      ) {
        case Some(internalId) ~ allEnrolments ~ Some(Individual) =>
          IdentifyEori
            .getEoriNumber(allEnrolments)
            .map(eori => block(IdentifierRequest(request, internalId, eori, Individual)))
            .getOrElse(throw InsufficientEnrolments())

        case Some(_) ~ _ ~ Some(_) =>
          throw UnsupportedAffinityGroup("User has wrong affinity group")

        case _ =>
          throw new UnauthorizedException("Unable to retrieve internal Id")
      }
      .recover {
        case _: NoActiveSession        =>
          Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
        case _: InsufficientEnrolments =>
          redirectToEoriComponent
        case _: AuthorisationException =>
          Redirect(routes.UnauthorisedController.onPageLoad)
      }
  }
}