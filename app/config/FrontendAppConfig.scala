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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URLEncoder

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "advance-valuation-ruling"
  private val basGatewayBaseUrl: String    = configuration.get[String]("bas-gateway.host")

  lazy val emailBaseUrl: String = servicesConfig.baseUrl("email")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${URLEncoder.encode(host + request.uri, "UTF-8")}"

  val internalAuthToken: String = configuration.get[String]("internal-auth.token")

  val internalAuthService: String = servicesConfig.baseUrl("internal-auth")

  val upscanInitiateService: String = servicesConfig.baseUrl("upscan-initiate")

  val callbackBaseUrl: String = servicesConfig.baseUrl("advance-valuation-rulings-frontend")

  val loginUrl: String             = configuration.get[String]("urls.login")
  val loginContinueUrl: String     = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String           = s"$basGatewayBaseUrl/bas-gateway/sign-out-without-state"
  val findCommodityCodeUrl: String = configuration.get[String]("urls.findCommodityCode")
  val contactEmail: String         = configuration.get[String]("urls.contactAddress")

  val advanceTariffRulingUrl: String = configuration.get[String]("urls.advanceTariffRuling")
  val advanceOriginRulingUrl: String = configuration.get[String]("urls.advanceOriginRuling")
  val inAndOutOfNIUrl: String        = configuration.get[String]("urls.inAndOutOfNI")
  val stepByStepGuideUrl: String     = configuration.get[String]("urls.stepByStepGuide")
  val generalInformationUrl: String  = configuration.get[String]("urls.generalInformation")
  val helpAndSupportUrl: String      = configuration.get[String]("urls.helpAndSupport")

  val overviewForMethodsUrl: String = configuration.get[String]("urls.overviewForMethods")

  val arsSubscribeUrl: String =
    configuration.get[String]("eori-common-component.host") + "/customs-enrolment-services/ars/subscribe"

  val fillInAnEnquiryFormUrl: String      = configuration.get[String]("urls.fillInAnEnquiryForm")
  val importAndExportEnquiriesUrl: String =
    configuration.get[String]("urls.importAndExportEnquiries")

  val customsDeclarationUrl: String                 = configuration.get[String]("urls.customsDeclaration")
  val appealsAndTribunalGuidanceUrl: String         =
    configuration.get[String]("urls.appealsAndTribunalGuidance")
  val advanceValuationRulingServiceEmailUrl: String =
    configuration.get[String]("urls.advanceValuationRulingServiceEmail")

  private val exitSurveyBaseUrl: String = configuration.get[String]("feedback-frontend.host")
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/advance-valuation-ruling"

  def languageMap: Map[String, Lang] =
    Map(
      "en" -> Lang("en"),
      "cy" -> Lang("cy")
    )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val minimumFileSize: Long = configuration.underlying.getBytes("upscan.minFileSize")
  val maxFiles: Int         = configuration.get[Int]("upscan.maxFiles")
  val maxFileSize: Long     = configuration.underlying.getBytes("upscan.maxFileSize")

  val advanceValuationRulingsBackendURL: String =
    s"${servicesConfig.baseUrl("advance-valuation-rulings-backend")}/advance-valuation-rulings"
}
