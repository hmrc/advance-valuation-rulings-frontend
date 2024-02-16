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

package navigation

import base.SpecBase
import controllers.routes
import controllers.routes.ApplicationContactDetailsController
import models.AuthUserType.{Agent, IndividualTrader, OrganisationAssistant, OrganisationUser}
import models.WhatIsYourRoleAsImporter.EmployeeOfOrg
import models._
import org.mockito.MockitoSugar.{mock, when}
import pages._
import play.api.libs.json.Writes
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries._
import userrole.{UserRole, UserRoleProvider}

import java.time.Instant

class NavigatorSpec extends SpecBase {

  private val EmptyUserAnswers: UserAnswers          = userAnswersAsIndividualTrader
  private val userRoleProvider                       = mock[UserRoleProvider]
  val unchangedModeNavigator: UnchangedModeNavigator = new UnchangedModeNavigator
  val navigator: Navigator                           = new Navigator(userRoleProvider, unchangedModeNavigator)

  private val successfulFile = UploadedFile.Success(
    reference = "reference",
    downloadUrl = "downloadUrl",
    uploadDetails = UploadedFile.UploadDetails(
      fileName = "fileName",
      fileMimeType = "fileMimeType",
      uploadTimestamp = Instant.now(),
      checksum = "checksum",
      size = 1337
    )
  )

  "Navigator" - {

    def userAnswersWith[A: Writes](page: Modifiable[A], value: A): UserAnswers =
      EmptyUserAnswers.set(page, value).success.value

    "/ must navigate to AccountHome" in {

      def redirectRoute: Call = routes.AccountHomeController.onPageLoad()

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      val request = FakeRequest(GET, "/advance-valuation-ruling/")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe redirectRoute.url

    }

    "must go from a page that doesn't exist in the route map to AccountHome" in {
      case object UnknownPage extends Page
      navigator.nextPage(
        UnknownPage,
        NormalMode,
        EmptyUserAnswers
      ) mustBe routes.AccountHomeController.onPageLoad()
    }

    "Account Home" - {

      "should navigate to RequiredInformation page for a IndividualTrader" in {

        navigator.nextPage(
          AccountHomePage,
          NormalMode,
          userAnswersAsIndividualTrader.setFuture(AccountHomePage, IndividualTrader).futureValue
        ) mustBe routes.WhatIsYourRoleAsImporterController
          .onPageLoad(NormalMode, draftId)
      }

      "should navigate to WhatIsYourRole page for an OrganisationAssistant" in {

        navigator.nextPage(
          AccountHomePage,
          NormalMode,
          userAnswersAsIndividualTrader
            .setFuture(AccountHomePage, OrganisationAssistant)
            .futureValue
        ) mustBe routes.WhatIsYourRoleAsImporterController
          .onPageLoad(NormalMode, draftId)
      }

      "should navigate to RequiredInformation page for an OrganisationUser" in {

        navigator.nextPage(
          AccountHomePage,
          NormalMode,
          userAnswersAsIndividualTrader.setFuture(AccountHomePage, OrganisationUser).futureValue
        ) mustBe routes.WhatIsYourRoleAsImporterController
          .onPageLoad(NormalMode, draftId)
      }

      "should navigate to WhatIsYourRole page for an Agent" in {

        navigator.nextPage(
          AccountHomePage,
          NormalMode,
          userAnswersAsIndividualTrader.setFuture(AccountHomePage, Agent).futureValue
        ) mustBe routes.WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, draftId)
      }

      "should navigate to JourneyRecovery page when ApplicantUserType does not exist in userAnswers" in {

        navigator.nextPage(
          AccountHomePage,
          NormalMode,
          UserAnswers(userAnswersId, draftId)
        ) mustBe routes.UnauthorisedController.onPageLoad
      }
    }

    "in Normal mode" - {

      "verifyLetterOfAuthorityPage" - {
        "must navigate to RequiredInformationPage page" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(
            VerifyLetterOfAuthorityPage,
            NormalMode,
            userAnswers
          ) mustBe routes.BusinessContactDetailsController.onPageLoad(NormalMode, draftId)

        }
      }

      "If we need to contact you page" - {

        "must receive next page from userRoleProvider" in {

          val userAnswers  = emptyUserAnswers // usage is mocked in this test
          val mockUserRole = mock[UserRole]

          when(userRoleProvider.getUserRole(userAnswers)) thenReturn mockUserRole
          when(
            mockUserRole.getEORIDetailsJourney(NormalMode, draftId)
          ) thenReturn onwardRoute

          navigator.nextPage(
            ContactPagePage,
            NormalMode,
            userAnswers
          ) mustBe onwardRoute

        }
      }

      "ChoosingMethodPage" - {
        "must navigate to ValuationMethod" in {
          navigator.nextPage(
            ChoosingMethodPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode, draftId)
        }
      }

      "AgentForTraderContactDetailsPage" - {
        "must navigate to ChoosingMethod page" in {
          navigator.nextPage(
            AgentForTraderContactDetailsPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe routes.ChoosingMethodController.onPageLoad(draftId)
        }
      }

      "WhatIsYourRoleAsImporterPage" - {

        "must navigate to RequiredInformationPage page when valid answer" in {
          val userAnswers =
            userAnswersWith(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
          navigator.nextPage(
            WhatIsYourRoleAsImporterPage,
            NormalMode,
            userAnswers
          ) mustBe routes.RequiredInformationController.onPageLoad(draftId)
        }

        "must navigate to WhatIsYourRoleAsImporterController page when no answer" in {
          val userAnswers =
            userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            WhatIsYourRoleAsImporterPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, draftId)
        }
      }

      "ChangeYourRoleImporterPage" - {

        "when user answers ChangeYourRoleImporterPage == true and WhatIsYourRoleAsImporterPage has not been answered" - {

          "must navigate to the WhatIsYourRoleAsImporter page" in {

            val userAnswers =
              emptyUserAnswers
                .setFuture(AccountHomePage, IndividualTrader)
                .futureValue
                .setFuture(ChangeYourRoleImporterPage, true)
                .futureValue

            val actual =
              navigator.nextPage(
                WhatIsYourRoleAsImporterPage,
                NormalMode,
                userAnswers
              )

            val expected = routes.WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, draftId)

            actual mustBe expected
          }
        }

        "when user answers the ChangeYourRoleImporterPage, but the WhatIsYourRoleAsImporterPage is unanswered" - {

          "must navigate to the WhatIsYourRoleAsImporter page" in {

            val userAnswers =
              emptyUserAnswers
                .setFuture(AccountHomePage, IndividualTrader)
                .futureValue
                .setFuture(ChangeYourRoleImporterPage, true)
                .futureValue

            val actual =
              navigator.nextPage(
                ChangeYourRoleImporterPage,
                NormalMode,
                userAnswers
              )

            val expected = routes.WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, draftId)

            actual mustBe expected
          }
        }

        "when user answers the ChangeYourRoleImporterPage, the WhatIsYourRoleAsImporterPage Role is any role" - {

          "must navigate to the RequiredInformation page" in {

            val userAnswers =
              emptyUserAnswers
                .setFuture(AccountHomePage, IndividualTrader)
                .futureValue
                .setFuture(ChangeYourRoleImporterPage, true)
                .futureValue
                .setFuture(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
                .futureValue

            val actual =
              navigator.nextPage(
                ChangeYourRoleImporterPage,
                NormalMode,
                userAnswers
              )

            val expected = routes.RequiredInformationController.onPageLoad(draftId)

            actual mustBe expected
          }
        }

        "when user answers ChangeYourRoleImporterPage == false, the WhatIsYourRoleAsImporterPage Role answered or unanswered" - {

          "must navigate to the WhatIsYourRoleAsImporter page" in {

            val userAnswers =
              emptyUserAnswers
                .setFuture(AccountHomePage, IndividualTrader)
                .futureValue
                .setFuture(ChangeYourRoleImporterPage, false)
                .futureValue

            val actual =
              navigator.nextPage(
                ChangeYourRoleImporterPage,
                NormalMode,
                userAnswers
              )

            val expected = routes.WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, draftId)

            actual mustBe expected
          }
        }
      }

      "TellUsAboutYourRulingPage" - {
        "must navigate to TellUsAboutYourRulingController when no answer saved" in {
          val userAnswers = userAnswersAsIndividualTrader

          navigator.nextPage(
            TellUsAboutYourRulingPage,
            NormalMode,
            userAnswers
          ) mustBe routes.TellUsAboutYourRulingController.onPageLoad(NormalMode, draftId)
        }

        "must navigate to AwareOfRulingController when answer saved" in {
          val userAnswers = userAnswersWith(TellUsAboutYourRulingPage, "test string")

          navigator.nextPage(
            TellUsAboutYourRulingPage,
            NormalMode,
            userAnswers
          ) mustBe routes.AwareOfRulingController.onPageLoad(NormalMode, draftId)
        }
      }

      "HaveYouReceivedADecisionPage" - {
        "must navigate to HaveYouReceivedADecisionController when no answer saved" in {
          val userAnswers = userAnswersAsIndividualTrader

          navigator.nextPage(
            HaveYouReceivedADecisionPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HaveYouReceivedADecisionController.onPageLoad(NormalMode, draftId)

        }

        "must navigate to TellUsAboutYourRulingController when true answer saved" in {
          val userAnswers = userAnswersWith(HaveYouReceivedADecisionPage, true)

          navigator.nextPage(
            HaveYouReceivedADecisionPage,
            NormalMode,
            userAnswers
          ) mustBe routes.TellUsAboutYourRulingController.onPageLoad(NormalMode, draftId)

        }

        "must navigate to AwareOfRulingController when false answer saved" in {
          val userAnswers = userAnswersWith(HaveYouReceivedADecisionPage, false)

          navigator.nextPage(
            HaveYouReceivedADecisionPage,
            NormalMode,
            userAnswers
          ) mustBe routes.AwareOfRulingController.onPageLoad(NormalMode, draftId)

        }
      }

      "AboutSimilarGoodsPage" - {
        "must navigate to AboutSimilarGoodsController when no answer saved" in {
          val userAnswers = userAnswersAsIndividualTrader

          navigator.nextPage(
            AboutSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.AboutSimilarGoodsController.onPageLoad(NormalMode, draftId)
        }

        "must navigate to HasCommodityController when answer saved" in {
          val userAnswers = userAnswersWith(AboutSimilarGoodsPage, "test string")

          navigator.nextPage(
            AboutSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HasCommodityCodeController.onPageLoad(NormalMode, draftId)
        }
      }

      "AwareOfRulingPage" - {
        "must navigate to AwareOfRulingController when no answer saved" in {
          val userAnswers = userAnswersAsIndividualTrader

          navigator.nextPage(
            AwareOfRulingPage,
            NormalMode,
            userAnswers
          ) mustBe routes.AwareOfRulingController.onPageLoad(NormalMode, draftId)

        }

        "must navigate to AboutSimilarGoodsController when true answer saved" in {
          val userAnswers = userAnswersWith(AwareOfRulingPage, true)

          navigator.nextPage(
            AwareOfRulingPage,
            NormalMode,
            userAnswers
          ) mustBe routes.AboutSimilarGoodsController.onPageLoad(NormalMode, draftId)

        }

        "must navigate to HasCommodityCodeController when false answer saved" in {
          val userAnswers = userAnswersWith(AwareOfRulingPage, false)

          navigator.nextPage(
            AwareOfRulingPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HasCommodityCodeController.onPageLoad(NormalMode, draftId)

        }
      }

      "WhyTransactionValueOfSimilarGoods page" - {

        "must navigate to HaveYouUsedMethodOneInPast page when valid answer" in {
          val userAnswers =
            userAnswersWith(WhyTransactionValueOfSimilarGoodsPage, "bananas")
          navigator.nextPage(
            WhyTransactionValueOfSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(
            NormalMode,
            draftId
          )
        }

        "must navigate to WhyTransactionValueOfSimilarGoodsController page when invalid answer" in {
          val userAnswers =
            userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            WhyTransactionValueOfSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyTransactionValueOfSimilarGoodsController.onPageLoad(
            NormalMode,
            draftId
          )
        }
      }

      "HaveYouUsedMethodOneInPast page" - {

        "must navigate to describeTheIdenticalGoods Page when True" in {
          val ans = userAnswersWith(HaveYouUsedMethodOneInPastPage, true)
          navigator.nextPage(
            HaveYouUsedMethodOneInPastPage,
            NormalMode,
            ans
          ) mustBe routes.DescribeTheIdenticalGoodsController.onPageLoad(NormalMode, draftId)
        }

        "must navigate to describeTheIdenticalGoods Page when no answer" in {
          val ans = userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            HaveYouUsedMethodOneInPastPage,
            NormalMode,
            ans
          ) mustBe routes.HaveYouUsedMethodOneInPastController.onPageLoad(NormalMode, draftId)
        }

        "must navigate to ValuationMethod Page when False" in {
          val ans = userAnswersWith(HaveYouUsedMethodOneInPastPage, false)
          navigator.nextPage(
            HaveYouUsedMethodOneInPastPage,
            NormalMode,
            ans
          ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode, draftId)
        }
      }

      "haveYouUsedMethodOneForSimilarGoodsInPast page" - {

        "must navigate to describeTheSimilarGoods Page when True" in {
          val ans = userAnswersWith(HaveYouUsedMethodOneForSimilarGoodsInPastPage, true)
          navigator.nextPage(
            HaveYouUsedMethodOneForSimilarGoodsInPastPage,
            NormalMode,
            ans
          ) mustBe routes.DescribeTheSimilarGoodsController.onPageLoad(NormalMode, draftId)
        }

        "must navigate to HaveYouUsedMethodOneForSimilarGoodsInPastController Page when no answer" in {
          val ans = userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            HaveYouUsedMethodOneForSimilarGoodsInPastPage,
            NormalMode,
            ans
          ) mustBe routes.HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(NormalMode, draftId)
        }

        "must navigate to ValuationMethod Page when False" in {
          val ans = userAnswersWith(HaveYouUsedMethodOneForSimilarGoodsInPastPage, false)
          navigator.nextPage(
            HaveYouUsedMethodOneForSimilarGoodsInPastPage,
            NormalMode,
            ans
          ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode, draftId)

        }
      }

      "DescribeTheSimilarGoods page" - {
        "must navigate to WillYouCompareToSimilarGoods Page when valid answer" in {
          val userAnswers =
            userAnswersWith(DescribeTheSimilarGoodsPage, "similar goods")
          navigator.nextPage(
            DescribeTheSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
        }

        "must navigate to DescribeTheSimilarGoodsController Page when no answer" in {
          val userAnswers =
            userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            DescribeTheSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescribeTheSimilarGoodsController.onPageLoad(NormalMode, draftId)
        }
      }

      "DescribeTheIdenticalGoods page" - {
        "must navigate to DescriptionOfGoods Page when valid answer" in {
          val userAnswers =
            userAnswersWith(DescribeTheIdenticalGoodsPage, "describe goods")
          navigator.nextPage(
            DescribeTheIdenticalGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
        }

        "must navigate to DescriptionOfGoods Page when no answer" in {
          val userAnswers =
            userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            DescribeTheIdenticalGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescribeTheIdenticalGoodsController.onPageLoad(NormalMode, draftId)
        }
      }

      "ExplainWhyYouHaveNotSelectedMethodOneToThree page" - {
        "must navigate to ExplainWhyYouChoseMethodFour Page when valid answer" in {
          val userAnswers =
            userAnswersWith(ExplainWhyYouHaveNotSelectedMethodOneToThreePage, "explain method four")
          navigator.nextPage(
            ExplainWhyYouHaveNotSelectedMethodOneToThreePage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainWhyYouChoseMethodFourController.onPageLoad(NormalMode, draftId)
        }

        "must navigate to ExplainWhyYouHaveNotSelectedMethodOneToThreeController Page when no answer" in {
          val userAnswers =
            userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            ExplainWhyYouHaveNotSelectedMethodOneToThreePage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(NormalMode, draftId)
        }
      }

      "ExplainWhyYouChoseMethodFour page" - {
        "must navigate to DescriptionOfGoods Page when valid answers" in {
          val userAnswers =
            userAnswersWith(ExplainWhyYouChoseMethodFourPage, "explain method four")
          navigator.nextPage(
            ExplainWhyYouChoseMethodFourPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
        }

        "must navigate to DescriptionOfGoods Page when invalid answers" in {
          val userAnswers =
            userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            ExplainWhyYouChoseMethodFourPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainWhyYouChoseMethodFourController.onPageLoad(NormalMode, draftId)
        }
      }

      "ImportGoodsPage must" - {

        "navigate to ContactPage page when True" in {
          val userAnswers = userAnswersWith(ImportGoodsPage, true)
          navigator.nextPage(
            ImportGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ContactPageController.onPageLoad(NormalMode, draftId)
        }

        "and navigate to ImportingGoodsPage when False" in {
          val userAnswers = userAnswersWith(ImportGoodsPage, false)
          navigator.nextPage(
            ImportGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ImportingGoodsController.onPageLoad(draftId)
        }

        "navigate to ImportingGoodsPage when no value is set" in {
          navigator.nextPage(
            ImportGoodsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ImportGoodsController.onPageLoad(NormalMode, draftId)
        }
      }

      "HaveTheGoodsBeenSubjectToLegalChallenges page" - {
        "navigate to DescribeTheLegalChallenges when Yes" in {
          val userAnswers = userAnswersWith(HaveTheGoodsBeenSubjectToLegalChallengesPage, true)
          navigator.nextPage(
            HaveTheGoodsBeenSubjectToLegalChallengesPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescribeTheLegalChallengesController.onPageLoad(NormalMode, draftId)
        }

        "and navigate to HasConfidentialInformation when No" in {
          val userAnswers =
            userAnswersWith(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          navigator.nextPage(
            HaveTheGoodsBeenSubjectToLegalChallengesPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HasConfidentialInformationController.onPageLoad(NormalMode, draftId)
        }

        "and navigate to HaveTheGoodsBeenSubjectToLegalChallengesController when no answer for HaveTheGoodsBeenSubjectToLegalChallengesPage" in {

          val userAnswers =
            userAnswersWith(HasCommodityCodePage, false)

          navigator.nextPage(
            HaveTheGoodsBeenSubjectToLegalChallengesPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(NormalMode, draftId)
        }
      }

      "DescribeTheLegalChallenges" - {
        "navigate to HasConfidentialInformation page when all values are set" in {
          val userAnswers =
            userAnswersWith(DescribeTheLegalChallengesPage, "legalities")
          navigator.nextPage(
            DescribeTheLegalChallengesPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HasConfidentialInformationController.onPageLoad(NormalMode, draftId)
        }

        "navigate to self when no values are set" in {
          navigator.nextPage(
            DescribeTheLegalChallengesPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.DescribeTheLegalChallengesController.onPageLoad(NormalMode, draftId)
        }
      }

      "HasConfidentialInformation page" - {
        "navigate to DescribeTheLegalChallenges when Yes" in {
          val userAnswers = userAnswersWith(HasConfidentialInformationPage, true)
          navigator.nextPage(
            HasConfidentialInformationPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ConfidentialInformationController.onPageLoad(NormalMode, draftId)
        }

        "navigate to HasConfidentialInformationController when no valid answer" in {
          val userAnswers = userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            HasConfidentialInformationPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HasConfidentialInformationController.onPageLoad(NormalMode, draftId)
        }

        "when no" - {

          "when there are no documents" - {

            "must navigate to DoYouWantToUploadDocuments" in {
              val userAnswers = userAnswersWith(HasConfidentialInformationPage, false)
              navigator.nextPage(
                HasConfidentialInformationPage,
                NormalMode,
                userAnswers
              ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, draftId)
            }
          }

          "when there are existing documents" - {

            "must navigate to UploadAnotherSupportingDocument" in {
              val userAnswers = (for {
                ua <- EmptyUserAnswers.set(HasConfidentialInformationPage, false)
                ua <- ua.set(AllDocuments, List(DraftAttachment(successfulFile, Some(true))))
              } yield ua).success.value

              navigator.nextPage(
                HasConfidentialInformationPage,
                NormalMode,
                userAnswers
              ) mustBe routes.UploadAnotherSupportingDocumentController.onPageLoad(
                NormalMode,
                draftId
              )
            }
          }
        }
      }

      "ConfidentialInformation page" - {

        "navigate to DoYouWantToUploadDocuments page when there are no documents" in {
          val userAnswers =
            userAnswersWith(ConfidentialInformationPage, "top secret")
          navigator.nextPage(
            ConfidentialInformationPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, draftId)
        }

        "navigate to UploadAnotherSupportingDocument page when there are existing documents" in {
          val userAnswers = (for {
            ua <- EmptyUserAnswers.set(ConfidentialInformationPage, "top secret")
            ua <- ua.set(AllDocuments, List(DraftAttachment(successfulFile, Some(true))))
          } yield ua).success.value

          navigator.nextPage(
            ConfidentialInformationPage,
            NormalMode,
            userAnswers
          ) mustBe routes.UploadAnotherSupportingDocumentController.onPageLoad(NormalMode, draftId)
        }

        "navigate to self when no values are set" in {
          navigator.nextPage(
            ConfidentialInformationPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ConfidentialInformationController.onPageLoad(NormalMode, draftId)
        }
      }

      "CheckRegisteredDetailsPage must" - {
        "navigate to Page from UserRole when Yes" in {
          val userAnswers = userAnswersAsIndividualTrader
            .setFuture(CheckRegisteredDetailsPage, value = true)
            .futureValue

          val userRole = mock[UserRole]
          when(userRoleProvider.getUserRole(userAnswers)).thenReturn(userRole)
          when(userRole.getContactDetailsJourney(draftId))
            .thenReturn(ApplicationContactDetailsController.onPageLoad(NormalMode, draftId))

          navigator.nextPage(
            CheckRegisteredDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ApplicationContactDetailsController.onPageLoad(NormalMode, draftId)
        }

        "navigate to Page from UserRole when no answer provided" in {
          val userAnswers = userAnswersAsIndividualTrader
            .setFuture(HasCommodityCodePage, value = true)
            .futureValue

          navigator.nextPage(
            CheckRegisteredDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.CheckRegisteredDetailsController.onPageLoad(NormalMode, draftId)
        }

        "and navigate to EORIBeUpToDatePage when No" in {
          val userAnswers = userAnswersAsIndividualTrader
            .setFuture(CheckRegisteredDetailsPage, value = false)
            .futureValue

          navigator.nextPage(
            CheckRegisteredDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.EORIBeUpToDateController.onPageLoad(draftId)
        }
      }

      "UploadLetterOfAuthorityPage must navigate to" - {

        "VerifyLetterOfAuthority page" in {
          navigator.nextPage(
            UploadLetterOfAuthorityPage,
            NormalMode,
            userAnswersAsIndividualTrader
          ) mustBe routes.VerifyLetterOfAuthorityController.onPageLoad(NormalMode, draftId)
        }
      }

      "AgentForOrgCheckRegisteredDetailsPage must navigate to" - {

        "BusinessContactDetailsController page" in {
          navigator.nextPage(
            AgentForOrgCheckRegisteredDetailsPage,
            NormalMode,
            userAnswersAsIndividualTrader
          ) mustBe routes.BusinessContactDetailsController.onPageLoad(NormalMode, draftId)
        }
      }

      "AgentForOrgApplicationContactDetailsPage must navigate to" - {

        "AgentCompanyDetailsController page" in {
          navigator.nextPage(
            AgentForOrgApplicationContactDetailsPage,
            NormalMode,
            userAnswersAsIndividualTrader
          ) mustBe routes.AgentCompanyDetailsController.onPageLoad(NormalMode, draftId)
        }
      }

      "ProvideTraderEoriPage must navigate to" - {

        "VerifyTraderEoriController page" in {
          navigator.nextPage(
            ProvideTraderEoriPage,
            NormalMode,
            userAnswersAsIndividualTrader
          ) mustBe routes.VerifyTraderEoriController.onPageLoad(NormalMode, draftId)
        }
      }

      "AgentCompanyDetailsPage" - {
        "must navigate to ChoosingMethodController when have valid answer" in {
          val userAnswers =
            userAnswersWith(
              AgentCompanyDetailsPage,
              AgentCompanyDetails(
                "Eori",
                "name",
                "streetAndNumber",
                "agentCity",
                Country("GB", "United Kingdom"),
                Some("AB1 2CD")
              )
            )

          navigator.nextPage(
            AgentCompanyDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ChoosingMethodController.onPageLoad(draftId)
        }

        "must navigate to AgentCompanyDetailsController when no answer" in {
          val userAnswers = userAnswersWith(HasCommodityCodePage, true)

          navigator.nextPage(
            AgentCompanyDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.AgentCompanyDetailsController.onPageLoad(NormalMode, draftId)
        }
      }

      "ApplicationContactDetailsPage" - {
        "must navigate to ChoosingMethodController when have valid answer" in {
          val userAnswers =
            userAnswersWith(
              ApplicationContactDetailsPage,
              ApplicationContactDetails("name", "email", "phone", "jobTitle")
            )
          navigator.nextPage(
            ApplicationContactDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ChoosingMethodController.onPageLoad(draftId)
        }

        "must navigate to AgentCompanyDetailsController when no answer for ApplicationContactDetailsPage" in {
          val userAnswers = userAnswersWith(HasCommodityCodePage, true)

          navigator.nextPage(
            ApplicationContactDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ApplicationContactDetailsController.onPageLoad(NormalMode, draftId)
        }
      }

      "DoYouWantToUploadDocumentsPage must" - {
        "self when no method is select" in {
          navigator.nextPage(
            DoYouWantToUploadDocumentsPage,
            NormalMode,
            userAnswersAsIndividualTrader
          ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, draftId)
        }

        "UploadSupportingDocumentsPage when Yes is selected" in {
          val userAnswers =
            userAnswersAsIndividualTrader.set(DoYouWantToUploadDocumentsPage, true).get
          navigator.nextPage(
            DoYouWantToUploadDocumentsPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.routes.UploadSupportingDocumentsController
            .onPageLoad(NormalMode, draftId, None, None)
        }

        "CheckYourAnswers page when No is selected" in {
          val userAnswers =
            userAnswersAsIndividualTrader.set(DoYouWantToUploadDocumentsPage, false).get
          navigator.nextPage(
            DoYouWantToUploadDocumentsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.CheckYourAnswersController.onPageLoad(draftId)
        }
      }

      "UploadSupportingDocumentPage must navigate to" - {

        "IsThisFileConfidential page" in {
          navigator.nextPage(
            UploadSupportingDocumentPage,
            NormalMode,
            userAnswersAsIndividualTrader
          ) mustBe routes.IsThisFileConfidentialController.onPageLoad(NormalMode, draftId)
        }
      }

      "IsThisFileConfidentialPage must navigate to" - {

        "UploadAnotherSupportingDocument page" in {
          navigator.nextPage(
            IsThisFileConfidentialPage,
            NormalMode,
            userAnswersAsIndividualTrader
          ) mustBe routes.UploadAnotherSupportingDocumentController.onPageLoad(NormalMode, draftId)
        }
      }

      "UploadAnotherSupportingDocumentPage must navigate to" - {

        "UploadSupportingDocumentsPage when Yes is selected" in {
          val userAnswers =
            userAnswersAsIndividualTrader.set(UploadAnotherSupportingDocumentPage, true).get
          navigator.nextPage(
            UploadAnotherSupportingDocumentPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.routes.UploadSupportingDocumentsController
            .onPageLoad(NormalMode, draftId, None, None)
        }

        "UploadSupportingDocumentsPage when Yes is selected and there are other files" in {
          val userAnswers = (for {
            ua <- EmptyUserAnswers.set(UploadSupportingDocumentPage, successfulFile)
            ua <- ua.set(IsThisFileConfidentialPage, true)
            ua <- ua.set(UploadAnotherSupportingDocumentPage, true)
          } yield ua).success.value

          navigator.nextPage(
            UploadAnotherSupportingDocumentPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.routes.UploadSupportingDocumentsController
            .onPageLoad(NormalMode, draftId, None, None)
        }

        "CheckYourAnswers page when No is selected and the user is an IndividualTrader" in {
          val userAnswers =
            userAnswersAsIndividualTrader.set(UploadAnotherSupportingDocumentPage, false).get
          navigator.nextPage(
            UploadAnotherSupportingDocumentPage,
            NormalMode,
            userAnswers
          ) mustBe routes.CheckYourAnswersController.onPageLoad(draftId)
        }

        "CheckYourAnswersForAgents page when No is selected and the user is an OrganisationAdmin" in {
          val userAnswers =
            userAnswersAsOrgUser.set(UploadAnotherSupportingDocumentPage, false).get
          navigator.nextPage(
            UploadAnotherSupportingDocumentPage,
            NormalMode,
            userAnswers
          ) mustBe routes.CheckYourAnswersController.onPageLoad(
            draftId
          )
        }

        "CheckYourAnswersForAgents page when No is selected and the user is an OrganisationAssistant" in {
          val userAnswers =
            userAnswersAsOrgAssistant.set(UploadAnotherSupportingDocumentPage, false).get
          navigator.nextPage(
            UploadAnotherSupportingDocumentPage,
            NormalMode,
            userAnswers
          ) mustBe routes.CheckYourAnswersController.onPageLoad(
            draftId
          )
        }

        "JourneyRecovery page when the page is not answered" in {
          navigator.nextPage(
            UploadAnotherSupportingDocumentPage,
            NormalMode,
            userAnswersAsIndividualTrader
          ) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "RemoveSupportingDocumentPage must navigate to" - {

        "UploadAnotherSupportingDocument page when there are more documents" in {
          val answers =
            userAnswersAsIndividualTrader
              .set(AllDocuments, List(DraftAttachment(successfulFile, Some(true))))
              .success
              .value

          navigator.nextPage(
            RemoveSupportingDocumentPage(Index(0)),
            NormalMode,
            answers
          ) mustBe routes.UploadAnotherSupportingDocumentController.onPageLoad(NormalMode, draftId)
        }

        "DoYouWantToUploadSupportingDocuments page when there are no more documents" in {
          navigator.nextPage(
            RemoveSupportingDocumentPage(Index(0)),
            NormalMode,
            userAnswersAsIndividualTrader
          ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, draftId)
        }
      }

      "valuationMethod page must navigate to" - {
        "choose valuation method when no method is selected" in {
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ChoosingMethodController.onPageLoad(draftId)
        }

        "isThereASaleInvolved page when method 1 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method1)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.IsThereASaleInvolvedController.onPageLoad(NormalMode, draftId)
        }

        "WhyIdenticalGoods page when method 2 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method2)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyIdenticalGoodsController.onPageLoad(NormalMode, draftId)
        }

        "WhyTransactionValueOfSimilarGoods page when method 3 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method3)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyTransactionValueOfSimilarGoodsController.onPageLoad(
            NormalMode,
            draftId
          )
        }

        "ExplainWhyYouHaveNotSelectedMethodOneToThree page when method 4 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method4)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(
            mode = NormalMode,
            draftId
          )
        }

        "WhyComputedValue page when method 5 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method5)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyComputedValueController.onPageLoad(NormalMode, draftId)
        }

        "ExplainWhyYouHaveNotSelectedMethodOneToFiveController page when method 6 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method6)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(
            mode = NormalMode,
            draftId
          )
        }
      }

      "isThereASaleInvolved must" - {
        "navigate to IsSaleBetweenRelatedParties page when yes" in {
          navigator.nextPage(
            IsThereASaleInvolvedPage,
            NormalMode,
            userAnswersAsIndividualTrader.set(IsThereASaleInvolvedPage, true).success.value
          ) mustBe routes.IsSaleBetweenRelatedPartiesController.onPageLoad(NormalMode, draftId)
        }

        "navigate to IsSaleBetweenRelatedParties page when no answer" in {
          navigator.nextPage(
            IsThereASaleInvolvedPage,
            NormalMode,
            userAnswersAsIndividualTrader.set(HasCommodityCodePage, true).success.value
          ) mustBe routes.IsThereASaleInvolvedController.onPageLoad(NormalMode, draftId)
        }

        "navigate to valuationMethod page when no" in {
          navigator.nextPage(
            IsThereASaleInvolvedPage,
            NormalMode,
            userAnswersAsIndividualTrader.set(IsThereASaleInvolvedPage, false).success.value
          ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode, draftId)
        }
      }

      "IsSaleBetweenRelatedParties page must" - {
        "navigate to ExplainHowPartiesAreRelated page when yes" in {
          navigator.nextPage(
            IsSaleBetweenRelatedPartiesPage,
            NormalMode,
            userAnswersAsIndividualTrader
              .set(IsSaleBetweenRelatedPartiesPage, true)
              .success
              .value
          ) mustBe routes.ExplainHowPartiesAreRelatedController.onPageLoad(NormalMode, draftId)
        }

        "navigate to ExplainHowPartiesAreRelated page when no answer" in {
          navigator.nextPage(
            IsSaleBetweenRelatedPartiesPage,
            NormalMode,
            userAnswersAsIndividualTrader
              .set(HasCommodityCodePage, true)
              .success
              .value
          ) mustBe routes.IsSaleBetweenRelatedPartiesController.onPageLoad(NormalMode, draftId)
        }

        "navigate to restrictions page when no" in {
          navigator.nextPage(
            IsSaleBetweenRelatedPartiesPage,
            NormalMode,
            userAnswersAsIndividualTrader
              .set(IsSaleBetweenRelatedPartiesPage, false)
              .success
              .value
          ) mustBe routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode, draftId)
        }
      }

      "ExplainHowPartiesAreRelated page must" - {
        "navigate to AreThereRestrictionsOnTheGoodsController page when valid answer" in {
          navigator.nextPage(
            ExplainHowPartiesAreRelatedPage,
            NormalMode,
            userAnswersAsIndividualTrader
              .set(ExplainHowPartiesAreRelatedPage, "explain")
              .success
              .value
          ) mustBe routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode, draftId)
        }

        "navigate to AreThereRestrictionsOnTheGoodsController page when no answer" in {
          navigator.nextPage(
            ExplainHowPartiesAreRelatedPage,
            NormalMode,
            userAnswersAsIndividualTrader
              .set(HasCommodityCodePage, true)
              .success
              .value
          ) mustBe routes.ExplainHowPartiesAreRelatedController.onPageLoad(NormalMode, draftId)
        }
      }

      "DescriptionOfGoods page must" - {
        "navigate to HaveYouReceivedADecision page when has answer for DescriptionOfGoodsPage" in {
          navigator.nextPage(
            DescriptionOfGoodsPage,
            NormalMode,
            userAnswersAsIndividualTrader
              .set(DescriptionOfGoodsPage, "explain")
              .success
              .value
          ) mustBe routes.HaveYouReceivedADecisionController.onPageLoad(NormalMode, draftId)
        }

        "navigate to DescriptionOfGoodsController page when no answer for DescriptionOfGoodsPage" in {
          navigator.nextPage(
            DescriptionOfGoodsPage,
            NormalMode,
            userAnswersAsIndividualTrader
              .set(DescribeTheConditionsPage, "explain")
              .success
              .value
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
        }
      }

      "HasCommodityCodePage must" - {
        "navigate to CommodityCode when yes" in {
          navigator.nextPage(
            HasCommodityCodePage,
            NormalMode,
            userAnswersWith(HasCommodityCodePage, true)
          ) mustBe routes.CommodityCodeController.onPageLoad(NormalMode, draftId)
        }

        "navigate to CommodityCode when no" in {
          navigator.nextPage(
            HasCommodityCodePage,
            NormalMode,
            userAnswersWith(HasCommodityCodePage, false)
          ) mustBe routes.HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(NormalMode, draftId)
        }

        "navigate to CommodityCodeController when no answer for HasCommodityCodePage" in {
          navigator.nextPage(
            HasCommodityCodePage,
            NormalMode,
            userAnswersWith(HasConfidentialInformationPage, false)
          ) mustBe routes.HasCommodityCodeController.onPageLoad(NormalMode, draftId)
        }
      }

      "CommodityCodePage must" - {
        "navigate to WhatCountryAreGoodsFrom when valid answer" in {
          navigator.nextPage(
            CommodityCodePage,
            NormalMode,
            userAnswersWith(CommodityCodePage, "1234567890")
          ) mustBe routes.HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(
            NormalMode,
            draftId
          )
        }

        "navigate to WhatCountryAreGoodsFrom when no answer" in {
          navigator.nextPage(
            CommodityCodePage,
            NormalMode,
            userAnswersWith(HasCommodityCodePage, true)
          ) mustBe routes.CommodityCodeController.onPageLoad(
            NormalMode,
            draftId
          )
        }
      }

      "areThereRestrictionsOnTheGoods page must" - {
        "navigate to DescribeTheRestrictions when True" in {
          navigator.nextPage(
            AreThereRestrictionsOnTheGoodsPage,
            NormalMode,
            userAnswersWith(AreThereRestrictionsOnTheGoodsPage, true)
          ) mustBe routes.DescribeTheRestrictionsController.onPageLoad(NormalMode, draftId)
        }

        "navigate to IsTheSaleSubjectToConditions when False" in {
          navigator.nextPage(
            AreThereRestrictionsOnTheGoodsPage,
            NormalMode,
            userAnswersWith(AreThereRestrictionsOnTheGoodsPage, false)
          ) mustBe routes.IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode, draftId)
        }

        "navigate to itself when user has no data for the page" in {
          navigator.nextPage(
            AreThereRestrictionsOnTheGoodsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode, draftId)
        }
      }

      "describeTheRestrictions page must" - {
        "navigate to itself when user has no data for the page" in {
          navigator.nextPage(
            DescribeTheRestrictionsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.DescribeTheRestrictionsController.onPageLoad(NormalMode, draftId)
        }

        "navigate to IsTheSaleSubjectToConditions when answers has data" in {
          navigator.nextPage(
            DescribeTheRestrictionsPage,
            NormalMode,
            userAnswersWith(DescribeTheRestrictionsPage, "Some restrictions")
          ) mustBe routes.IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode, draftId)
        }
      }

      "isTheSaleSubjectToConditions page must" - {
        "navigate to describeTheConditions when True" in {
          navigator.nextPage(
            IsTheSaleSubjectToConditionsPage,
            NormalMode,
            userAnswersWith(IsTheSaleSubjectToConditionsPage, true)
          ) mustBe routes.DescribeTheConditionsController.onPageLoad(NormalMode, draftId)
        }

        "navigate to DescriptionOfGoods when False" in {
          navigator.nextPage(
            IsTheSaleSubjectToConditionsPage,
            NormalMode,
            userAnswersWith(IsTheSaleSubjectToConditionsPage, false)
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
        }

        "navigate to itself when user has no data for the page" in {
          navigator.nextPage(
            IsTheSaleSubjectToConditionsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode, draftId)
        }
      }

      "describeTheConditions page must" - {
        "navigate to itself when user has no data for the page" in {
          navigator.nextPage(
            DescribeTheConditionsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.DescribeTheConditionsController.onPageLoad(NormalMode, draftId)
        }

        "navigate to DescriptionOfGoods when answers has data" in {
          navigator.nextPage(
            DescribeTheConditionsPage,
            NormalMode,
            userAnswersWith(DescribeTheConditionsPage, "Some conditions")
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
        }
      }

      "whyIdenticalGoods Page must" - {
        "navigate to HaveYouUsedMethodOneInPastPage" in {
          val userAnswers = userAnswersWith(WhyIdenticalGoodsPage, "banana")
          navigator.nextPage(
            WhyIdenticalGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HaveYouUsedMethodOneInPastController.onPageLoad(NormalMode, draftId)
        }

        "navigate to itself when user has no data for the page" in {
          navigator.nextPage(
            WhyIdenticalGoodsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.WhyIdenticalGoodsController.onPageLoad(NormalMode, draftId)
        }
      }

      "whyComputedValue Page must" - {
        "navigate go to explainReasonComputedValuePage when valid answer" in {
          val userAnswers = userAnswersWith(WhyComputedValuePage, "banana")
          navigator.nextPage(
            WhyComputedValuePage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainReasonComputedValueController.onPageLoad(NormalMode, draftId)
        }

        "navigate go to explainReasonComputedValuePage when invalid answer" in {
          val userAnswers = userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            WhyComputedValuePage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyComputedValueController.onPageLoad(NormalMode, draftId)
        }
      }

      "ExplainReasonComputedValuePage must" - {
        "navigate go to explainReasonComputedValuePage when valid answer" in {
          val userAnswers = userAnswersWith(ExplainReasonComputedValuePage, "banana")
          navigator.nextPage(
            ExplainReasonComputedValuePage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
        }

        "navigate go to explainReasonComputedValuePage when invalid answer" in {
          val userAnswers = userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            ExplainReasonComputedValuePage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainReasonComputedValueController.onPageLoad(NormalMode, draftId)
        }
      }

      // Method 6
      "explainWhyYouHaveNotSelectedMethodOneToFive Page must" - {
        "navigate to self when user has no data for the page" in {
          navigator.nextPage(
            ExplainWhyYouHaveNotSelectedMethodOneToFivePage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(
            mode = NormalMode,
            draftId
          )
        }

        "navigate to adaptMethodPage when user has data for the page" in {
          val userAnswers =
            userAnswersWith(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, "banana")
          navigator.nextPage(
            ExplainWhyYouHaveNotSelectedMethodOneToFivePage,
            NormalMode,
            userAnswers
          ) mustBe routes.AdaptMethodController.onPageLoad(NormalMode, draftId)
        }
      }

      "adaptMethod Page must" - {
        "navigate to self when user has no data for the page" in {
          navigator.nextPage(
            AdaptMethodPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.AdaptMethodController.onPageLoad(NormalMode, draftId)
        }

        "navigate to explainHowYouWillUseMethodSixPage when user has data for the page" in {
          val userAnswers = userAnswersWith(AdaptMethodPage, AdaptMethod.values.head)
          navigator.nextPage(
            AdaptMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainHowYouWillUseMethodSixController.onPageLoad(NormalMode, draftId)
        }
      }

      "explainHowYouWillUseMethodSix Page must" - {
        "navigate to self when user has no data for the page" in {
          navigator.nextPage(
            ExplainHowYouWillUseMethodSixPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ExplainHowYouWillUseMethodSixController.onPageLoad(NormalMode, draftId)
        }

        "navigate to descriptionOfTheGoodsPage when user has data for the page" in {
          val userAnswers = userAnswersWith(ExplainHowYouWillUseMethodSixPage, "banana")
          navigator.nextPage(
            ExplainHowYouWillUseMethodSixPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
        }
      }
    }

    "must go from Delete Draft to Account Home" in {

      navigator.nextPage(
        DeleteDraftPage,
        NormalMode,
        userAnswersAsIndividualTrader
      ) mustBe routes.AccountHomeController.onPageLoad()
    }
  }
}
