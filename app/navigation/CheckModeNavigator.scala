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

import play.api.mvc.Call

import controllers.routes._
import models._
import models.CheckMode
import models.ValuationMethod._
import pages._

object CheckModeNavigator {
  import controllers._

  private val checkYourAnswers = routes.CheckYourAnswersController.onPageLoad

  private def hasConfidentialInformation(implicit userAnswers: UserAnswers): Call =
    userAnswers.get(HasConfidentialInformationPage) match {
      case None        => HasConfidentialInformationController.onPageLoad(CheckMode)
      case Some(true)  => ConfidentialInformationController.onPageLoad(CheckMode)
      case Some(false) => checkYourAnswers
    }

  private def haveBeenSubjectToLegalChallenges(implicit userAnswers: UserAnswers): Call =
    userAnswers.get(HaveTheGoodsBeenSubjectToLegalChallengesPage) match {
      case None        => HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(CheckMode)
      case Some(true)  => DescribeTheLegalChallengesController.onPageLoad(CheckMode)
      case Some(false) => checkYourAnswers
    }

  private def hasCommodityCode(implicit userAnswers: UserAnswers): Call =
    userAnswers.get(HasCommodityCodePage) match {
      case None        => HasCommodityCodeController.onPageLoad(CheckMode)
      case Some(true)  => CommodityCodeController.onPageLoad(CheckMode)
      case Some(false) => checkYourAnswers
    }

  private def doYouWantToUploadDocuments(implicit userAnswers: UserAnswers): Call =
    userAnswers.get(DoYouWantToUploadDocumentsPage) match {
      case None        => DoYouWantToUploadDocumentsController.onPageLoad(CheckMode)
      case Some(true)  =>
        controllers.fileupload.routes.UploadSupportingDocumentsController
          .onPageLoad(None, None, None, CheckMode)
      case Some(false) => checkYourAnswers
    }

  private def isThisFileConfidential(implicit userAnswers: UserAnswers): Call =
    userAnswers.get(UploadSupportingDocumentPage) match {
      case None                => doYouWantToUploadDocuments(userAnswers)
      case Some(uploadedFiles) =>
        uploadedFiles match {
          case UploadedFiles(Some(_), _)                    =>
            IsThisFileConfidentialController.onPageLoad(CheckMode)
          case UploadedFiles(None, files) if files.nonEmpty =>
            UploadAnotherSupportingDocumentController.onPageLoad(CheckMode)
          case UploadedFiles(None, _)                       =>
            DoYouWantToUploadDocumentsController.onPageLoad(CheckMode)
        }
    }

  private def uploadAnotherSupportingDocument(implicit userAnswers: UserAnswers): Call =
    userAnswers.get(UploadAnotherSupportingDocumentPage) match {
      case None        => UploadAnotherSupportingDocumentController.onPageLoad(CheckMode)
      case Some(true)  =>
        controllers.fileupload.routes.UploadSupportingDocumentsController
          .onPageLoad(None, None, None, CheckMode)
      case Some(false) => checkYourAnswers
    }

  def nextPage(page: Page)(implicit userAnswers: UserAnswers): Call = page match {
    case HasConfidentialInformationPage               => hasConfidentialInformation
    case HaveTheGoodsBeenSubjectToLegalChallengesPage => haveBeenSubjectToLegalChallenges
    case HasCommodityCodePage                         => hasCommodityCode
    case DoYouWantToUploadDocumentsPage               => doYouWantToUploadDocuments
    case IsThisFileConfidentialPage                   => isThisFileConfidential
    case UploadAnotherSupportingDocumentPage          => uploadAnotherSupportingDocument
    case _                                            => routes.CheckYourAnswersController.onPageLoad
  }
}
