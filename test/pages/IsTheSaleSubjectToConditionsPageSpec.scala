package pages

import pages.behaviours.PageBehaviours

class IsTheSaleSubjectToConditionsPageSpec extends PageBehaviours {

  "IsTheSaleSubjectToConditionsPage" - {

    beRetrievable[Boolean](IsTheSaleSubjectToConditionsPage)

    beSettable[Boolean](IsTheSaleSubjectToConditionsPage)

    beRemovable[Boolean](IsTheSaleSubjectToConditionsPage)
  }
}
