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

import cats.data.{Validated, ValidatedNel}
import pages._
import play.api.libs.json._
import queries.{Gettable, Modifiable}

import java.time.Instant
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  userId: String,
  draftId: DraftId,
  data: JsObject = Json.obj(),
  lastUpdated: Instant = Instant.now
) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def getOrElse[A](page: Modifiable[A], default: => A)(implicit rds: Reads[A]): A =
    get(page).getOrElse(default)

  def validated[A](
    page: QuestionPage[A]
  )(implicit rds: Reads[A]): ValidatedNel[QuestionPage[_], A] =
    Validated
      .fromOption(get(page), page)
      .toValidatedNel

  def validatedF[A, B](
    page: QuestionPage[A],
    f: A => B
  )(implicit rds: Reads[A]): ValidatedNel[QuestionPage[_], B] =
    Validated
      .fromOption(get(page).map(f), page)
      .toValidatedNel

  def set[A](page: Modifiable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors)       =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(Some(value), updatedAnswers)
    }
  }

  def setFuture[A](page: Modifiable[A], value: A)(implicit
    writes: Writes[A]
  ): Future[UserAnswers] =
    Future.fromTry(set(page, value))

  def remove[A](page: Modifiable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_)            =>
        Success(data)
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(None, updatedAnswers)
    }
  }

  def removeFuture[A](page: Modifiable[A]): Future[UserAnswers] =
    Future.fromTry(remove(page))
}

object UserAnswers {

  private val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "userId").read[String] and
        (__ \ "draftId").read[DraftId] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read[Instant]
    )(UserAnswers.apply _)
  }

  private val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "userId").write[String] and
        (__ \ "draftId").write[DraftId] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write[Instant]
    )(o => Tuple.fromProductTyped(o))
  }

  given format: OFormat[UserAnswers] = OFormat(reads, writes)

}
