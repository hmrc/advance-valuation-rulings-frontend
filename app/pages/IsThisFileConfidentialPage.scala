/*
 * Copyright 2023 HM Revenue & Customs
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

package pages

import play.api.libs.json.JsPath

import models.Index

final case class IsThisFileConfidentialPage(index: Index) extends QuestionPage[Boolean] {
  override val key          = s"$toString:$index.position"
  override def path: JsPath = JsPath \ "supportingDocuments" \ index.position \ toString

  override def toString: String = "isThisFileConfidential"
}
object IsThisFileConfidentialPage extends WithIndexedKeys

//   private def makeKey: Index => String = index => s"${IsThisFileConfidentialPage(index).toString}:${index.position}"
//   val key0 = makeKey(Index(0))
//   val key1 = makeKey(Index(1))
//   val key2 = makeKey(Index(2))
//   val key3 = makeKey(Index(3))
//   val key4 = makeKey(Index(4))
// }
