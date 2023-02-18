package pages

import pages.behaviours.PageBehaviours

class AreThereRestrictionsOnTheGoodsPageSpec extends PageBehaviours {

  "AreThereRestrictionsOnTheGoodsPage" - {

    beRetrievable[Boolean](AreThereRestrictionsOnTheGoodsPage)

    beSettable[Boolean](AreThereRestrictionsOnTheGoodsPage)

    beRemovable[Boolean](AreThereRestrictionsOnTheGoodsPage)
  }
}
