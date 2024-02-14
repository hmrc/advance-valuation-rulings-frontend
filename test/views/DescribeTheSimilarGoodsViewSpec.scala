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

package views

import forms.DescribeTheSimilarGoodsFormProvider
import models.NormalMode
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.DescribeTheSimilarGoodsView

class DescribeTheSimilarGoodsViewSpec extends ViewBehaviours {

  private val form: DescribeTheSimilarGoodsFormProvider = app.injector.instanceOf[DescribeTheSimilarGoodsFormProvider]

  private val view: DescribeTheSimilarGoodsView = app.injector.instanceOf[DescribeTheSimilarGoodsView]

  val viewViaApply: HtmlFormat.Appendable  = view.apply(form.apply(), NormalMode, draftId)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable = view.render(form.apply(), NormalMode, draftId, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      = view.f(form.apply(), NormalMode, draftId)(fakeRequest, messages)

  "DescribeTheSimilarGoodsView" - {
    normalPage("describeTheSimilarGoods")()
  }
}
