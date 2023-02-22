package pages

import pages.behaviours.PageBehaviours


class DescriptionOfGoodsPageSpec extends PageBehaviours {

  "DescriptionOfGoodsPage" - {

    beRetrievable[String](DescriptionOfGoodsPage)

    beSettable[String](DescriptionOfGoodsPage)

    beRemovable[String](DescriptionOfGoodsPage)
  }
}
