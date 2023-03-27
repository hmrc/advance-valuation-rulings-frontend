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

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import play.api.mvc.ActionTransformer

import com.google.inject.ImplementedBy
import com.softwaremill.quicklens._
import models.requests.{ApplicationNumberRequest, OptionalDataRequest}
import repositories.ApplicationNumberRepository

@Singleton
class ApplicationNumberGenerationActionImpl @Inject() (
  val applicationNumberRepository: ApplicationNumberRepository
)(implicit val executionContext: ExecutionContext)
    extends ApplicationNumberGenerationAction {

  override protected def transform[A](
    request: OptionalDataRequest[A]
  ): Future[ApplicationNumberRequest[A]] =
    applicationNumberRepository.generate("GBAVR").map {
      applicationNumber =>
        val updatedRequest =
          request.modify(_.userAnswers.each.applicationNumber).setTo(applicationNumber.render)

        ApplicationNumberRequest(
          updatedRequest,
          request.userId,
          request.eoriNumber,
          applicationNumber,
          request.affinityGroup,
          updatedRequest.userAnswers
        )
    }
}

@ImplementedBy(classOf[ApplicationNumberGenerationActionImpl])
trait ApplicationNumberGenerationAction
    extends ActionTransformer[OptionalDataRequest, ApplicationNumberRequest]
