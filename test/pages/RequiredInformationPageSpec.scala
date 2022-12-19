package pages

import models.RequiredInformation
import pages.behaviours.PageBehaviours

class RequiredInformationPageSpec extends PageBehaviours {

  "RequiredInformationPage" - {

    beRetrievable[Set[RequiredInformation]](RequiredInformationPage)

    beSettable[Set[RequiredInformation]](RequiredInformationPage)

    beRemovable[Set[RequiredInformation]](RequiredInformationPage)
  }
}
