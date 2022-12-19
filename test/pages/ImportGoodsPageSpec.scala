package pages

import pages.behaviours.PageBehaviours

class ImportGoodsPageSpec extends PageBehaviours {

  "ImportGoodsPage" - {

    beRetrievable[Boolean](ImportGoodsPage)

    beSettable[Boolean](ImportGoodsPage)

    beRemovable[Boolean](ImportGoodsPage)
  }
}
