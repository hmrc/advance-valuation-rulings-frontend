package pages

import models.ValuationMethod
import pages.behaviours.PageBehaviours

class ValuationMethodSpec extends PageBehaviours {

  "ValuationMethodPage" - {

    beRetrievable[ValuationMethod](ValuationMethodPage)

    beSettable[ValuationMethod](ValuationMethodPage)

    beRemovable[ValuationMethod](ValuationMethodPage)
  }
}
