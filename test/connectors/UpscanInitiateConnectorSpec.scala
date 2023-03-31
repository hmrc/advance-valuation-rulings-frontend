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

package connectors

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient

import base.SpecBase
import config.FrontendAppConfig
import models.fileupload._
import org.mockito.ArgumentMatchers.{eq => mEq, _}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar

class UpscanInitiateConnectorSpec extends SpecBase with MockitoSugar {

  val appConfig                  = mock[FrontendAppConfig]
  val mockHttpClient: HttpClient = mock[HttpClient]

  trait Setup {
    val connector = new UpscanInitiateConnector(mockHttpClient, appConfig)
  }

  "posting to Upscan Initiate" - {
    "return a Prepared Upload object" in new Setup {
      val callbackUrl     = "http://localhost:8085/upscan/v2/callback"
      val initiateUrl     = "http://localhost:8085/upscan/v2/initiate"
      val successRedirect = Some("successRedirect")
      val errorRedirect   = Some("errorRedirect")

      when(appConfig.initiateV2Url).thenReturn(initiateUrl)
      when(appConfig.callbackEndpointTarget).thenReturn(callbackUrl)
      when(appConfig.maximumFileSizeBytes).thenReturn(5000)

      val expectedRequest = UpscanInitiateRequest(
        callbackUrl = callbackUrl,
        successRedirect = successRedirect,
        errorRedirect = errorRedirect,
        minimumFileSize = None,
        maximumFileSize = Some(5000)
      )
      when(
        mockHttpClient
          .POST[UpscanInitiateRequest, PreparedUpload](
            url = mEq(initiateUrl),
            body = mEq(expectedRequest),
            headers = any()
          )(wts = any(), rds = any(), hc = any(), ec = any())
      )
        .thenReturn(
          Future.successful(
            PreparedUpload(
              reference = Reference("ref"),
              uploadRequest = UploadForm(
                href = callbackUrl,
                fields = Map.empty
              )
            )
          )
        )

      val response: UpscanInitiateResponse =
        connector
          .initiateV2(successRedirect, errorRedirect)(
            HeaderCarrier()
          )
          .futureValue

      val expectedResponse = UpscanInitiateResponse(
        UpscanFileReference("ref"),
        callbackUrl,
        Map.empty
      )

      response mustEqual expectedResponse
    }
  }

}
