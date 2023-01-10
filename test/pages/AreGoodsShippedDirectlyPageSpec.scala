package pages

import pages.behaviours.PageBehaviours

class AreGoodsShippedDirectlyPageSpec extends PageBehaviours {

  "AreGoodsShippedDirectlyPage" - {

    beRetrievable[Boolean](AreGoodsShippedDirectlyPage)

    beSettable[Boolean](AreGoodsShippedDirectlyPage)

    beRemovable[Boolean](AreGoodsShippedDirectlyPage)
  }
}
