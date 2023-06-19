package controllers

import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import views.html.DraftHasBeenSavedView

class DraftHasBeenSavedControllerSpec extends SpecBase {

  "DraftHasBeenSaved Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.DraftHasBeenSavedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DraftHasBeenSavedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
