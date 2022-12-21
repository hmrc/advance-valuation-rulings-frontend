package pages

import pages.behaviours.PageBehaviours

class ConfidentialInformationPageSpec extends PageBehaviours {

  "ConfidentialInformationPage" - {

    beRetrievable[String](ConfidentialInformationPage)

    beSettable[String](ConfidentialInformationPage)

    beRemovable[String](ConfidentialInformationPage)
  }
}
