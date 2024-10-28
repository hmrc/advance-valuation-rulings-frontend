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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.AuthActionSpec.AuthRetrievals
import controllers.routes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.Application
import play.api.mvc.{Action, AnyContent, BodyParsers, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  private val application: Application         = applicationBuilder(userAnswers = None).build()
  private val bodyParsers: BodyParsers.Default = application.injector.instanceOf[BodyParsers.Default]
  private val appConfig: FrontendAppConfig     = application.injector.instanceOf[FrontendAppConfig]

  private val mockAuthConnector: AuthConnector = mock(classOf[AuthConnector])

  private def enrolments(enrolmentKey: String): Enrolments = Enrolments(
    Set(
      Enrolment(
        key = enrolmentKey,
        identifiers = Seq(
          EnrolmentIdentifier("EORINumber", "GB12345678")
        ),
        state = "activated"
      )
    )
  )
  private val affinityGroup: AffinityGroup                 = Individual
  private val credentialRole: CredentialRole               = User

  private class LoginSetup(
    internalId: Option[String] = Some("internalId"),
    enrolmentKey: String = "HMRC-ATAR-ORG"
  ) {

    private type RetrievalType = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole]

    when(mockAuthConnector.authorise[RetrievalType](any(), any())(any(), any()))
      .thenReturn(
        Future.successful(
          internalId `composeRetrievals` enrolments(enrolmentKey) `composeRetrievals`
            Some(affinityGroup) `composeRetrievals` Some(credentialRole)
        )
      )
  }

  private class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

  "Auth Action" - {

    "when the user has logged in with valid credentials" - {

      "must return OK" in new LoginSetup() {

        val authAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          mockAuthConnector,
          appConfig,
          bodyParsers
        )
        val controller: Harness                       = new Harness(authAction)
        val result: Result                            = controller.onPageLoad()(FakeRequest()).futureValue

        result.header.status mustBe OK
      }
    }

    "when the user tries to logged in with an invalid enrolment key" - {

      "must redirect the user to eori common component" in new LoginSetup(enrolmentKey = "ATAR") {

        val authAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          mockAuthConnector,
          appConfig,
          bodyParsers
        )
        val controller: Harness                       = new Harness(authAction)
        val result: Result                            = controller.onPageLoad()(FakeRequest()).futureValue

        result.header.status mustBe SEE_OTHER
        result.header.headers(LOCATION) mustBe appConfig.arsSubscribeUrl
      }
    }

    "when the user tries to logged in without an internal ID" - {

      "must throw UnauthorizedException" in new LoginSetup(internalId = None) {

        val authAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          mockAuthConnector,
          appConfig,
          bodyParsers
        )
        val controller: Harness                       = new Harness(authAction)
        val result: Throwable                         = controller.onPageLoad()(FakeRequest()).failed.futureValue

        result.getMessage mustBe "Unable to retrieve internal Id"
        result mustBe an[UnauthorizedException]
      }
    }

    "when the user hasn't logged in" - {

      "must redirect the user to log in" in {

        val authAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new MissingBearerToken),
          appConfig,
          bodyParsers
        )
        val controller: Harness                       = new Harness(authAction)
        val result: Result                            = controller.onPageLoad()(FakeRequest()).futureValue

        result.header.status mustBe SEE_OTHER
        result.header.headers(LOCATION) must startWith(appConfig.loginUrl)
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in" in {

        val authAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new BearerTokenExpired),
          appConfig,
          bodyParsers
        )
        val controller: Harness                       = new Harness(authAction)
        val result: Result                            = controller.onPageLoad()(FakeRequest()).futureValue

        result.header.status mustBe SEE_OTHER
        result.header.headers(LOCATION) must startWith(appConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to eori common component" in {

        val authAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new InsufficientEnrolments),
          appConfig,
          bodyParsers
        )
        val controller: Harness                       = new Harness(authAction)
        val result: Result                            = controller.onPageLoad()(FakeRequest()).futureValue

        result.header.status mustBe SEE_OTHER
        result.header.headers(LOCATION) mustBe appConfig.arsSubscribeUrl
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {

        val authAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
          appConfig,
          bodyParsers
        )
        val controller: Harness                       = new Harness(authAction)
        val result: Result                            = controller.onPageLoad()(FakeRequest()).futureValue

        result.header.status mustBe SEE_OTHER
        result.header.headers(LOCATION) mustBe routes.UnauthorisedController.onPageLoad.url
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {

        val authAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new UnsupportedAuthProvider),
          appConfig,
          bodyParsers
        )
        val controller: Harness                       = new Harness(authAction)
        val result: Result                            = controller.onPageLoad()(FakeRequest()).futureValue

        result.header.status mustBe SEE_OTHER
        result.header.headers(LOCATION) mustBe routes.UnauthorisedController.onPageLoad.url
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {

        val authAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
          appConfig,
          bodyParsers
        )
        val controller: Harness                       = new Harness(authAction)
        val result: Result                            = controller.onPageLoad()(FakeRequest()).futureValue

        result.header.status mustBe SEE_OTHER
        result.header.headers(LOCATION) mustBe routes.UnauthorisedController.onPageLoad.url
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {

        val authAction: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new UnsupportedCredentialRole),
          appConfig,
          bodyParsers
        )
        val controller: Harness                       = new Harness(authAction)
        val result: Result                            = controller.onPageLoad()(FakeRequest()).futureValue

        result.header.status mustBe SEE_OTHER
        result.header.headers(LOCATION) mustBe routes.UnauthorisedController.onPageLoad.url
      }
    }
  }
}

private class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}

private object AuthActionSpec {
  implicit class AuthRetrievals[A](a: A) {
    def composeRetrievals[B](b: B): ~[A, B] = new ~(a, b)
  }
}
