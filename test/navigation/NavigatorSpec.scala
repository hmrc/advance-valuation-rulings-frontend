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

package navigation

import java.time.Instant

import play.api.libs.json.Writes
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import controllers.routes
import models._
import models.AuthUserType.{Agent, IndividualTrader, OrganisationAdmin, OrganisationAssistant}
import models.WhatIsYourRoleAsImporter.{AgentOnBehalfOfOrg, EmployeeOfOrg}
import pages._
import queries._

class NavigatorSpec extends SpecBase {

  val EmptyUserAnswers: UserAnswers = userAnswersAsIndividualTrader
  val navigator                     = new Navigator

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

      def redirectRoute = routes.AccountHomeController.onPageLoad()

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

      "should navigate to RequiredInformation page for an OrganisationAdmin" in {
        navigator.nextPage(
          AccountHomePage,
          NormalMode,
          userAnswersAsIndividualTrader.setFuture(AccountHomePage, OrganisationAdmin).futureValue
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

      "WhatIsYourRoleAsImporterPage" - {

        "must navigate to RequiredInformationPage page" in {
          val userAnswers =
            userAnswersWith(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
          navigator.nextPage(
            WhatIsYourRoleAsImporterPage,
            NormalMode,
            userAnswers
          ) mustBe routes.RequiredInformationController.onPageLoad(draftId)

        }
      }

      "WhyTransactionValueOfSimilarGoods page" - {

        "must navigate to HaveYouUsedMethodOneInPast page" in {
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
        "must navigate to WillYouCompareToSimilarGoods Page" in {
          val userAnswers =
            userAnswersWith(DescribeTheSimilarGoodsPage, "similar goods")
          navigator.nextPage(
            DescribeTheSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
        }
      }

      "DescribeTheIdenticalGoods page" - {
        "must navigate to DescriptionOfGoods Page" in {
          val userAnswers =
            userAnswersWith(DescribeTheIdenticalGoodsPage, "describe goods")
          navigator.nextPage(
            DescribeTheIdenticalGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
        }
      }

      "ExplainWhyYouHaveNotSelectedMethodOneToThree page" - {
        "must navigate to ExplainWhyYouChoseMethodFour Page" in {
          val userAnswers =
            userAnswersWith(ExplainWhyYouHaveNotSelectedMethodOneToThreePage, "explain method four")
          navigator.nextPage(
            ExplainWhyYouHaveNotSelectedMethodOneToThreePage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainWhyYouChoseMethodFourController.onPageLoad(NormalMode, draftId)
        }
      }

      "ExplainWhyYouChoseMethodFour page" - {
        "must navigate to DescriptionOfGoods Page" in {
          val userAnswers =
            userAnswersWith(ExplainWhyYouChoseMethodFourPage, "explain method four")
          navigator.nextPage(
            ExplainWhyYouChoseMethodFourPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode, draftId)
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

      "hasCommodityCodePage" - {
        "navigate to CommodityCode page when True" in {
          val userAnswers = userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            HasCommodityCodePage,
            NormalMode,
            userAnswers
          ) mustBe routes.CommodityCodeController.onPageLoad(NormalMode, draftId)
        }

        "and navigate to HaveTheGoodsBeenSubjectToLegalChallenges when False" in {
          val userAnswers = userAnswersWith(HasCommodityCodePage, false)
          navigator.nextPage(
            HasCommodityCodePage,
            NormalMode,
            userAnswers
          ) mustBe routes.HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(
            NormalMode,
            draftId
          )
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

        "when Individual" - {

          "navigate to ApplicationContactDetailsPage when Yes" in {
            val userAnswers = userAnswersAsIndividualTrader
              .setFuture(CheckRegisteredDetailsPage, value = true)
              .futureValue

            navigator.nextPage(
              CheckRegisteredDetailsPage,
              NormalMode,
              userAnswers
            ) mustBe routes.ApplicationContactDetailsController.onPageLoad(NormalMode, draftId)
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

        "when OrganisationAdmin" - {

          "navigate to ApplicationContactDetailsController when Yes" in {

            val userAnswers = userAnswersAsOrgAdmin
              .setFuture(CheckRegisteredDetailsPage, value = true)
              .futureValue

            navigator.nextPage(
              CheckRegisteredDetailsPage,
              NormalMode,
              userAnswers
            ) mustBe routes.ApplicationContactDetailsController.onPageLoad(NormalMode, draftId)
          }
        }

        "when OrganisationAssistant" - {

          "navigate to BusinessContactDetailsPage when Yes" in {

            val userAnswers = userAnswersAsOrgAssistant
              .setFuture(CheckRegisteredDetailsPage, value = true)
              .futureValue

            navigator.nextPage(
              CheckRegisteredDetailsPage,
              NormalMode,
              userAnswers
            ) mustBe routes.BusinessContactDetailsController.onPageLoad(NormalMode, draftId)
          }
        }
      }

      "BusinessContactDetailsPage must" - {
        val userAnswers =
          userAnswersWith(
            BusinessContactDetailsPage,
            BusinessContactDetails("name", "email", "phone")
          )

        "when OrganisationAssistant claiming to be AgentOnBehalfOfOrg" - {
          "navigate to AgentCompanyDetailsPage when Yes" in {
            val ua = userAnswers
              .set(AccountHomePage, value = OrganisationAssistant)
              .flatMap(_.set(WhatIsYourRoleAsImporterPage, AgentOnBehalfOfOrg))
              .success
              .value

            navigator.nextPage(
              BusinessContactDetailsPage,
              NormalMode,
              ua
            ) mustBe routes.AgentCompanyDetailsController.onPageLoad(NormalMode, draftId)
          }
        }

        "when OrganisationAssistant claiming to be EmployeeOfOrg" - {
          "navigate to ValuationMethodPage when Yes" in {
            val ua = userAnswers
              .set(AccountHomePage, value = OrganisationAssistant)
              .flatMap(_.set(WhatIsYourRoleAsImporterPage, EmployeeOfOrg))
              .success
              .value

            navigator.nextPage(
              BusinessContactDetailsPage,
              NormalMode,
              ua
            ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode, draftId)
          }
        }

        "when OrganisationAssistant without Importer role" - {
          "navigate to WhatIsYourRoleAsImporterPage when Yes" in {
            val ua = userAnswers
              .set(AccountHomePage, value = OrganisationAssistant)
              .success
              .value

            navigator.nextPage(
              BusinessContactDetailsPage,
              NormalMode,
              ua
            ) mustBe routes.WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, draftId)
          }
        }

        "when an OrganisationAdmin" - {
          "navigate to valuation method page" in {

            navigator.nextPage(
              BusinessContactDetailsPage,
              NormalMode,
              userAnswers.set(AccountHomePage, value = OrganisationAdmin).success.value
            ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode, draftId)
          }
        }

        "when an IndividualTrader" - {
          "navigate to JourneyRecovery page" in {

            navigator.nextPage(
              BusinessContactDetailsPage,
              NormalMode,
              userAnswers.set(AccountHomePage, value = IndividualTrader).success.value
            ) mustBe routes.UnauthorisedController.onPageLoad
          }
        }

        "when ApplicantUserType is missing from user answers" - {
          "navigate to JourneyRecovery page" in {

            navigator.nextPage(
              BusinessContactDetailsPage,
              NormalMode,
              userAnswers
            ) mustBe routes.UnauthorisedController.onPageLoad
          }
        }
      }

      "AgentCompanyDetailsPage must" in {
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
        ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode, draftId)
      }

      "ApplicationContactDetailsPage must" in {
        val userAnswers =
          userAnswersWith(
            ApplicationContactDetailsPage,
            ApplicationContactDetails("name", "email", "phone")
          )
        navigator.nextPage(
          ApplicationContactDetailsPage,
          NormalMode,
          userAnswers
        ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode, draftId)
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
            userAnswersAsOrgAdmin.set(UploadAnotherSupportingDocumentPage, false).get
          navigator.nextPage(
            UploadAnotherSupportingDocumentPage,
            NormalMode,
            userAnswers
          ) mustBe routes.CheckYourAnswersForAgentsController.onPageLoad(
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
          ) mustBe routes.CheckYourAnswersForAgentsController.onPageLoad(
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
        "self when no method is selected" in {
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode, draftId)
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
        "navigate to 'restrictions' page" in {
          navigator.nextPage(
            ExplainHowPartiesAreRelatedPage,
            NormalMode,
            userAnswersAsIndividualTrader
              .set(ExplainHowPartiesAreRelatedPage, "explain")
              .success
              .value
          ) mustBe routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode, draftId)
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
      }

      "CommodityCode must" - {
        "navigate to WhatCountryAreGoodsFrom when set" in {
          navigator.nextPage(
            CommodityCodePage,
            NormalMode,
            userAnswersWith(CommodityCodePage, "1234567890")
          ) mustBe routes.HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(
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
        "navigate go to explainReasonComputedValuePage" in {
          val userAnswers = userAnswersWith(WhyComputedValuePage, "banana")
          navigator.nextPage(
            WhyComputedValuePage,
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

    "must go from ContactPage to CheckRegisteredDetails" in {

      navigator.nextPage(
        ContactPagePage,
        NormalMode,
        userAnswersAsIndividualTrader
      ) mustBe routes.CheckRegisteredDetailsController.onPageLoad(NormalMode, draftId)
    }
  }
}
