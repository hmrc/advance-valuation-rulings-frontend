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

package models

import java.time.Instant

import cats.data.{Validated, ValidatedNel}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import models.ValuationMethod._
import pages._
import queries.Modifiable

final case class UserAnswers(
  id: String,
  applicationNumber: String,
  data: JsObject = Json.obj(),
  lastUpdated: Instant = Instant.now
) {

  def get[A](page: Modifiable[A])(implicit rds: Reads[A]): Option[A] =
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

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def setFuture[A](page: Modifiable[A], value: A)(implicit
    writes: Writes[A]
  ): Future[UserAnswers] =
    Future.fromTry(set(page, value))

  def upsert[A](page: Modifiable[A], f: A => A, default: A)(implicit
    format: Format[A]
  ): Try[UserAnswers] =
    get[A](page) match {
      case Some(value) =>
        set[A](page, f(value))
      case None        =>
        set[A](page, default)
    }

  def upsertFuture[A](page: Modifiable[A], f: A => A, default: A)(implicit
    format: Format[A]
  ): Future[UserAnswers] =
    Future.fromTry(upsert(page, f, default))

  def modify[A](page: Modifiable[A], f: A => A)(implicit format: Format[A]): Try[UserAnswers] =
    get[A](page) match {
      case Some(value) =>
        set[A](page, f(value))
      case None        =>
        Failure(new Exception(s"Cannot find value at ${page.path}"))
    }

  def modifyFuture[A](page: Modifiable[A], f: A => A)(implicit
    format: Format[A]
  ): Future[UserAnswers] =
    Future.fromTry(modify(page, f))

  def remove[A](page: Modifiable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_)            =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(None, updatedAnswers)
    }
  }

  def removeFuture[A](page: Modifiable[A]): Future[UserAnswers] =
    Future.fromTry(remove(page))
}

object UserAnswers {

  val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[String] and
        (__ \ "applicationNumber").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[String] and
        (__ \ "applicationNumber").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(unlift(UserAnswers.unapply))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)

  def updateValuationMethod(userAnswers: UserAnswers, value: ValuationMethod)(implicit
    format: Format[ValuationMethod]
  ): Try[UserAnswers] = {
    val currentMethod = userAnswers.get(ValuationMethodPage)

    val pathsToClear = ValuationMethodPage.toString +: (currentMethod match {
      case None          => Seq.empty
      case Some(Method1) =>
        Seq(
          IsThereASaleInvolvedPage.toString,
          IsSaleBetweenRelatedPartiesPage.toString,
          IsTheSaleSubjectToConditionsPage.toString,
          DescribeTheConditionsPage.toString,
          AreThereRestrictionsOnTheGoodsPage.toString,
          DescribeTheRestrictionsPage.toString
        )
      case Some(Method2) =>
        Seq(
          WhyIdenticalGoodsPage.toString,
          HaveYouUsedMethodOneInPastPage.toString,
          DescribeTheIdenticalGoodsPage.toString,
          WillYouCompareGoodsToIdenticalGoodsPage.toString,
          ExplainYourGoodsComparingToIdenticalGoodsPage.toString
        )
      case Some(Method3) =>
        Seq(
          WhyTransactionValueOfSimilarGoodsPage.toString(),
          HaveYouUsedMethodOneForSimilarGoodsInPastPage.toString,
          WillYouCompareToSimilarGoodsPage.toString,
          ExplainYourGoodsComparingToSimilarGoodsPage.toString,
          DescribeTheSimilarGoodsPage.toString
        )
      case Some(Method4) =>
        Seq(
          ExplainWhyYouHaveNotSelectedMethodOneToThreePage.toString,
          ExplainWhyYouChoseMethodFourPage.toString
        )
      case Some(Method5) =>
        Seq(WhyComputedValuePage.toString, ExplainReasonComputedValuePage.toString)
      case Some(Method6) =>
        Seq(
          ExplainWhyYouHaveNotSelectedMethodOneToFivePage.toString,
          AdaptMethodPage.toString,
          ExplainHowYouWillUseMethodSixPage.toString
        )
    })

    val updatedData = pathsToClear.foldLeft(userAnswers.data) {
      case (data, path) =>
        data - path
    }

    val updatedAnswers = userAnswers.copy(data = updatedData)

    updatedAnswers.set(ValuationMethodPage, value)
  }
}
