package pages

import pages.behaviours.PageBehaviours

class HasCommodityCodePageSpec extends PageBehaviours {

  "HasCommodityCodePage" - {

    beRetrievable[Boolean](HasCommodityCodePage)

    beSettable[Boolean](HasCommodityCodePage)

    beRemovable[Boolean](HasCommodityCodePage)
  }
}
