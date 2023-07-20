package controllers.actions

import base.SpecBase
import models.requests.{DataRequest, OptionalDataRequest}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Result
import play.api.mvc.Results.NotFound
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionSpec extends SpecBase {

  val ec = mock[ExecutionContext]
  val dataRequiredAction = new DataRequiredActionImpl()(ec) {
    def callRefine(request: OptionalDataRequest[_]): Future[Either[Result, DataRequest[_]]] = refine(request)
  }

  "Data Required Action" - {

    "there is no data in the optional data request" must {

      "return Left(NotFound)" in {

        val req =
        val result = await(dataRequiredAction.callRefine(req))

        result mustBe Left(NotFound)
      }
    }

    "there is data" must {

      "return Right[DataRequest]" in {

        val request = fakeOptionalDataRequest(userAnswers = Some(emptyUserAnswers))
        val result = await(dataRequiredAction.callRefine(request))

        result mustBe Right(DataRequest(request.request, request.empRef, emptyUserAnswers))
      }
    }
  }
}
