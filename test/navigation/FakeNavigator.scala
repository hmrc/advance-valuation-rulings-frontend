package navigation

import play.api.mvc.Call

import models.{Mode, UserAnswers}
import pages._

class FakeNavigator(desiredRoute: Call) extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    desiredRoute
}
