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
import play.api.mvc.Call
import play.api.test.FakeRequest

import config.{InternalAuthTokenInitialiser, NoOpInternalAuthTokenInitialiser}
import controllers.actions._
import models.{CDSEstablishmentAddress, ContactInformation, CounterId, Done, DraftId, TraderDetailsWithCountryCode, UserAnswers}
import models.AuthUserType.{IndividualTrader, OrganisationAdmin, OrganisationAssistant}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.AccountHomePage
import repositories.CounterRepository
import services.UserAnswersService

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach {

  val ContactName        = "some name"
  val ContactEmail       = "test@email.com"
  val ContactPhoneNumber = "01234567890"

  val EoriNumber      = "GB123456789000"
  val RegisteredName  = "My Test Company"
  val StreetAndNumber = "1 Somewhere"
  val City            = "London"
  val countryCode     = "GB"
  val country         = "United Kingdom"
  val Postcode        = "A12 2AB"
  val phoneNumber     = "01234567890"

  val userAnswersId: String = "id"
  val DraftIdSequence       = 123456789L
  val draftId               = DraftId(DraftIdSequence)

  val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, draftId)

  val userAnswersAsIndividualTrader: UserAnswers =
    emptyUserAnswers
      .setFuture(AccountHomePage, IndividualTrader)
      .futureValue

  val userAnswersAsOrgAdmin: UserAnswers =
    emptyUserAnswers
      .setFuture(AccountHomePage, OrganisationAdmin)
      .futureValue

  emptyUserAnswers
    .setFuture(AccountHomePage, OrganisationAdmin)
    .futureValue

  val userAnswersAsOrgAssistant: UserAnswers = emptyUserAnswers
    .setFuture(AccountHomePage, OrganisationAssistant)
    .futureValue

  val contactInformation = ContactInformation(
    personOfContact = Some("Test Person"),
    sepCorrAddrIndicator = Some(false),
    streetAndNumber = Some("Test Street 1"),
    city = Some("Test City"),
    postalCode = Some("Test Postal Code"),
    countryCode = Some("GB"),
    telephoneNumber = Some("Test Telephone Number"),
    faxNumber = Some("Test Fax Number"),
    emailAddress = Some("Test Email Address"),
    emailVerificationTimestamp = Some("2000-01-31T23:59:59Z")
  )

  val traderDetailsWithCountryCode = TraderDetailsWithCountryCode(
    EORINo = "GB123456789012345",
    consentToDisclosureOfPersonalData = true,
    CDSFullName = "Test Name",
    CDSEstablishmentAddress = CDSEstablishmentAddress(
      streetAndNumber = "Test Street 1",
      city = "Test City",
      countryCode = "GB",
      postalCode = Some("Test Postal Code")
    ),
    contactInformation = Some(contactInformation)
  )

  def messages(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val mockDraftIdRepo: CounterRepository =
    mock[CounterRepository]

  when(mockDraftIdRepo.nextId(eqTo(CounterId.DraftId))) thenReturn Future.successful(
    DraftIdSequence
  )

  protected def applicationBuilder(
    userAnswers: Option[UserAnswers] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[IdentifyIndividualAction].to[FakeIdentifyIndividualAction],
        bind[DataRetrievalActionProvider].toInstance(
          new FakeDataRetrievalActionProvider(userAnswers)
        ),
        bind[CounterRepository].to(mockDraftIdRepo),
        bind[InternalAuthTokenInitialiser].to[NoOpInternalAuthTokenInitialiser]
      )
  protected def applicationBuilderAsAgent(
    userAnswers: Option[UserAnswers] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeAgentIdentifierAction],
        bind[IdentifyAgentAction].to[FakeIdentifyAgentAction],
        bind[DataRetrievalActionProvider].toInstance(
          new FakeDataRetrievalActionProvider(userAnswers)
        ),
        bind[CounterRepository].to(mockDraftIdRepo),
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
        bind[DataRetrievalActionProvider].toInstance(
          new FakeDataRetrievalActionProvider(userAnswers)
        ),
        bind[CounterRepository].to(mockDraftIdRepo),
        bind[InternalAuthTokenInitialiser].to[NoOpInternalAuthTokenInitialiser]
      )

  def setupTestBuild(userAnswers: UserAnswers) = {
    val mockUserAnswersService = mock[UserAnswersService]

    when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)
    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(mockUserAnswersService)
      )
      .build()

  }
  def onwardRoute = Call("GET", "/foo")

}
