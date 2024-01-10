/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import play.api.libs.json.{OFormat, OWrites, Reads, __}

case class ApplicationContactDetails(name: String, email: String, phone: String, jobTitle: String)

object ApplicationContactDetails {

  val reads: Reads[ApplicationContactDetails] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "name").read[String] and
        (__ \ "email").read[String] and
        (__ \ "phone").read[String] and
        (__ \ "jobTitle").read[String]
    )(ApplicationContactDetails.apply _)
  }

  val writes: OWrites[ApplicationContactDetails] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "name").write[String] and
        (__ \ "email").write[String] and
        (__ \ "phone").write[String] and
        (__ \ "jobTitle").write[String]
    )(unlift(ApplicationContactDetails.unapply))
  }

  implicit val format: OFormat[ApplicationContactDetails] = OFormat(reads, writes)
}
