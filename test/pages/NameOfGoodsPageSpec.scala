package pages

import pages.behaviours.PageBehaviours


class NameOfGoodsPageSpec extends PageBehaviours {

  "NameOfGoodsPage" - {

    beRetrievable[String](NameOfGoodsPage)

    beSettable[String](NameOfGoodsPage)

    beRemovable[String](NameOfGoodsPage)
  }
}
