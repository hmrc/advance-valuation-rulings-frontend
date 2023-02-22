package pages

import pages.behaviours.PageBehaviours

class HaveTheGoodsBeenSubjectToLegalChallengesPageSpec extends PageBehaviours {

  "HaveTheGoodsBeenSubjectToLegalChallengesPage" - {

    beRetrievable[Boolean](HaveTheGoodsBeenSubjectToLegalChallengesPage)

    beSettable[Boolean](HaveTheGoodsBeenSubjectToLegalChallengesPage)

    beRemovable[Boolean](HaveTheGoodsBeenSubjectToLegalChallengesPage)
  }
}
