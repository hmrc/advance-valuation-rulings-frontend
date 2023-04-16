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

import java.net.URL
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.{Md5Hash, Path}
import uk.gov.hmrc.objectstore.client.config.ObjectStoreClientConfig
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import connectors.UpscanConnector
import models.{Done, DraftId, Index, Mode, UploadedFile, UserAnswers}
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import pages.UploadSupportingDocumentPage
import services.UserAnswersService
import services.fileupload.FileService.NoUserAnswersFoundException

@Singleton
class FileService @Inject() (
  configuration: Configuration,
  upscanConnector: UpscanConnector,
  userAnswersService: UserAnswersService,
  objectStoreClient: PlayObjectStoreClient,
  objectStoreConfig: ObjectStoreClientConfig
)(implicit ec: ExecutionContext) {

  private val host: String            = configuration.get[String]("host")
  private val callbackBaseUrl: String = configuration.get[String]("upscan.callbackBaseUrl")
  private val minimumFileSize: Long   = configuration.underlying.getBytes("upscan.minFileSize")
  private val maximumFileSize: Long   = configuration.underlying.getBytes("upscan.maxFileSize")

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  def initiate(draftId: DraftId, mode: Mode, index: Index)(implicit
    hc: HeaderCarrier
  ): Future[UpscanInitiateResponse] = {

    val redirectPath =
      controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(index, mode, draftId, None, None, None)
        .url
    val redirectUrl  = s"$host$redirectPath"

    val request = UpscanInitiateRequest(
      callbackUrl =
        s"$callbackBaseUrl${controllers.callback.routes.UploadCallbackController.callback(draftId, index).url}",
      successRedirect = redirectUrl,
      errorRedirect = redirectUrl,
      minimumFileSize = minimumFileSize,
      maximumFileSize = maximumFileSize
    )

    upscanConnector.initiate(request)
  }

  def update(draftId: DraftId, index: Index, file: UploadedFile): Future[Done] =
    for {
      updatedFile    <- processFile(draftId, file)
      answers        <- getUserAnswers(draftId)
      updatedAnswers <-
        Future.fromTry(answers.set(UploadSupportingDocumentPage(index), updatedFile))
      _              <- userAnswersService.set(updatedAnswers)
    } yield Done

  private def processFile(draftId: DraftId, file: UploadedFile): Future[UploadedFile] =
    file match {
      case success: UploadedFile.Success =>
        objectStoreClient
          .uploadFromUrl(
            from = new URL(success.downloadUrl),
            to = Path.File(s"drafts/$draftId/${success.uploadDetails.fileName}"),
            retentionPeriod = objectStoreConfig.defaultRetentionPeriod,
            contentType = Some(success.uploadDetails.fileMimeType),
            contentMd5 = Some(Md5Hash(success.uploadDetails.checksum)),
            owner = objectStoreConfig.owner
          )
          .map(summary => success.copy(downloadUrl = summary.location.asUri))
      case _                             =>
        Future.successful(file)
    }

  private def getUserAnswers(draftId: DraftId): Future[UserAnswers] =
    userAnswersService.get(draftId).flatMap {
      _.map(Future.successful)
        .getOrElse(Future.failed(NoUserAnswersFoundException(draftId)))
    }
}

object FileService {

  final case class NoUserAnswersFoundException(draftId: DraftId)
      extends Exception
      with NoStackTrace {
    override def getMessage: String = s"No user answers found for $draftId"
  }
}
