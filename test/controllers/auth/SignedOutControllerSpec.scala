package controllers.auth

import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import views.html.auth.SignedOutView

class SignedOutControllerSpec extends SpecBase {

  "SignedOut Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.SignedOutController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SignedOutView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
