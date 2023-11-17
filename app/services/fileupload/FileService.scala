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

package services.fileupload

import config.Service
import connectors.UpscanConnector
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import models.{Done, DraftId, UploadedFile, UserAnswers}
import pages.{UploadLetterOfAuthorityPage, UploadSupportingDocumentPage}
import play.api.Configuration
import queries.AllDocuments
import services.UserAnswersService
import services.fileupload.FileService.NoUserAnswersFoundException
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.config.ObjectStoreClientConfig
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.objectstore.client.{Md5Hash, Path}

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

@Singleton
class FileService @Inject() (
  configuration: Configuration,
  upscanConnector: UpscanConnector,
  userAnswersService: UserAnswersService,
  objectStoreClient: PlayObjectStoreClient,
  objectStoreConfig: ObjectStoreClientConfig
)(implicit ec: ExecutionContext) {

  private val host: String            = configuration.get[String]("host")
  private val callbackBaseUrl: String =
    configuration.get[Service]("microservice.services.advance-valuation-rulings-frontend")
  private val minimumFileSize: Long   = configuration.underlying.getBytes("upscan.minFileSize")
  private val maximumFileSize: Long   = configuration.underlying.getBytes("upscan.maxFileSize")

  def initiate(draftId: DraftId, redirectPath: String, isLetterOfAuthority: Boolean)(implicit
    hc: HeaderCarrier
  ): Future[UpscanInitiateResponse] = {
    val redirectUrl = s"$host$redirectPath"
    val request     = UpscanInitiateRequest(
      callbackUrl =
        s"$callbackBaseUrl${controllers.callback.routes.UploadCallbackController.callback(draftId, isLetterOfAuthority).url}",
      successRedirect = redirectUrl,
      errorRedirect = redirectUrl,
      minimumFileSize = minimumFileSize,
      maximumFileSize = maximumFileSize
    )

    for {
      response       <- upscanConnector.initiate(request)
      answers        <- getUserAnswers(draftId)
      updatedAnswers <-
        answers.setFuture(
          pageForIsLetterOfAuthority(isLetterOfAuthority),
          UploadedFile.Initiated(response.reference)
        )
      _              <- userAnswersService.set(updatedAnswers)
    } yield response
  }

  def update(draftId: DraftId, file: UploadedFile, isLetterOfAuthority: Boolean)(implicit
    hc: HeaderCarrier
  ): Future[Done] =
    for {
      answers        <- getUserAnswersInternal(draftId)
      updatedFile    <- processFile(answers, file)
      updatedAnswers <-
        answers.setFuture(pageForIsLetterOfAuthority(isLetterOfAuthority), updatedFile)
      _              <- userAnswersService.setInternal(updatedAnswers)
    } yield Done

  private def pageForIsLetterOfAuthority(isLetterOfAuthority: Boolean) =
    if (isLetterOfAuthority) {
      UploadLetterOfAuthorityPage
    } else {
      UploadSupportingDocumentPage
    }

  private def processFile(
    answers: UserAnswers,
    file: UploadedFile
  )(implicit hc: HeaderCarrier): Future[UploadedFile] =
    file match {
      case file: UploadedFile.Success =>
        val otherDocuments = answers.get(AllDocuments).getOrElse(Seq.empty)

        if (otherDocuments.flatMap(_.file.fileName).contains(file.uploadDetails.fileName)) {

          Future.successful {
            UploadedFile.Failure(
              reference = file.reference,
              failureDetails = UploadedFile.FailureDetails(
                failureReason = UploadedFile.FailureReason.Duplicate,
                failureMessage = None
              )
            )
          }
        } else {

          val path = Path.File(s"drafts/${answers.draftId}/${file.uploadDetails.fileName}")
          objectStoreClient
            .uploadFromUrl(
              from = new URL(file.downloadUrl),
              to = path,
              retentionPeriod = objectStoreConfig.defaultRetentionPeriod,
              contentType = Some(file.uploadDetails.fileMimeType),
              contentMd5 = Some(Md5Hash(file.uploadDetails.checksum)),
              owner = objectStoreConfig.owner
            )
            .map(_ => file.copy(downloadUrl = path.asUri))
        }
      case _                          =>
        Future.successful(file)
    }

  private def getUserAnswers(draftId: DraftId)(implicit hc: HeaderCarrier): Future[UserAnswers] =
    userAnswersService.get(draftId).flatMap {
      _.map(Future.successful)
        .getOrElse(Future.failed(NoUserAnswersFoundException(draftId)))
    }

  private def getUserAnswersInternal(
    draftId: DraftId
  )(implicit hc: HeaderCarrier): Future[UserAnswers] =
    userAnswersService.getInternal(draftId).flatMap {
      _.map(Future.successful)
        .getOrElse(Future.failed(NoUserAnswersFoundException(draftId)))
    }
}

object FileService {

  final case class NoUserAnswersFoundException(draftId: DraftId) extends Exception with NoStackTrace {
    override def getMessage: String = s"No user answers found for $draftId"
  }
}
