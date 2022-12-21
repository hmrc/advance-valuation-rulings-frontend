package pages

import pages.behaviours.PageBehaviours

class CommodityCodePageSpec extends PageBehaviours {

  "CommodityCodePage" - {

    beRetrievable[String](CommodityCodePage)

    beSettable[String](CommodityCodePage)

    beRemovable[String](CommodityCodePage)
  }
}
