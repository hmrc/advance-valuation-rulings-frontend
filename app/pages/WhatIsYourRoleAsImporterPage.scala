package pages

import models.WhatIsYourRoleAsImporter
import play.api.libs.json.JsPath

case object WhatIsYourRoleAsImporterPage extends QuestionPage[WhatIsYourRoleAsImporter] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "whatIsYourRoleAsImporter"
}
