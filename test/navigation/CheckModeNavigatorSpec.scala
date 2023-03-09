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

import base.SpecBase
import controllers.routes
import models._
import models.fileupload._
import pages._
import queries.Modifiable

class CheckModeNavigatorSpec extends SpecBase {

  val EmptyUserAnswers: UserAnswers  = UserAnswers("id")
  val navigator                      = new Navigator
  val fileDetails: UpscanFileDetails = UpscanFileDetails(UploadId("id"), "name", "some.url")
  val uploadedFile: UploadedFile     = UploadedFile(
    fileDetails.fileName,
    fileDetails.downloadUrl,
    isConfidential = false
  )
  "Navigator" - {

    def userAnswersWith[A: Writes](page: Modifiable[A], value: A): UserAnswers =
      EmptyUserAnswers.set(page, value).success.value

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          EmptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad
      }

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
          ) mustBe routes.CheckYourAnswersController.onPageLoad
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
          ) mustBe routes.CheckYourAnswersController.onPageLoad
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
          ) mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }

      "DoYouWantToUploadDocuments page" - {
        "navigate to UploadSupportingDocuments when Yes" in {
          val userAnswers = userAnswersWith(DoYouWantToUploadDocumentsPage, true)
          navigator.nextPage(
            DoYouWantToUploadDocumentsPage,
            CheckMode,
            userAnswers
          ) mustBe controllers.fileupload.routes.UploadSupportingDocumentsController.onPageLoad(
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
          ) mustBe routes.CheckYourAnswersController.onPageLoad
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
                UploadId("id") -> uploadedFile
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

      "Other pages" - {
        "should navigate to CheckYourAnswers page" in {
          val userAnswers =
            userAnswersWith(ConfidentialInformationPage, "top secret")
          navigator.nextPage(
            ConfidentialInformationPage,
            CheckMode,
            userAnswers
          ) mustBe routes.CheckYourAnswersController.onPageLoad
        }
      }
    }

  }
}
