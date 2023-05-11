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
import play.api.mvc.Results.Redirect

import controllers.routes.UnauthorisedController
import models.requests.DataRequest
import pages.AccountHomePage

class DataRequiredActionImpl @Inject() (implicit val executionContext: ExecutionContext)
    extends DataRequiredAction {

  override protected def refine[A](
    request: DataRequest[A]
  ): Future[Either[Result, DataRequest[A]]] = {
    val result =
      Either.cond(
        request.userAnswers.get(AccountHomePage).nonEmpty,
        DataRequest(
          request.request,
          request.userId,
          request.eoriNumber,
          request.userAnswers,
          request.affinityGroup,
          request.credentialRole
        ),
        Redirect(UnauthorisedController.onPageLoad)
      )

    Future.successful(result)
  }
}

trait DataRequiredAction extends ActionRefiner[DataRequest, DataRequest]
