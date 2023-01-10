package pages

import pages.behaviours.PageBehaviours


class WhatCountryAreGoodsFromPageSpec extends PageBehaviours {

  "WhatCountryAreGoodsFromPage" - {

    beRetrievable[String](WhatCountryAreGoodsFromPage)

    beSettable[String](WhatCountryAreGoodsFromPage)

    beRemovable[String](WhatCountryAreGoodsFromPage)
  }
}
