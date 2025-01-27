/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.common

import connectors.BackendConnector
import controllers.routes
import models.requests.DataRequest
import models.{AcknowledgementReference, EoriNumber, TraderDetailsWithCountryCode}
import play.api.Logger
import play.api.http.Status.NOT_FOUND
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait TraderDetailsHelper {

  def getTraderDetails(
    handleSuccess: TraderDetailsWithCountryCode => Future[Result],
    notFound: Option[Future[Result]] = None,
    eori: Option[EoriNumber] = None
  )(implicit
    request: DataRequest[AnyContent],
    backendConnector: BackendConnector,
    hc: HeaderCarrier,
    ec: ExecutionContext,
    logger: Logger
  ): Future[Result] =
    backendConnector
      .getTraderDetails(
        AcknowledgementReference(request.userAnswers.draftId),
        eori.getOrElse(EoriNumber(request.eoriNumber))
      )
      .flatMap { r =>
        (r, notFound) match {
          case (Right(traderDetails), _)                             =>
            handleSuccess(traderDetails)
          case (Left(error), Some(nfRes)) if error.code == NOT_FOUND =>
            logger.warn(
              s"[TraderDetailsHelper][getTraderDetails] Trader details not found. Error: $error"
            )
            nfRes
          case (Left(error), _)                                      =>
            logger.error(
              s"[TraderDetailsHelper][getTraderDetails] Failed to get trader details from backend: $error"
            )
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }
      }
}
