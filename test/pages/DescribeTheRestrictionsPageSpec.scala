package pages

import pages.behaviours.PageBehaviours


class DescribeTheRestrictionsPageSpec extends PageBehaviours {

  "DescribeTheRestrictionsPage" - {

    beRetrievable[String](DescribeTheRestrictionsPage)

    beSettable[String](DescribeTheRestrictionsPage)

    beRemovable[String](DescribeTheRestrictionsPage)
  }
}
