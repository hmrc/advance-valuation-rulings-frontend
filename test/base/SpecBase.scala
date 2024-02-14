/*
 * Copyright 2024 HM Revenue & Customs
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

import config.{FrontendAppConfig, InternalAuthTokenInitialiser, NoOpInternalAuthTokenInitialiser}
import controllers.actions._
import handlers.ErrorHandler
import models.AuthUserType.{IndividualTrader, OrganisationAssistant, OrganisationUser}
import models._
import models.upscan.UpscanInitiateResponse
import navigation.FakeNavigators.FakeNavigator
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, EitherValues, OptionValues, TryValues}
import pages.{AccountHomePage, WhatIsYourRoleAsImporterPage}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.{Injector, bind}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest
import repositories.CounterRepository
import services.UserAnswersService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.client.HttpClientV2
import viewmodels.checkAnswers.summary._

import scala.concurrent.Future

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with EitherValues
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach {

  val ContactName        = "some name"
  val ContactEmail       = "test@email.com"
  val ContactPhoneNumber = "01234567890"
  val JobTitle           = "CEO"

  val EoriNumber      = "GB123456789000"
  val RegisteredName  = "My Test Company"
  val StreetAndNumber = "1 Somewhere"
  val City            = "London"
  val countryCode     = "GB"
  val countryAsString = "United Kingdom"
  val Postcode        = "A12 2AB"
  val phoneNumber     = "01234567890"

  val userAnswersId: String = "id"
  val DraftIdSequence: Long = 123456789L
  val draftId: DraftId      = DraftId(DraftIdSequence)

  val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, draftId)

  val userAnswersAsIndividualTrader: UserAnswers =
    emptyUserAnswers
      .setFuture(AccountHomePage, IndividualTrader)
      .futureValue

  val userAnswersAsOrgUser: UserAnswers =
    emptyUserAnswers
      .setFuture(AccountHomePage, OrganisationUser)
      .futureValue

  val userAnswersAsOrgAssistant: UserAnswers = emptyUserAnswers
    .setFuture(AccountHomePage, OrganisationAssistant)
    .futureValue

  def userAnswersForRole(role: WhatIsYourRoleAsImporter): UserAnswers = emptyUserAnswers
    .setFuture(WhatIsYourRoleAsImporterPage, role)
    .futureValue

  val contactInformation: ContactInformation = ContactInformation(
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

  val traderDetailsWithCountryCode: TraderDetailsWithCountryCode = TraderDetailsWithCountryCode(
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

  val traderDetailsWithConfirmation: TraderDetailsWithConfirmation =
    TraderDetailsWithConfirmation(traderDetailsWithCountryCode)

  val testEoriDetailsSummary: IndividualEoriDetailsSummary = IndividualEoriDetailsSummary(SummaryList())
  val testApplicantSummary: IndividualApplicantSummary     = IndividualApplicantSummary(SummaryList())
  val testDetailsSummary: DetailsSummary                   = DetailsSummary(SummaryList())
  val testMethodSummary: MethodSummary                     = MethodSummary(SummaryList())

  val testApplicationSummary: ApplicationSummary =
    ApplicationSummary(testEoriDetailsSummary, testApplicantSummary, testDetailsSummary, testMethodSummary)

  val upscanInitiateResponse: UpscanInitiateResponse = UpscanInitiateResponse(
    reference = "reference",
    uploadRequest = UpscanInitiateResponse.UploadRequest(
      href = "href",
      fields = Map(
        "field1" -> "value1",
        "field2" -> "value2"
      )
    )
  )

  def messagesApi(app: Application): MessagesApi =
    app.injector.instanceOf[MessagesApi]

  def messages(app: Application): Messages =
    messagesApi(app).preferred(FakeRequest())

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
        bind[DataRetrievalActionProvider].toInstance(
          new FakeDataRetrievalActionProvider(userAnswers)
        ),
        bind[CounterRepository].to(mockDraftIdRepo),
        bind[InternalAuthTokenInitialiser].to[NoOpInternalAuthTokenInitialiser]
      )

  def setupTestBuild(userAnswers: UserAnswers): Application = {
    val mockUserAnswersService = mock[UserAnswersService]

    when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)
    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(mockUserAnswersService)
      )
      .build()

  }

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val injector: Injector = applicationBuilder().injector

  implicit lazy val frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  lazy val httpV2: HttpClientV2 = injector.instanceOf[HttpClientV2]

  lazy val messagesControllerComponents: MessagesControllerComponents =
    injector.instanceOf[MessagesControllerComponents]

  lazy val playBodyParsers: PlayBodyParsers =
    injector.instanceOf[MessagesControllerComponents].parsers

  implicit lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def messagesHelper(fakeRequest: FakeRequest[AnyContentAsEmpty.type]): Messages =
    messagesApi.preferred(fakeRequest)

  implicit lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  lazy val dataRequiredAction: DataRequiredActionImpl = injector.instanceOf[DataRequiredActionImpl]
}
