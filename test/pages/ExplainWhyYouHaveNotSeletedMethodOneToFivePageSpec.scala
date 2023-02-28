package pages

import pages.behaviours.PageBehaviours

class ExplainWhyYouHaveNotSeletedMethodOneToFivePageSpec extends PageBehaviours {

  "ExplainWhyYouHaveNotSeletedMethodOneToFivePage" - {

    beRetrievable[String](ExplainWhyYouHaveNotSeletedMethodOneToFivePage)

    beSettable[String](ExplainWhyYouHaveNotSeletedMethodOneToFivePage)

    beRemovable[String](ExplainWhyYouHaveNotSeletedMethodOneToFivePage)
  }
}
