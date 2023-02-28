package pages

import pages.behaviours.PageBehaviours

class ExplainHowYouWillUseMethodSixPageSpec extends PageBehaviours {

  "ExplainHowYouWillUseMethodSixPage" - {

    beRetrievable[String](ExplainHowYouWillUseMethodSixPage)

    beSettable[String](ExplainHowYouWillUseMethodSixPage)

    beRemovable[String](ExplainHowYouWillUseMethodSixPage)
  }
}
