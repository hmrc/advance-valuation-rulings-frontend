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

package utils

import base.SpecBase
import play.api.i18n.MessagesApi

import scala.util.matching.Regex

class MessagesSpec extends SpecBase {

  implicit lazy val realMessagesApi: MessagesApi = messagesApi(applicationBuilder().build())

  private lazy val defaultMessages: Map[String, String] = getExpectedMessages("default")

  private lazy val welshMessages: Map[String, String] = getExpectedMessages("cy")

  private def getExpectedMessages(languageCode: String) =
    realMessagesApi.messages.getOrElse(
      languageCode,
      throw new Exception(s"Missing messages for $languageCode")
    )

  "all english messages" - {
    "have a welsh translation" in {
      val realMessages    = realMessagesApi.messages
      val englishMessages = realMessages("en")
      val welshMessages   = realMessages("cy")

      val missingWelshKeys = englishMessages.keySet.filterNot(welshMessages.keySet)

      if (missingWelshKeys.nonEmpty) {
        val failureText = missingWelshKeys.foldLeft(
          s"There are ${missingWelshKeys.size} missing Welsh translations:"
        ) { case (failureString, key) =>
          failureString + s"\n$key:${englishMessages(key)}"
        }

        fail(failureText)
      }
    }
    "have a non-empty message for each key" in {
      assertNonEmptyValuesForDefaultMessages()
      assertNonEmptyValuesForWelshMessages()
    }
    "have no unescaped single quotes in value" in {
      assertCorrectUseOfQuotesForDefaultMessages()
      assertCorrectUseOfQuotesForWelshMessages()
    }
  }

  private def assertNonEmptyValuesForDefaultMessages(): Unit =
    assertNonEmptyNonTemporaryValues("Default", defaultMessages)

  private def assertNonEmptyValuesForWelshMessages(): Unit =
    assertNonEmptyNonTemporaryValues("Welsh", welshMessages)

  private def assertCorrectUseOfQuotesForDefaultMessages(): Unit =
    assertCorrectUseOfQuotes("Default", defaultMessages)

  private def assertCorrectUseOfQuotesForWelshMessages(): Unit =
    assertCorrectUseOfQuotes("Welsh", welshMessages)

  private def assertNonEmptyNonTemporaryValues(label: String, messages: Map[String, String]): Unit =
    messages.foreach { case (key: String, value: String) =>
      withClue(s"In $label, there is an empty value for the key:[$key][$value]") {
        value.trim.isEmpty mustBe false
      }
    }

  private def assertCorrectUseOfQuotes(label: String, messages: Map[String, String]): Unit =
    messages.foreach { case (key: String, value: String) =>
      withClue(s"In $label, there is an unescaped or invalid quote:[$key][$value]") {
        MatchSingleQuoteOnly.findFirstIn(value).isDefined mustBe false
        MatchBacktickQuoteOnly.findFirstIn(value).isDefined mustBe false
      }
    }

  val MatchSingleQuoteOnly: Regex   = """\w+'{1}\w+""".r
  val MatchBacktickQuoteOnly: Regex = """`+""".r

}
