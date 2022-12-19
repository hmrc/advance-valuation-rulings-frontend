package pages

import pages.behaviours.PageBehaviours


class HowAreTheGoodsMadePageSpec extends PageBehaviours {

  "HowAreTheGoodsMadePage" - {

    beRetrievable[String](HowAreTheGoodsMadePage)

    beSettable[String](HowAreTheGoodsMadePage)

    beRemovable[String](HowAreTheGoodsMadePage)
  }
}
