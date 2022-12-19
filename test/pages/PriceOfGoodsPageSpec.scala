package pages

import pages.behaviours.PageBehaviours

class PriceOfGoodsPageSpec extends PageBehaviours {

  "PriceOfGoodsPage" - {

    beRetrievable[Int](PriceOfGoodsPage)

    beSettable[Int](PriceOfGoodsPage)

    beRemovable[Int](PriceOfGoodsPage)
  }
}
