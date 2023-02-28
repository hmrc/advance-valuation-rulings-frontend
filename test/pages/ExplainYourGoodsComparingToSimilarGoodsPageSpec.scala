package pages

import pages.behaviours.PageBehaviours

class ExplainYourGoodsComparingToSimilarGoodsPageSpec extends PageBehaviours {

  "ExplainYourGoodsComparingToSimilarGoodsPage" - {

    beRetrievable[String](ExplainYourGoodsComparingToSimilarGoodsPage)

    beSettable[String](ExplainYourGoodsComparingToSimilarGoodsPage)

    beRemovable[String](ExplainYourGoodsComparingToSimilarGoodsPage)
  }
}
