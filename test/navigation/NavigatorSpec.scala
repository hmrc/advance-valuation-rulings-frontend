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

import play.api.libs.json.Writes
import uk.gov.hmrc.auth.core.AffinityGroup._

import base.SpecBase
import controllers.routes
import models._
import models.WhatIsYourRoleAsImporter.Employeeoforg
import models.fileupload._
import pages._
import queries.Modifiable

class NavigatorSpec extends SpecBase {

  val EmptyUserAnswers: UserAnswers  = emptyUserAnswers
  val navigator                      = new Navigator
  val fileDetails: UpscanFileDetails = UpscanFileDetails(UploadId("id"), "name", "some.url")

  "Navigator" - {

    def userAnswersWith[A: Writes](page: Modifiable[A], value: A): UserAnswers =
      EmptyUserAnswers.set(page, value).success.value

    "must go from a page that doesn't exist in the route map to Index" in {
      case object UnknownPage extends Page
      navigator.nextPage(
        UnknownPage,
        NormalMode,
        EmptyUserAnswers
      ) mustBe routes.IndexController.onPageLoad
    }

    "Account Home" - {
      "should navigate to RequiredInformation page when Individual" in {
        navigator.startApplicationRouting(Individual) mustBe routes.RequiredInformationController
          .onPageLoad()

      }
      "should navigate to WhatIsYourRole page when Agent" in {
        navigator.startApplicationRouting(Agent) mustBe routes.WhatIsYourRoleAsImporterController
          .onPageLoad()

      }
      "should navigate to WhatIsYourRole page when Org" in {
        navigator.startApplicationRouting(
          Organisation
        ) mustBe routes.WhatIsYourRoleAsImporterController
          .onPageLoad()
      }
    }

    "in Normal mode" - {

      "WhatIsYourRoleAsImporterPage" - {

        "must navigate to RequiredInformationPage page" in {
          val userAnswers =
            userAnswersWith(WhatIsYourRoleAsImporterPage, Employeeoforg)
          navigator.nextPage(
            WhatIsYourRoleAsImporterPage,
            NormalMode,
            userAnswers
          ) mustBe routes.RequiredInformationController.onPageLoad()

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
          ) mustBe routes.HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(NormalMode)

        }
      }

      "HaveYouUsedMethodOneInPast page" - {

        "must navigate to describeTheIdenticalGoods Page when True" in {
          val ans = userAnswersWith(HaveYouUsedMethodOneInPastPage, true)
          navigator.nextPage(
            HaveYouUsedMethodOneInPastPage,
            NormalMode,
            ans
          ) mustBe routes.DescribeTheIdenticalGoodsController.onPageLoad(NormalMode)
        }

        "must navigate to willYouCompareGoodsToIdenticalGoods Page when False" in {
          val ans = userAnswersWith(HaveYouUsedMethodOneInPastPage, false)
          navigator.nextPage(
            HaveYouUsedMethodOneInPastPage,
            NormalMode,
            ans
          ) mustBe routes.WillYouCompareGoodsToIdenticalGoodsController.onPageLoad(NormalMode)
        }
      }

      "haveYouUsedMethodOneForSimilarGoodsInPast page" - {

        "must navigate to describeTheSimilarGoods Page when True" in {
          val ans = userAnswersWith(HaveYouUsedMethodOneForSimilarGoodsInPastPage, true)
          navigator.nextPage(
            HaveYouUsedMethodOneForSimilarGoodsInPastPage,
            NormalMode,
            ans
          ) mustBe routes.DescribeTheSimilarGoodsController.onPageLoad(NormalMode)
        }

        "must navigate to WillYouCompareToSimilarGoodsController Page when False" in {
          val ans = userAnswersWith(HaveYouUsedMethodOneForSimilarGoodsInPastPage, false)
          navigator.nextPage(
            HaveYouUsedMethodOneForSimilarGoodsInPastPage,
            NormalMode,
            ans
          ) mustBe routes.WillYouCompareToSimilarGoodsController.onPageLoad(NormalMode)

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
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode)
        }
      }

      "WillYouCompareGoodsToSimilarGoods page" - {
        "must navigate to ExplainYourGoodsComparingToSimilarGoods Page when True" in {
          val userAnswers =
            userAnswersWith(WillYouCompareToSimilarGoodsPage, true)
          navigator.nextPage(
            WillYouCompareToSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainYourGoodsComparingToSimilarGoodsController.onPageLoad(NormalMode)
        }

        "must navigate to ValuationMethod Page when False" in {
          val userAnswers =
            userAnswersWith(WillYouCompareToSimilarGoodsPage, false)
          navigator.nextPage(
            WillYouCompareToSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode)
        }
      }

      "ExplainYourGoodsComparingToSimilarGoods page" - {
        "must navigate to DescriptionOfGoods Page" in {
          val userAnswers =
            userAnswersWith(ExplainYourGoodsComparingToSimilarGoodsPage, "describe goods")
          navigator.nextPage(
            ExplainYourGoodsComparingToSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode)
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
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode)
        }
      }

      "WillYouCompareGoodsToIdenticalGoods page" - {
        "must navigate to explainYourGoodsComparingToIdenticalGoods Page when True" in {
          val userAnswers =
            userAnswersWith(WillYouCompareGoodsToIdenticalGoodsPage, true)
          navigator.nextPage(
            WillYouCompareGoodsToIdenticalGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainYourGoodsComparingToIdenticalGoodsController.onPageLoad(NormalMode)
        }

        "must navigate to ValuationMethod Page when False" in {
          val userAnswers =
            userAnswersWith(WillYouCompareGoodsToIdenticalGoodsPage, false)
          navigator.nextPage(
            WillYouCompareGoodsToIdenticalGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode)
        }
      }

      "ExplainYourGoodsComparingToIdenticalGoods page" - {
        "must navigate to DescriptionOfGoods Page" in {
          val userAnswers =
            userAnswersWith(ExplainYourGoodsComparingToIdenticalGoodsPage, "describe goods")
          navigator.nextPage(
            ExplainYourGoodsComparingToIdenticalGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode)
        }
      }

      "RequiredInformationPage must" - {
        "navigate to Import goods page when all values are set" in {
          val userAnswers =
            userAnswersWith(RequiredInformationPage, RequiredInformation.values.toSet)
          navigator.nextPage(
            RequiredInformationPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ImportGoodsController.onPageLoad(mode = NormalMode)
        }

        "navigate to self when no values are set" in {
          navigator.nextPage(
            RequiredInformationPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.RequiredInformationController.onPageLoad()
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
          ) mustBe routes.ExplainWhyYouChoseMethodFourController.onPageLoad(NormalMode)
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
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode)
        }
      }

      "ImportGoodsPage must" - {

        "navigate to ContactPage page when True" in {
          val userAnswers = userAnswersWith(ImportGoodsPage, true)
          navigator.nextPage(
            ImportGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ContactPageController.onPageLoad()
        }

        "and navigate to ImportingGoodsPage when False" in {
          val userAnswers = userAnswersWith(ImportGoodsPage, false)
          navigator.nextPage(
            ImportGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ImportingGoodsController.onPageLoad()
        }

        "navigate to ImportingGoodsPage when no value is set" in {
          navigator.nextPage(
            ImportGoodsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ImportGoodsController.onPageLoad(mode = NormalMode)
        }
      }

      "hasCommodityCodePage" - {
        "navigate to CommodityCode page when True" in {
          val userAnswers = userAnswersWith(HasCommodityCodePage, true)
          navigator.nextPage(
            HasCommodityCodePage,
            NormalMode,
            userAnswers
          ) mustBe routes.CommodityCodeController.onPageLoad(NormalMode)
        }

        "and navigate to HaveTheGoodsBeenSubjectToLegalChallenges when False" in {
          val userAnswers = userAnswersWith(HasCommodityCodePage, false)
          navigator.nextPage(
            HasCommodityCodePage,
            NormalMode,
            userAnswers
          ) mustBe routes.HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(NormalMode)
        }

      }

      "HaveTheGoodsBeenSubjectToLegalChallenges page" - {
        "navigate to DescribeTheLegalChallenges when Yes" in {
          val userAnswers = userAnswersWith(HaveTheGoodsBeenSubjectToLegalChallengesPage, true)
          navigator.nextPage(
            HaveTheGoodsBeenSubjectToLegalChallengesPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescribeTheLegalChallengesController.onPageLoad(mode = NormalMode)
        }

        "and navigate to HasConfidentialInformation when No" in {
          val userAnswers =
            userAnswersWith(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          navigator.nextPage(
            HaveTheGoodsBeenSubjectToLegalChallengesPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HasConfidentialInformationController.onPageLoad(mode = NormalMode)
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
          ) mustBe routes.HasConfidentialInformationController.onPageLoad(mode = NormalMode)
        }

        "navigate to self when no values are set" in {
          navigator.nextPage(
            DescribeTheLegalChallengesPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.DescribeTheLegalChallengesController.onPageLoad(NormalMode)
        }
      }

      "HasConfidentialInformation page" - {
        "navigate to DescribeTheLegalChallenges when Yes" in {
          val userAnswers = userAnswersWith(HasConfidentialInformationPage, true)
          navigator.nextPage(
            HasConfidentialInformationPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ConfidentialInformationController.onPageLoad(mode = NormalMode)
        }

        "and navigate to DoYouWantToUploadDocuments when No" in {
          val userAnswers = userAnswersWith(HasConfidentialInformationPage, false)
          navigator.nextPage(
            HasConfidentialInformationPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(mode = NormalMode)

        }
      }

      "ConfidentialInformation page" - {
        "navigate to DoYouWantToUploadDocuments page when all values are set" in {
          val userAnswers =
            userAnswersWith(ConfidentialInformationPage, "top secret")
          navigator.nextPage(
            ConfidentialInformationPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(mode = NormalMode)
        }

        "navigate to self when no values are set" in {
          navigator.nextPage(
            ConfidentialInformationPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ConfidentialInformationController.onPageLoad(NormalMode)
        }
      }

      "CheckRegisteredDetailsPage must" - {
        val data = CheckRegisteredDetails(
          value = true,
          eori = "GB1234567890",
          name = "name",
          streetAndNumber = "street",
          city = "city",
          postalCode = Some("postcode"),
          country = "GB"
        )

        "navigate to ApplicationContactDetailsPage when Yes" in {
          val userAnswers =
            userAnswersWith(CheckRegisteredDetailsPage, value = data)
          navigator.nextPage(
            CheckRegisteredDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ApplicationContactDetailsController.onPageLoad(mode = NormalMode)
        }

        "and navigate to EORIBeUpToDatePage when No" in {
          val userAnswers =
            userAnswersWith(CheckRegisteredDetailsPage, value = data.copy(value = false))
          navigator.nextPage(
            CheckRegisteredDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.EORIBeUpToDateController.onPageLoad()
        }
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
        ) mustBe routes.ValuationMethodController.onPageLoad(mode = NormalMode)
      }

      "DoYouWantToUploadDocumentsPage must" - {
        "self when no method is select" in {
          navigator.nextPage(
            DoYouWantToUploadDocumentsPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(mode = NormalMode)
        }

        "UploadSupportingDocumentsPage when Yes is selected" in {
          val userAnswers =
            emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, true).get
          navigator.nextPage(
            DoYouWantToUploadDocumentsPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.routes.UploadSupportingDocumentsController
            .onPageLoad(None, None, None, NormalMode)
        }

        "CheckYourAnswers page when No is selected" in {
          val userAnswers =
            emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, false).get
          navigator.nextPage(
            DoYouWantToUploadDocumentsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      "UploadAnotherSupportingDocumentPage must" - {
        "self when no answer is selected" in {
          navigator.nextPage(
            UploadAnotherSupportingDocumentPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe routes.UploadAnotherSupportingDocumentController.onPageLoad(mode = NormalMode)
        }

        "UploadSupportingDocumentsPage when Yes is selected" in {
          val userAnswers =
            emptyUserAnswers.set(UploadAnotherSupportingDocumentPage, true).get
          navigator.nextPage(
            UploadAnotherSupportingDocumentPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.routes.UploadSupportingDocumentsController
            .onPageLoad(None, None, None, NormalMode)
        }

        "CheckYourAnswers page when No is selected" in {
          val userAnswers =
            emptyUserAnswers.set(UploadAnotherSupportingDocumentPage, false).get
          navigator.nextPage(
            UploadAnotherSupportingDocumentPage,
            NormalMode,
            userAnswers
          ) mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      "IsThisFileConfidentialPage must" - {

        "redirect to UploadSupportingDocumentsPage user has no files" in {
          navigator.nextPage(
            IsThisFileConfidentialPage,
            NormalMode,
            emptyUserAnswers
          ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(mode = NormalMode)
        }

        "redirect to self when user has a file without confidentiality info" in {
          val userAnswers = emptyUserAnswers
            .set(
              UploadSupportingDocumentPage,
              UploadedFiles.initialise(fileDetails)
            )
            .get
          navigator.nextPage(
            IsThisFileConfidentialPage,
            NormalMode,
            userAnswers
          ) mustBe routes.IsThisFileConfidentialController.onPageLoad(mode = NormalMode)
        }

        "UploadSupportingDocumentsPage when an answer is selected" in {
          val userAnswers = emptyUserAnswers
            .set(
              UploadSupportingDocumentPage,
              UploadedFiles.initialise(fileDetails).setConfidentiality(false)
            )
            .get
          navigator.nextPage(
            IsThisFileConfidentialPage,
            NormalMode,
            userAnswers
          ) mustBe routes.UploadAnotherSupportingDocumentController.onPageLoad(NormalMode)
        }
      }

      "valuationMethod page must navigate to" - {
        "self when no method is selected" in {
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ValuationMethodController.onPageLoad(mode = NormalMode)
        }

        "isThereASaleInvolved page when method 1 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method1)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.IsThereASaleInvolvedController.onPageLoad(mode = NormalMode)
        }

        "WhyIdenticalGoods page when method 2 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method2)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyIdenticalGoodsController.onPageLoad(mode = NormalMode)
        }

        "WhyTransactionValueOfSimilarGoods page when method 3 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method3)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyTransactionValueOfSimilarGoodsController.onPageLoad(mode = NormalMode)
        }

        "ExplainWhyYouHaveNotSelectedMethodOneToThree page when method 4 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method4)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(mode =
            NormalMode
          )
        }

        "WhyComputedValue page when method 5 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method5)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyComputedValueController.onPageLoad(mode = NormalMode)
        }

        "ExplainWhyYouHaveNotSelectedMethodOneToFiveController page when method 6 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method6)
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(mode =
            NormalMode
          )
        }
      }

      "isThereASaleInvolved must" - {
        "navigate to IsSaleBetweenRelatedParties page when yes" in {
          navigator.nextPage(
            IsThereASaleInvolvedPage,
            NormalMode,
            emptyUserAnswers.set(IsThereASaleInvolvedPage, true).success.value
          ) mustBe routes.IsSaleBetweenRelatedPartiesController.onPageLoad(NormalMode)
        }
        "navigate to valuationMethod page when no" in {
          navigator.nextPage(
            IsThereASaleInvolvedPage,
            NormalMode,
            emptyUserAnswers.set(IsThereASaleInvolvedPage, false).success.value
          ) mustBe routes.ValuationMethodController.onPageLoad(NormalMode)
        }
      }

      "IsSaleBetweenRelatedParties page must" - {
        "navigate to ExplainHowPartiesAreRelated page when yes" in {
          navigator.nextPage(
            IsSaleBetweenRelatedPartiesPage,
            NormalMode,
            emptyUserAnswers
              .set(IsSaleBetweenRelatedPartiesPage, true)
              .success
              .value
          ) mustBe routes.ExplainHowPartiesAreRelatedController.onPageLoad(NormalMode)
        }
        "navigate to restrictions page when no" in {
          navigator.nextPage(
            IsSaleBetweenRelatedPartiesPage,
            NormalMode,
            emptyUserAnswers
              .set(IsSaleBetweenRelatedPartiesPage, false)
              .success
              .value
          ) mustBe routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode)
        }
      }

      "ExplainHowPartiesAreRelated page must" - {
        "navigate to 'restrictions' page" in {
          navigator.nextPage(
            ExplainHowPartiesAreRelatedPage,
            NormalMode,
            emptyUserAnswers
              .set(ExplainHowPartiesAreRelatedPage, "explain")
              .success
              .value
          ) mustBe routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode)
        }
      }

      "HasCommodityCodePage must" - {
        "navigate to CommodityCode when yes" in {
          navigator.nextPage(
            HasCommodityCodePage,
            NormalMode,
            userAnswersWith(HasCommodityCodePage, true)
          ) mustBe routes.CommodityCodeController.onPageLoad(NormalMode)
        }
      }

      "CommodityCode must" - {
        "navigate to WhatCountryAreGoodsFrom when set" in {
          navigator.nextPage(
            CommodityCodePage,
            NormalMode,
            userAnswersWith(CommodityCodePage, "1234567890")
          ) mustBe routes.HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(NormalMode)
        }
      }

      "areThereRestrictionsOnTheGoods page must" - {
        "navigate to DescribeTheRestrictions when True" in {
          navigator.nextPage(
            AreThereRestrictionsOnTheGoodsPage,
            NormalMode,
            userAnswersWith(AreThereRestrictionsOnTheGoodsPage, true)
          ) mustBe routes.DescribeTheRestrictionsController.onPageLoad(NormalMode)
        }

        "navigate to IsTheSaleSubjectToConditions when False" in {
          navigator.nextPage(
            AreThereRestrictionsOnTheGoodsPage,
            NormalMode,
            userAnswersWith(AreThereRestrictionsOnTheGoodsPage, false)
          ) mustBe routes.IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode)
        }

        "navigate to itself when user has no data for the page" in {
          navigator.nextPage(
            AreThereRestrictionsOnTheGoodsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode)
        }
      }

      "describeTheRestrictions page must" - {
        "navigate to itself when user has no data for the page" in {
          navigator.nextPage(
            DescribeTheRestrictionsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.DescribeTheRestrictionsController.onPageLoad(NormalMode)
        }

        "navigate to IsTheSaleSubjectToConditions when answers has data" in {
          navigator.nextPage(
            DescribeTheRestrictionsPage,
            NormalMode,
            userAnswersWith(DescribeTheRestrictionsPage, "Some restrictions")
          ) mustBe routes.IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode)
        }
      }

      "isTheSaleSubjectToConditions page must" - {
        "navigate to describeTheConditions when True" in {
          navigator.nextPage(
            IsTheSaleSubjectToConditionsPage,
            NormalMode,
            userAnswersWith(IsTheSaleSubjectToConditionsPage, true)
          ) mustBe routes.DescribeTheConditionsController.onPageLoad(NormalMode)
        }

        "navigate to DescriptionOfGoods when False" in {
          navigator.nextPage(
            IsTheSaleSubjectToConditionsPage,
            NormalMode,
            userAnswersWith(IsTheSaleSubjectToConditionsPage, false)
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode)
        }

        "navigate to itself when user has no data for the page" in {
          navigator.nextPage(
            IsTheSaleSubjectToConditionsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode)
        }
      }

      "describeTheConditions page must" - {
        "navigate to itself when user has no data for the page" in {
          navigator.nextPage(
            DescribeTheConditionsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.DescribeTheConditionsController.onPageLoad(NormalMode)
        }

        "navigate to DescriptionOfGoods when answers has data" in {
          navigator.nextPage(
            DescribeTheConditionsPage,
            NormalMode,
            userAnswersWith(DescribeTheConditionsPage, "Some conditions")
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(NormalMode)
        }
      }

      "whyIdenticalGoods Page must" - {
        "navigate to HaveYouUsedMethodOneInPastPage" in {
          val userAnswers = userAnswersWith(WhyIdenticalGoodsPage, "banana")
          navigator.nextPage(
            WhyIdenticalGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HaveYouUsedMethodOneInPastController.onPageLoad(mode = NormalMode)
        }

        "navigate to itself when user has no data for the page" in {
          navigator.nextPage(
            WhyIdenticalGoodsPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.WhyIdenticalGoodsController.onPageLoad(mode = NormalMode)
        }
      }

      "whyComputedValue Page must" - {
        "navigate go to explainReasonComputedValuePage" in {
          val userAnswers = userAnswersWith(WhyComputedValuePage, "banana")
          navigator.nextPage(
            WhyComputedValuePage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainReasonComputedValueController.onPageLoad(mode = NormalMode)
        }
      }

      // Method 6
      "explainWhyYouHaveNotSelectedMethodOneToFive Page must" - {
        "navigate to self when user has no data for the page" in {
          navigator.nextPage(
            ExplainWhyYouHaveNotSelectedMethodOneToFivePage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(mode =
            NormalMode
          )
        }

        "navigate to adaptMethodPage when user has data for the page" in {
          val userAnswers =
            userAnswersWith(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, "banana")
          navigator.nextPage(
            ExplainWhyYouHaveNotSelectedMethodOneToFivePage,
            NormalMode,
            userAnswers
          ) mustBe routes.AdaptMethodController.onPageLoad(mode = NormalMode)
        }
      }

      "adaptMethod Page must" - {
        "navigate to self when user has no data for the page" in {
          navigator.nextPage(
            AdaptMethodPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.AdaptMethodController.onPageLoad(mode = NormalMode)
        }

        "navigate to explainHowYouWillUseMethodSixPage when user has data for the page" in {
          val userAnswers = userAnswersWith(AdaptMethodPage, AdaptMethod.values.head)
          navigator.nextPage(
            AdaptMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainHowYouWillUseMethodSixController.onPageLoad(mode = NormalMode)
        }
      }

      "explainHowYouWillUseMethodSix Page must" - {
        "navigate to self when user has no data for the page" in {
          navigator.nextPage(
            ExplainHowYouWillUseMethodSixPage,
            NormalMode,
            EmptyUserAnswers
          ) mustBe routes.ExplainHowYouWillUseMethodSixController.onPageLoad(mode = NormalMode)
        }

        "navigate to descriptionOfTheGoodsPage when user has data for the page" in {
          val userAnswers = userAnswersWith(ExplainHowYouWillUseMethodSixPage, "banana")
          navigator.nextPage(
            ExplainHowYouWillUseMethodSixPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DescriptionOfGoodsController.onPageLoad(mode = NormalMode)
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          EmptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad
      }
    }

  }
}
