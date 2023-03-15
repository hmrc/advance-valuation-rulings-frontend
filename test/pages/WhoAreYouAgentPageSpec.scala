package pages

import pages.behaviours.PageBehaviours

class WhoAreYouAgentPageSpec extends PageBehaviours {

  "WhoAreYouAgentPage" - {

    beRetrievable[String](WhoAreYouAgentPage)

    beSettable[String](WhoAreYouAgentPage)

    beRemovable[String](WhoAreYouAgentPage)
  }
}
