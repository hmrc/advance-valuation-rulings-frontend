package pages

import pages.behaviours.PageBehaviours

class HaveYouUsedMethodOneForSimilarGoodsInPastPageSpec extends PageBehaviours {

  "HaveYouUsedMethodOneForSimilarGoodsInPastPage" - {

    beRetrievable[Boolean](HaveYouUsedMethodOneForSimilarGoodsInPastPage)

    beSettable[Boolean](HaveYouUsedMethodOneForSimilarGoodsInPastPage)

    beRemovable[Boolean](HaveYouUsedMethodOneForSimilarGoodsInPastPage)
  }
}
