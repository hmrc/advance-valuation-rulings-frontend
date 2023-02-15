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

package config

import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

import com.google.inject.{Inject, Singleton}

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "advance-valuation-ruling"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val loginUrl: String             = configuration.get[String]("urls.login")
  val loginContinueUrl: String     = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String           = configuration.get[String]("urls.signOut")
  val searchUrl: String            = configuration.get[String]("urls.search")
  val findCommodityCodeUrl: String = configuration.get[String]("urls.findCommodityCode")
  val contactEmail: String         = configuration.get[String]("urls.contactAddress")

  val advanceTariffRulingUrl: String = configuration.get[String]("urls.advanceTariffRuling")
  val advanceOriginRulingUrl: String = configuration.get[String]("urls.advanceOriginRuling")
  val stepByStepGuideUrl: String     = configuration.get[String]("urls.stepByStepGuide")
  val generalInformationUrl: String  = configuration.get[String]("urls.generalInformation")
  val helpAndSupportUrl: String      = configuration.get[String]("urls.helpAndSupport")

  val importingGoodsGuideUrl: String     = configuration.get[String]("urls.importingGoodsGuide")
  val importedGoodsValueGuideUrl: String = configuration.get[String]("urls.importedGoodsValueGuide")
  val applyForATRGuideUrl: String        = configuration.get[String]("urls.applyForATRGuide")
  val overviewForMethodsUrl: String      = configuration.get[String]("urls.overviewForMethods")

  val tradeSanctionsInformationUrl: String =
    configuration.get[String]("urls.tradeSanctionsInformation")

  val fillInAnEnquiryFormUrl: String      = configuration.get[String]("urls.fillInAnEnquiryForm")
  val importAndExportEnquiriesUrl: String =
    configuration.get[String]("urls.importAndExportEnquiries")

  private val exitSurveyBaseUrl: String =
    configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/advance-valuation-ruling"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  lazy val callbackEndpointTarget: String = loadConfig("host")
    + "/upscan-callback"

  lazy val initiateV2Url: String =
    configuration
      .get[Service]("microservice.services.upscan-initiate")
      .baseUrl + "/upscan/v2/initiate"

  private def loadConfig(key: String) =
    configuration
      .getOptional[String](key)
      .getOrElse(throw new Exception(s"Missing configuration key: $key"))

}
