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
import models.{CounterId, DraftId}
import models.requests.{DraftIdRequest, OptionalDataRequest}
import repositories.CounterRepository

@Singleton
class DraftIdGenerationActionImpl @Inject() (
  val draftIdRepository: CounterRepository
)(implicit val executionContext: ExecutionContext)
    extends DraftIdGenerationAction {

  override protected def transform[A](
    request: OptionalDataRequest[A]
  ): Future[DraftIdRequest[A]] =
    draftIdRepository.nextId(CounterId.DraftId).map {
      draftId =>
        val updatedRequest =
          request.modify(_.userAnswers.each.draftId).setTo(DraftId(draftId).toString)

        DraftIdRequest(
          updatedRequest,
          request.userId,
          request.eoriNumber,
          DraftId(draftId),
          request.affinityGroup,
          updatedRequest.userAnswers
        )
    }
}

@ImplementedBy(classOf[DraftIdGenerationActionImpl])
trait DraftIdGenerationAction extends ActionTransformer[OptionalDataRequest, DraftIdRequest]
