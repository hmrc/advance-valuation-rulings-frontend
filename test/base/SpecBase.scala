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

package base

import scala.concurrent.Future

import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest

import config.{InternalAuthTokenInitialiser, NoOpInternalAuthTokenInitialiser}
import controllers.actions._
import models.{ApplicationNumber, UserAnswers}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import repositories.ApplicationNumberRepository
import services.FakeFileUploadService
import services.fileupload.FileUploadService

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val userAnswersId: String     = "id"
  val ApplicationNumberPrefix   = "GBAVR"
  val ApplicationNumberSequence = 123456789
  val applicationNumber: String = s"$ApplicationNumberPrefix$ApplicationNumberSequence"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, applicationNumber)

  def messages(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val mockApplicationNumberRepo: ApplicationNumberRepository =
    mock[ApplicationNumberRepository]

  when(mockApplicationNumberRepo.generate(ApplicationNumberPrefix)) thenReturn Future.successful(
    ApplicationNumber(ApplicationNumberPrefix, ApplicationNumberSequence)
  )

  protected def applicationBuilder(
    userAnswers: Option[UserAnswers] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[IdentifyIndividualAction].to[FakeIdentifyIndividualAction],
        bind[FileUploadService].to[FakeFileUploadService],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[ApplicationNumberRepository].to(mockApplicationNumberRepo),
        bind[InternalAuthTokenInitialiser].to[NoOpInternalAuthTokenInitialiser]
      )
  protected def applicationBuilderAsAgent(
    userAnswers: Option[UserAnswers] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[IdentifyAgentAction].to[FakeIdentifyAgentAction],
        bind[FileUploadService].to[FakeFileUploadService],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[ApplicationNumberRepository].to(mockApplicationNumberRepo),
        bind[InternalAuthTokenInitialiser].to[NoOpInternalAuthTokenInitialiser]
      )
}
