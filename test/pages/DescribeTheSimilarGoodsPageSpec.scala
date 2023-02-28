package pages

import pages.behaviours.PageBehaviours

class DescribeTheSimilarGoodsPageSpec extends PageBehaviours {

  "DescribeTheSimilarGoodsPage" - {

    beRetrievable[String](DescribeTheSimilarGoodsPage)

    beSettable[String](DescribeTheSimilarGoodsPage)

    beRemovable[String](DescribeTheSimilarGoodsPage)
  }
}
