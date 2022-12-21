package pages

import pages.behaviours.PageBehaviours

class DescribeTheGoodsPageSpec extends PageBehaviours {

  "DescribeTheGoodsPage" - {

    beRetrievable[String](DescribeTheGoodsPage)

    beSettable[String](DescribeTheGoodsPage)

    beRemovable[String](DescribeTheGoodsPage)
  }
}
