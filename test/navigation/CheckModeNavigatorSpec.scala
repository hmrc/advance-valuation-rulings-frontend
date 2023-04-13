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
import uk.gov.hmrc.auth.core.AffinityGroup

import base.SpecBase
import controllers.routes
import models._
import models.fileupload._
import pages._
import queries.Modifiable

class CheckModeNavigatorSpec extends SpecBase {

  val EmptyUserAnswers: UserAnswers  = emptyUserAnswers
  val navigator                      = new Navigator
  val fileDetails: UpscanFileDetails =
    UpscanFileDetails(UploadId("id"), "name", "some.url", "txt", 1L)
  val uploadedFile: UploadedFile     = UploadedFile(
    fileDetails.fileName,
    fileDetails.downloadUrl,
    isConfidential = false,
    fileDetails.mimeType,
    fileDetails.size
  )
  val checkYourAnswers               = routes.CheckYourAnswersController.onPageLoad

  "Navigator" - {

    def userAnswersWith[A: Writes](page: Modifiable[A], value: A): UserAnswers =
      EmptyUserAnswers.set(page, value).success.value

    implicit val affinityGroup: AffinityGroup.Individual.type = (AffinityGroup.Individual)

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          EmptyUserAnswers
        ) mustBe checkYourAnswers
      }

      "ValuationMethod page" - {
        "isThereASaleInvolved page when method 1 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method1)
          navigator.nextPage(
            ValuationMethodPage,
            CheckMode,
            userAnswers
          ) mustBe routes.IsThereASaleInvolvedController.onPageLoad(mode = CheckMode)
        }

        "WhyIdenticalGoods page when method 2 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method2)
          navigator.nextPage(
            ValuationMethodPage,
            CheckMode,
            userAnswers
          ) mustBe routes.WhyIdenticalGoodsController.onPageLoad(mode = CheckMode)
        }

        "WhyTransactionValueOfSimilarGoods page when method 3 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method3)
          navigator.nextPage(
            ValuationMethodPage,
            CheckMode,
            userAnswers
          ) mustBe routes.WhyTransactionValueOfSimilarGoodsController.onPageLoad(mode = CheckMode)
        }

        "ExplainWhyYouHaveNotSelectedMethodOneToThree page when method 4 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method4)
          navigator.nextPage(
            ValuationMethodPage,
            CheckMode,
            userAnswers
          ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(mode =
            CheckMode
          )
        }

        "WhyComputedValue page when method 5 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method5)
          navigator.nextPage(
            ValuationMethodPage,
            CheckMode,
            userAnswers
          ) mustBe routes.WhyComputedValueController.onPageLoad(mode = CheckMode)
        }

        "ExplainWhyYouHaveNotSelectedMethodOneToFiveController page when method 6 is selected" in {
          val userAnswers =
            userAnswersWith(ValuationMethodPage, ValuationMethod.Method6)
          navigator.nextPage(
            ValuationMethodPage,
            CheckMode,
            userAnswers
          ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(mode =
            CheckMode
          )
        }
      }
      // Method one pages
      "Method One Navigation" - {
        "isThereASaleInvolved must" - {
          "navigate to self when user has no data for the page" in {
            navigator.nextPage(
              IsThereASaleInvolvedPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.IsThereASaleInvolvedController.onPageLoad(mode = CheckMode)
          }

          "navigate to IsSaleBetweenRelatedParties page when yes" in {
            val userAnswers =
              userAnswersWith(IsThereASaleInvolvedPage, true)
            navigator.nextPage(
              IsThereASaleInvolvedPage,
              CheckMode,
              userAnswers
            ) mustBe routes.IsSaleBetweenRelatedPartiesController.onPageLoad(mode = CheckMode)
          }

          "navigate to valuationMethod page when no" in {
            val userAnswers =
              userAnswersWith(IsThereASaleInvolvedPage, false)
            navigator.nextPage(
              IsThereASaleInvolvedPage,
              CheckMode,
              userAnswers
            ) mustBe routes.ValuationMethodController.onPageLoad(mode = CheckMode)
          }

          "navigate to checkYourAnswers if yes and user has answered all other questions" in {
            val userAnswers = (for {
              ua <- EmptyUserAnswers.set(IsThereASaleInvolvedPage, true)
              ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
              ua <- ua.set(ExplainHowPartiesAreRelatedPage, "test")
              ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
              ua <- ua.set(DescribeTheRestrictionsPage, "test")
              ua <- ua.set(IsTheSaleSubjectToConditionsPage, true)
              ua <- ua.set(DescribeTheConditionsPage, "test")
            } yield ua).success.value

            navigator.nextPage(
              IsThereASaleInvolvedPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }

          "navigate to checkYourAnswers if yes and user has answered other questions" in {
            val userAnswers = (for {
              ua <- EmptyUserAnswers.set(IsThereASaleInvolvedPage, true)
              ua <- ua.set(IsSaleBetweenRelatedPartiesPage, false)
              ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, false)
              ua <- ua.set(IsTheSaleSubjectToConditionsPage, false)
            } yield ua).success.value

            navigator.nextPage(
              IsThereASaleInvolvedPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "isSaleBetweenRelatedParties must" - {
          "navigate to self when user has no data for the page" in {
            navigator.nextPage(
              IsSaleBetweenRelatedPartiesPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.IsSaleBetweenRelatedPartiesController.onPageLoad(mode = CheckMode)
          }

          "navigate to ExplainHowPartiesAreRelated page when yes" in {
            val userAnswers =
              userAnswersWith(IsSaleBetweenRelatedPartiesPage, true)
            navigator.nextPage(
              IsSaleBetweenRelatedPartiesPage,
              CheckMode,
              userAnswers
            ) mustBe routes.ExplainHowPartiesAreRelatedController.onPageLoad(mode = CheckMode)
          }

          "navigate to AreThereRestrictionsOnTheGoods page when no" in {
            val userAnswers =
              userAnswersWith(IsSaleBetweenRelatedPartiesPage, false)
            navigator.nextPage(
              IsSaleBetweenRelatedPartiesPage,
              CheckMode,
              userAnswers
            ) mustBe routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(mode = CheckMode)
          }
        }

        "ExplainHowPartiesAreRelated page must" - {
          "navigate to 'restrictions' page when user has data for the page" in {
            navigator.nextPage(
              ExplainHowPartiesAreRelatedPage,
              CheckMode,
              emptyUserAnswers
                .set(ExplainHowPartiesAreRelatedPage, "explain")
                .success
                .value
            ) mustBe routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(CheckMode)
          }
        }

        "AreThereRestrictionsOnTheGoods page must" - {
          "navigate to itself when user has no data for the page" in {
            navigator.nextPage(
              AreThereRestrictionsOnTheGoodsPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(CheckMode)
          }
        }

        "describeTheRestrictions page must" - {
          "navigate to itself when user has no data for the page" in {
            navigator.nextPage(
              DescribeTheRestrictionsPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.DescribeTheRestrictionsController.onPageLoad(CheckMode)
          }

          "navigate to IsTheSaleSubjectToConditions when answers has data" in {
            navigator.nextPage(
              DescribeTheRestrictionsPage,
              CheckMode,
              userAnswersWith(DescribeTheRestrictionsPage, "Some restrictions")
            ) mustBe routes.IsTheSaleSubjectToConditionsController.onPageLoad(CheckMode)
          }
        }

        "isTheSaleSubjectToConditions page must" - {
          "navigate to describeTheConditions when True" in {
            navigator.nextPage(
              IsTheSaleSubjectToConditionsPage,
              CheckMode,
              userAnswersWith(IsTheSaleSubjectToConditionsPage, true)
            ) mustBe routes.DescribeTheConditionsController.onPageLoad(CheckMode)
          }

          "navigate to checkYourAnswers when False" in {
            navigator.nextPage(
              IsTheSaleSubjectToConditionsPage,
              CheckMode,
              userAnswersWith(IsTheSaleSubjectToConditionsPage, false)
            ) mustBe checkYourAnswers
          }

          "navigate to itself when user has no data for the page" in {
            navigator.nextPage(
              IsTheSaleSubjectToConditionsPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.IsTheSaleSubjectToConditionsController.onPageLoad(CheckMode)
          }
        }

        "describeTheConditions page must" - {
          "navigate to itself when user has no data for the page" in {
            navigator.nextPage(
              DescribeTheConditionsPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.DescribeTheConditionsController.onPageLoad(CheckMode)
          }

          "navigate to checkYourAnswers when answer has data" in {
            navigator.nextPage(
              DescribeTheConditionsPage,
              CheckMode,
              userAnswersWith(DescribeTheConditionsPage, "Some conditions")
            ) mustBe checkYourAnswers
          }
        }
      }

      // Method two pages
      "Method Two Navigation" - {
        "whyIdenticalGoods Page must" - {
          "navigate to self when user has no data for the page" in {
            navigator.nextPage(
              WhyIdenticalGoodsPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.WhyIdenticalGoodsController.onPageLoad(mode = CheckMode)
          }

          "navigate to HaveYouUsedMethodOneInPastPage" in {
            val userAnswers = userAnswersWith(WhyIdenticalGoodsPage, "reason")
            navigator.nextPage(
              WhyIdenticalGoodsPage,
              CheckMode,
              userAnswers
            ) mustBe routes.HaveYouUsedMethodOneInPastController.onPageLoad(mode = CheckMode)
          }

          "navigate to checkYourAnswers when user has answers for remaining pages" in {
            val userAnswers = (for {
              ua <- EmptyUserAnswers.set(WhyIdenticalGoodsPage, "reason")
              ua <- ua.set(HaveYouUsedMethodOneInPastPage, true)
              ua <- ua.set(DescribeTheIdenticalGoodsPage, "reason")
            } yield ua).success.value

            navigator.nextPage(
              WhyIdenticalGoodsPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "HaveYouUsedMethodOneInPast page" - {
          "must navigate to self when user has no data for the page" in {
            navigator.nextPage(
              HaveYouUsedMethodOneInPastPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.HaveYouUsedMethodOneInPastController.onPageLoad(mode = CheckMode)
          }

          "must navigate to describeTheIdenticalGoods Page when True" in {
            val ans = userAnswersWith(HaveYouUsedMethodOneInPastPage, true)
            navigator.nextPage(
              HaveYouUsedMethodOneInPastPage,
              CheckMode,
              ans
            ) mustBe routes.DescribeTheIdenticalGoodsController.onPageLoad(CheckMode)
          }

          "must navigate to valuationMethod Page when False" in {
            val ans = userAnswersWith(HaveYouUsedMethodOneInPastPage, false)
            navigator.nextPage(
              HaveYouUsedMethodOneInPastPage,
              CheckMode,
              ans
            ) mustBe routes.ValuationMethodController.onPageLoad(CheckMode)
          }
        }

        "DescribeTheIdenticalGoods page" - {
          "must navigate to self when user has no data for the page" in {
            navigator.nextPage(
              DescribeTheIdenticalGoodsPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.DescribeTheIdenticalGoodsController.onPageLoad(mode = CheckMode)
          }
          "must navigate to checkYourAnswers Page when set" in {
            val userAnswers =
              userAnswersWith(DescribeTheIdenticalGoodsPage, "describe goods")
            navigator.nextPage(
              DescribeTheIdenticalGoodsPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }
      }

      // Method three pages
      "Method Three Navigation" - {
        "WhyTransactionValueOfSimilarGoods page" - {
          "must navigate to self when user has no data for the page" in {
            navigator.nextPage(
              WhyTransactionValueOfSimilarGoodsPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.WhyTransactionValueOfSimilarGoodsController.onPageLoad(mode = CheckMode)
          }

          "must navigate to haveYouUsedMethodOneForSimilarGoodsInPast page when user has data for the page" in {
            val userAnswers =
              userAnswersWith(WhyTransactionValueOfSimilarGoodsPage, "reason")
            navigator.nextPage(
              WhyTransactionValueOfSimilarGoodsPage,
              CheckMode,
              userAnswers
            ) mustBe routes.HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(
              mode = CheckMode
            )
          }

          "navigate to checkYourAnswers when user has answers for remaining pages" in {
            val userAnswers = (for {
              ua <- EmptyUserAnswers.set(WhyTransactionValueOfSimilarGoodsPage, "reason")
              ua <- ua.set(HaveYouUsedMethodOneForSimilarGoodsInPastPage, true)
              ua <- ua.set(DescribeTheSimilarGoodsPage, "reason")
            } yield ua).success.value

            navigator.nextPage(
              WhyTransactionValueOfSimilarGoodsPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "HaveYouUsedMethodOneForSimilarGoodsInPast page" - {
          "must navigate to self when user has no data for the page" in {
            navigator.nextPage(
              HaveYouUsedMethodOneForSimilarGoodsInPastPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(
              mode = CheckMode
            )
          }

          "must navigate to describeTheSimilarGoods Page when True" in {
            val ans = userAnswersWith(HaveYouUsedMethodOneForSimilarGoodsInPastPage, true)
            navigator.nextPage(
              HaveYouUsedMethodOneForSimilarGoodsInPastPage,
              CheckMode,
              ans
            ) mustBe routes.DescribeTheSimilarGoodsController.onPageLoad(CheckMode)
          }

          "must navigate to valuationMethod Page when False" in {
            val ans = userAnswersWith(HaveYouUsedMethodOneForSimilarGoodsInPastPage, false)
            navigator.nextPage(
              HaveYouUsedMethodOneForSimilarGoodsInPastPage,
              CheckMode,
              ans
            ) mustBe routes.ValuationMethodController.onPageLoad(CheckMode)
          }
        }

        "DescribeTheSimilarGoods page" - {
          "must navigate to self when user has no data for the page" in {
            navigator.nextPage(
              DescribeTheSimilarGoodsPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.DescribeTheSimilarGoodsController.onPageLoad(mode = CheckMode)
          }
          "must navigate to checkYourAnswers Page when set" in {
            val userAnswers =
              userAnswersWith(DescribeTheSimilarGoodsPage, "describe goods")
            navigator.nextPage(
              DescribeTheSimilarGoodsPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }
      }

      // Method four pages
      "Method Four Navigation" - {
        "explainWhyYouHaveNotSelectedMethodOneToThree Page must" - {
          "navigate to self when user has no data for the page" in {
            navigator.nextPage(
              ExplainWhyYouHaveNotSelectedMethodOneToThreePage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(mode =
              CheckMode
            )
          }

          "navigate to explainWhyYouChoseMethodFour page when user has data for the page" in {
            val userAnswers =
              userAnswersWith(ExplainWhyYouHaveNotSelectedMethodOneToThreePage, "reason")
            navigator.nextPage(
              ExplainWhyYouHaveNotSelectedMethodOneToThreePage,
              CheckMode,
              userAnswers
            ) mustBe routes.ExplainWhyYouChoseMethodFourController.onPageLoad(mode = CheckMode)
          }

          "navigate to checkYourAnswersPage when user has data for the remaining questions" in {
            val userAnswers =
              (for {
                ua <-
                  EmptyUserAnswers.set(ExplainWhyYouHaveNotSelectedMethodOneToThreePage, "reason")
                ua <- ua.set(ExplainWhyYouChoseMethodFourPage, "reason")
              } yield ua).success.value
            navigator.nextPage(
              ExplainWhyYouHaveNotSelectedMethodOneToThreePage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "explainWhyYouChoseMethodFour Page must" - {
          "navigate to self when user has no data for the page" in {
            navigator.nextPage(
              ExplainWhyYouChoseMethodFourPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.ExplainWhyYouChoseMethodFourController.onPageLoad(mode = CheckMode)
          }

          "navigate to checkYourAnswersPage when user has data for the page" in {
            val userAnswers =
              userAnswersWith(ExplainWhyYouChoseMethodFourPage, "reason")
            navigator.nextPage(
              ExplainWhyYouChoseMethodFourPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }
      }

      // Method five pages
      "Method Five Navigation" - {
        "whyComputedValue Page must" - {
          "navigate to self when user has no data for the page" in {
            navigator.nextPage(
              WhyComputedValuePage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.WhyComputedValueController.onPageLoad(mode = CheckMode)
          }

          "navigate to explainReasonComputedValuePage when user has data for the page" in {
            val userAnswers =
              userAnswersWith(WhyComputedValuePage, "reason")
            navigator.nextPage(
              WhyComputedValuePage,
              CheckMode,
              userAnswers
            ) mustBe routes.ExplainReasonComputedValueController.onPageLoad(mode = CheckMode)
          }

          "navigate to checkYourAnswersPage when user has data for the remaining questions" in {
            val userAnswers =
              (for {
                ua <- EmptyUserAnswers.set(WhyComputedValuePage, "reason")
                ua <- ua.set(ExplainReasonComputedValuePage, "reason")
              } yield ua).success.value
            navigator.nextPage(
              WhyComputedValuePage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "explainReasonComputedValue Page must" - {
          "navigate to self when user has no data for the page" in {
            navigator.nextPage(
              ExplainReasonComputedValuePage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.ExplainReasonComputedValueController.onPageLoad(mode = CheckMode)
          }

          "navigate to checkYourAnswersPage when user has data for the page" in {
            val userAnswers =
              userAnswersWith(ExplainReasonComputedValuePage, "reason")
            navigator.nextPage(
              ExplainReasonComputedValuePage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }
      }

      // Method six pages
      "Method Six Navigation" - {
        "explainWhyYouHaveNotSelectedMethodOneToFive Page must" - {
          "navigate to self when user has no data for the page" in {
            navigator.nextPage(
              ExplainWhyYouHaveNotSelectedMethodOneToFivePage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(mode =
              CheckMode
            )
          }

          "navigate to adaptMethodPage when user has data for the page" in {
            val userAnswers =
              userAnswersWith(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, "reason")
            navigator.nextPage(
              ExplainWhyYouHaveNotSelectedMethodOneToFivePage,
              CheckMode,
              userAnswers
            ) mustBe routes.AdaptMethodController.onPageLoad(mode = CheckMode)
          }

          "navigate to checkYourAnswersPage when user has data for the remaining questions" in {
            val userAnswers =
              (for {
                ua <- EmptyUserAnswers.set(AdaptMethodPage, AdaptMethod.values.head)
                ua <- ua.set(ExplainHowYouWillUseMethodSixPage, "reason")
                ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, "reason")
              } yield ua).success.value

            navigator.nextPage(
              ExplainWhyYouHaveNotSelectedMethodOneToFivePage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "adaptMethod Page must" - {
          "navigate to self when user has no data for the page" in {
            navigator.nextPage(
              AdaptMethodPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.AdaptMethodController.onPageLoad(mode = CheckMode)
          }

          "navigate to explainHowYouWillUseMethodSixPage when user has data for the page" in {
            val userAnswers = userAnswersWith(AdaptMethodPage, AdaptMethod.values.head)
            navigator.nextPage(
              AdaptMethodPage,
              CheckMode,
              userAnswers
            ) mustBe routes.ExplainHowYouWillUseMethodSixController.onPageLoad(mode = CheckMode)
          }

          "navigate to checkYourAnswersPage when user has data for the remaining questions" in {
            val userAnswers =
              (for {
                ua <- EmptyUserAnswers.set(AdaptMethodPage, AdaptMethod.values.head)
                ua <- ua.set(ExplainHowYouWillUseMethodSixPage, "reason")
                ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, "reason")
              } yield ua).success.value
            navigator.nextPage(
              AdaptMethodPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "explainHowYouWillUseMethodSix Page must" - {
          "navigate to self when user has no data for the page" in {
            navigator.nextPage(
              ExplainHowYouWillUseMethodSixPage,
              CheckMode,
              EmptyUserAnswers
            ) mustBe routes.ExplainHowYouWillUseMethodSixController.onPageLoad(mode = CheckMode)
          }

          "navigate to descriptionOfTheGoodsPage when user has data for the page" in {
            val userAnswers = userAnswersWith(ExplainHowYouWillUseMethodSixPage, "reason")
            navigator.nextPage(
              ExplainHowYouWillUseMethodSixPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }
      }

      // Pre method pages
      "Pre Method Navigation" - {
        "CheckRegisteredDetails page" - {
          def registeredDetails(value: Boolean) = CheckRegisteredDetails(
            value,
            eori = "eori",
            consentToDisclosureOfPersonalData = true,
            name = "name",
            streetAndNumber = "streetAndNumber",
            city = "city",
            country = "country",
            postalCode = None,
            phoneNumber = Some("phoneNumber")
          )
          "navigate to CheckYourAnswers when Yes" in {
            val userAnswers = userAnswersWith(CheckRegisteredDetailsPage, registeredDetails(true))
            navigator.nextPage(
              CheckRegisteredDetailsPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }

          "and navigate to EORI Be Up To Date when No" in {
            val userAnswers = userAnswersWith(CheckRegisteredDetailsPage, registeredDetails(false))
            navigator.nextPage(
              CheckRegisteredDetailsPage,
              CheckMode,
              userAnswers
            ) mustBe routes.EORIBeUpToDateController.onPageLoad()
          }
        }
      }

      // Post method pages
      "Post Method Navigation" - {
        "HasConfidentialInformation page" - {
          "navigate to DescribeTheLegalChallenges when Yes" in {
            val userAnswers = userAnswersWith(HasConfidentialInformationPage, true)
            navigator.nextPage(
              HasConfidentialInformationPage,
              CheckMode,
              userAnswers
            ) mustBe routes.ConfidentialInformationController.onPageLoad(mode = CheckMode)
          }

          "and navigate to CheckYourAnswers when No" in {
            val userAnswers = userAnswersWith(HasConfidentialInformationPage, false)
            navigator.nextPage(
              HasConfidentialInformationPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "HaveTheGoodsBeenSubjectToLegalChallenges page" - {
          "navigate to DescribeTheLegalChallenges when Yes" in {
            val userAnswers = userAnswersWith(HaveTheGoodsBeenSubjectToLegalChallengesPage, true)
            navigator.nextPage(
              HaveTheGoodsBeenSubjectToLegalChallengesPage,
              CheckMode,
              userAnswers
            ) mustBe routes.DescribeTheLegalChallengesController.onPageLoad(mode = CheckMode)
          }

          "and navigate to CheckYourAnswers when No" in {
            val userAnswers = userAnswersWith(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
            navigator.nextPage(
              HaveTheGoodsBeenSubjectToLegalChallengesPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "HasCommodityCode page" - {
          "navigate to CommodityCode when Yes" in {
            val userAnswers = userAnswersWith(HasCommodityCodePage, true)
            navigator.nextPage(
              HasCommodityCodePage,
              CheckMode,
              userAnswers
            ) mustBe routes.CommodityCodeController.onPageLoad(mode = CheckMode)
          }

          "and navigate to DoYouWantToUploadSupportingDocuments when No" in {
            val userAnswers = userAnswersWith(HasCommodityCodePage, false)
            navigator.nextPage(
              HasCommodityCodePage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "DoYouWantToUploadDocuments page" - {
          "navigate to UploadSupportingDocuments when Yes" in {
            val userAnswers = userAnswersWith(DoYouWantToUploadDocumentsPage, true)
            navigator.nextPage(
              DoYouWantToUploadDocumentsPage,
              CheckMode,
              userAnswers
            ) mustBe controllers.routes.UploadSupportingDocumentsController.onPageLoad(
              None,
              None,
              None,
              CheckMode
            )
          }

          "and navigate to CheckYourAnswers when No" in {
            val userAnswers = userAnswersWith(DoYouWantToUploadDocumentsPage, false)
            navigator.nextPage(
              DoYouWantToUploadDocumentsPage,
              CheckMode,
              userAnswers
            ) mustBe checkYourAnswers
          }
        }

        "IsThisFileConfidential page" - {
          "navigate to self if file confidentiality not set" in {
            val userAnswers = userAnswersWith(
              UploadSupportingDocumentPage,
              UploadedFiles(
                lastUpload = Some(fileDetails),
                files = Map.empty
              )
            )
            navigator.nextPage(
              IsThisFileConfidentialPage,
              CheckMode,
              userAnswers
            ) mustBe routes.IsThisFileConfidentialController.onPageLoad(
              CheckMode
            )
          }

          "navigate to upload another if file confidentiality not set" in {
            val userAnswers = userAnswersWith(
              UploadSupportingDocumentPage,
              UploadedFiles(
                lastUpload = Some(fileDetails),
                files = Map.empty
              )
            )
            navigator.nextPage(
              IsThisFileConfidentialPage,
              CheckMode,
              userAnswers
            ) mustBe routes.IsThisFileConfidentialController.onPageLoad(
              CheckMode
            )
          }

          "navigate to UploadAnotherSupportingDocument when set" in {
            val userAnswers = userAnswersWith(
              UploadSupportingDocumentPage,
              UploadedFiles(
                lastUpload = None,
                files = Map(
                  UploadId("id") ->
                    uploadedFile
                )
              )
            )
            navigator.nextPage(
              IsThisFileConfidentialPage,
              CheckMode,
              userAnswers
            ) mustBe routes.UploadAnotherSupportingDocumentController.onPageLoad(CheckMode)
          }

          "navigate to DoYouWantToUploadDocuments when there are no uploaded documents" in {
            val userAnswers = userAnswersWith(
              UploadSupportingDocumentPage,
              UploadedFiles(
                lastUpload = None,
                files = Map.empty
              )
            )
            navigator.nextPage(
              IsThisFileConfidentialPage,
              CheckMode,
              userAnswers
            ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(CheckMode)
          }

          "navigate to DoYouWantToUploadDocuments when the question has no answer" in {
            val userAnswers = EmptyUserAnswers
            navigator.nextPage(
              IsThisFileConfidentialPage,
              CheckMode,
              userAnswers
            ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(CheckMode)
          }
        }
      }

      "Other pages" - {
        "should navigate to CheckYourAnswers page" in {
          val userAnswers =
            userAnswersWith(ConfidentialInformationPage, "top secret")
          navigator.nextPage(
            ConfidentialInformationPage,
            CheckMode,
            userAnswers
          ) mustBe checkYourAnswers
        }
      }
    }

  }
}
