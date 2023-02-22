package pages

import pages.behaviours.PageBehaviours


class DescribeTheLegalChallengesPageSpec extends PageBehaviours {

  "DescribeTheLegalChallengesPage" - {

    beRetrievable[String](DescribeTheLegalChallengesPage)

    beSettable[String](DescribeTheLegalChallengesPage)

    beRemovable[String](DescribeTheLegalChallengesPage)
  }
}
