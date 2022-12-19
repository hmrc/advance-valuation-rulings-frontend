package pages

import pages.behaviours.PageBehaviours

class HasConfidentialInformationPageSpec extends PageBehaviours {

  "HasConfidentialInformationPage" - {

    beRetrievable[Boolean](HasConfidentialInformationPage)

    beSettable[Boolean](HasConfidentialInformationPage)

    beRemovable[Boolean](HasConfidentialInformationPage)
  }
}
