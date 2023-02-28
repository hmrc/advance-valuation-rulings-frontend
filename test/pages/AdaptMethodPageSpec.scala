package pages

import models.AdaptMethod
import pages.behaviours.PageBehaviours

class AdaptMethodSpec extends PageBehaviours {

  "AdaptMethodPage" - {

    beRetrievable[AdaptMethod](AdaptMethodPage)

    beSettable[AdaptMethod](AdaptMethodPage)

    beRemovable[AdaptMethod](AdaptMethodPage)
  }
}
