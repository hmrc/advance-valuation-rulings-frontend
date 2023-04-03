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

package viewmodels.govuk

import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

trait TableFluency {

  implicit class FluentTableRow(row: TableRow) {

    def withCssClass(className: String): TableRow =
      row.copy(classes = s"${row.classes} $className")

  }

  implicit class FluentTableRows(rows: Seq[TableRow]) {

    def withCssClass(className: String): Seq[TableRow] =
      rows.map(_.withCssClass(className))

  }

  implicit class FluentTable(rows: Seq[Seq[TableRow]]) {

    def withCssClass(className: String): Seq[Seq[TableRow]] =
      rows.map(_.withCssClass(className))

  }
}
