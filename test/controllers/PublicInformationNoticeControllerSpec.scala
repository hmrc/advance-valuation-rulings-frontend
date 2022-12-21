package controllers

import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import views.html.PublicInformationNoticeView

class PublicInformationNoticeControllerSpec extends SpecBase {

  "PublicInformationNotice Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.PublicInformationNoticeController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PublicInformationNoticeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
