/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.mappings

import models.Enumerable
import play.api.data.FormError
import play.api.data.format.Formatter
import utils.PostcodeValidator

trait Formatters {

  private[mappings] def commodityCodeFormatter(
    errorKey: String,
    args: Seq[String] = Seq.empty
  ): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      val code: String = data.getOrElse(key, "").filterNot(_.isWhitespace)

      code match {
        case "" => Left(Seq(FormError(key, errorKey, args)))
        case s  => Right(s)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def stringFormatter(
    errorKey: String,
    args: Seq[String] = Seq.empty
  ): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                      => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s)                   => Right(s)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def booleanFormatter(
    requiredKey: String,
    invalidKey: String,
    args: Seq[String] = Seq.empty
  ): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Boolean] =
        baseFormatter
          .bind(key, data)
          .flatMap {
            case "true"  => Right(true)
            case "false" => Right(false)
            case _       => Left(Seq(FormError(key, invalidKey, args)))
          }

      def unbind(key: String, value: Boolean): Map[String, String] = Map(key -> value.toString)
    }

  private[mappings] def enumerableFormatter[A](
    requiredKey: String,
    invalidKey: String,
    args: Seq[String] = Seq.empty
  )(implicit ev: Enumerable[A]): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).flatMap { str =>
          ev.withName(str)
            .map(Right.apply)
            .getOrElse(Left(Seq(FormError(key, invalidKey, args))))
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def postcodeFormatter(
    emptyPostcodeErrorKey: String,
    notValidPostcodeErrorKey: String
  ): Formatter[Option[String]] = new Formatter[Option[String]] {

    override def bind(
      key: String,
      data: Map[String, String]
    ): Either[Seq[FormError], Option[String]] = {
      lazy val country  = data.getOrElse("country", "").trim.toUpperCase
      lazy val postCode = data.getOrElse(key, "").trim

      // validate only GB postcodes
      if (country == "GB" && postCode.isEmpty) {
        // if country is gb and no postcode was entered
        Left(Seq(FormError(key, emptyPostcodeErrorKey)))
      } else if (country == "GB" && !PostcodeValidator.validate(postCode)) {
        // if invalid gb postcode
        Left(Seq(FormError(key, notValidPostcodeErrorKey)))
      } else {
        // if is empty or non gb country set as is, empty or user input one
        Right(Some(postCode))
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))

  }

}
