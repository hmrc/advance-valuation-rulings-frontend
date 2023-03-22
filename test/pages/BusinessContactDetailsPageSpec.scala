package pages

import pages.behaviours.PageBehaviours

class BusinessContactDetailsPageSpec extends PageBehaviours {

  "BusinessContactDetailsPage" - {

    beRetrievable[String](BusinessContactDetailsPage)

    beSettable[String](BusinessContactDetailsPage)

    beRemovable[String](BusinessContactDetailsPage)
  }
}
