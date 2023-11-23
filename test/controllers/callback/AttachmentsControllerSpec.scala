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

package controllers.callback

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import org.scalatest.freespec.AnyFreeSpec
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.objectstore.client.{Md5Hash, Object, ObjectMetadata, Path}

import java.time.Instant
import scala.concurrent.Future

class AttachmentsControllerSpec extends AnyFreeSpec with SpecBase {

  val mockObjectStoreClient: PlayObjectStoreClient = mock(classOf[PlayObjectStoreClient])

  private lazy val app: Application = applicationBuilder()
    .overrides(
      bind[PlayObjectStoreClient].toInstance(mockObjectStoreClient)
    )
    .build()

  private implicit lazy val mat: Materializer = app.materializer

  "get" - {

    "must return a stream of the object from object store when the item exists" in {

      val bytes                                  = ByteString.fromString("Hello, World!")
      val o: Object[Source[ByteString, NotUsed]] = Object(
        location = Path.Directory("some").file("location"),
        content = Source.single(bytes),
        metadata = ObjectMetadata(
          contentType = "application/pdf",
          contentLength = bytes.length,
          contentMd5 = Md5Hash("somemd5"),
          lastModified = Instant.now(),
          userMetadata = Map.empty
        )
      )

      when(mockObjectStoreClient.getObject[Source[ByteString, NotUsed]](any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(o)))

      val request = FakeRequest(routes.AttachmentsController.get("some/location"))
      val result  = route(app, request).value

      status(result) mustBe OK
      header("Content-Type", result).value mustBe "application/pdf"
      header("Digest", result).value mustBe "md5=somemd5"
      contentAsString(result) mustBe "Hello, World!"
    }

    "must return NOT_FOUND when the object does not exist in object-store" in {

      when(mockObjectStoreClient.getObject[Source[ByteString, _]](any(), any())(any(), any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(routes.AttachmentsController.get("some/location"))
      val result  = route(app, request).value

      status(result) mustBe NOT_FOUND
    }

    "must fail when object-store fails" in {

      when(mockObjectStoreClient.getObject[Source[ByteString, _]](any(), any())(any(), any()))
        .thenReturn(Future.failed(new RuntimeException()))

      val request = FakeRequest(routes.AttachmentsController.get("some/location"))
      route(app, request).value.failed.futureValue
    }
  }
}
