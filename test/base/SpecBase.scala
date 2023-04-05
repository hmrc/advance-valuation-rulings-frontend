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
import models.{DraftId, UserAnswers}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import repositories.DraftIdRepository
import services.FakeFileUploadService
import services.fileupload.FileUploadService

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val ContactName        = "some name"
  val ContactEmail       = "test@email.com"
  val ContactPhoneNumber = "01234567890"

  val EoriNumber      = "GB123456789000"
  val RegisteredName  = "My Test Company"
  val StreetAndNumber = "1 Somewhere"
  val City            = "London"
  val Country         = "United Kingdom"
  val Postcode        = "A12 2AB"
  val phoneNumber     = "01234567890"

  val userAnswersId: String = "id"
  val DraftIdPrefix         = "DRAFT"
  val DraftIdSequence       = 123456789
  val draftId: String       = s"$DraftIdPrefix$DraftIdSequence"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, draftId)

  def messages(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val mockDraftIdRepo: DraftIdRepository =
    mock[DraftIdRepository]

  when(mockDraftIdRepo.generate(DraftIdPrefix)) thenReturn Future.successful(
    DraftId(DraftIdPrefix, DraftIdSequence)
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
        bind[DraftIdRepository].to(mockDraftIdRepo),
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
        bind[DraftIdRepository].to(mockDraftIdRepo),
        bind[InternalAuthTokenInitialiser].to[NoOpInternalAuthTokenInitialiser]
      )
  protected def applicationBuilderAsOrg(
    userAnswers: Option[UserAnswers] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeOrgIdentifierAction],
        bind[IdentifyAgentAction].to[FakeIdentifyOrgAction],
        bind[FileUploadService].to[FakeFileUploadService],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[DraftIdRepository].to(mockDraftIdRepo),
        bind[InternalAuthTokenInitialiser].to[NoOpInternalAuthTokenInitialiser]
      )
}
