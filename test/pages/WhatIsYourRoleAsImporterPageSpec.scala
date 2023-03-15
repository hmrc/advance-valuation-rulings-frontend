package pages

import models.WhatIsYourRoleAsImporter
import pages.behaviours.PageBehaviours

class WhatIsYourRoleAsImporterSpec extends PageBehaviours {

  "WhatIsYourRoleAsImporterPage" - {

    beRetrievable[WhatIsYourRoleAsImporter](WhatIsYourRoleAsImporterPage)

    beSettable[WhatIsYourRoleAsImporter](WhatIsYourRoleAsImporterPage)

    beRemovable[WhatIsYourRoleAsImporter](WhatIsYourRoleAsImporterPage)
  }
}
