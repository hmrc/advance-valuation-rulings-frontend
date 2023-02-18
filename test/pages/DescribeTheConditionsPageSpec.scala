package pages

import pages.behaviours.PageBehaviours


class DescribeTheConditionsPageSpec extends PageBehaviours {

  "DescribeTheConditionsPage" - {

    beRetrievable[String](DescribeTheConditionsPage)

    beSettable[String](DescribeTheConditionsPage)

    beRemovable[String](DescribeTheConditionsPage)
  }
}
