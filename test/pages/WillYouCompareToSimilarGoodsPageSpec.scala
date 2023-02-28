package pages

import pages.behaviours.PageBehaviours

class WillYouCompareToSimilarGoodsPageSpec extends PageBehaviours {

  "WillYouCompareToSimilarGoodsPage" - {

    beRetrievable[Boolean](WillYouCompareToSimilarGoodsPage)

    beSettable[Boolean](WillYouCompareToSimilarGoodsPage)

    beRemovable[Boolean](WillYouCompareToSimilarGoodsPage)
  }
}
