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

import scala.concurrent.Future

import play.api.data.Form
import play.api.libs.json._

import models.UserAnswers
import models.requests.DataRequest

package object controllers {
  implicit class PageOps[A: Format](page: pages.QuestionPage[A]) {
    def set(value: A)(implicit request: DataRequest[_]): Future[UserAnswers]                 =
      request.userAnswers.setFuture(page, value)
    def get()(implicit request: DataRequest[_]): Option[A]                                   =
      request.userAnswers.get(page)
    def modify(f: A => A)(implicit request: DataRequest[_]): Future[UserAnswers]             =
      request.userAnswers.modifyFuture(page, f)
    def upsert(f: A => A, default: A)(implicit request: DataRequest[_]): Future[UserAnswers] =
      request.userAnswers.upsertFuture(page, f, default)
    def fill(form: Form[A])(implicit request: DataRequest[_]): Form[A]                       =
      request.userAnswers.get(page) match {
        case Some(data) => form.fill(data)
        case None       => form
      }
  }
}
