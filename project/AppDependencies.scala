import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val HmrcMongoPlayVersion              = "1.1.0"
  val PlayFrontendHmrcVersion           = "6.8.0-play-28"
  val PlayConditionalFormMappingVersion = "1.12.0-play-28"
  val BootstrapFrontendPlayVersion      = "7.14.0"
  val CatsVersion                       = "2.9.0"

  val ScalaTestVersion          = "3.2.10"
  val ScalaTestPlusVersion      = "3.2.10.0"
  val ScalaTestPlusPlayVersion  = "5.1.0"
  val MockitoScalaVersion       = "1.16.42"
  val PegdownVersion            = "1.6.0"
  val JsoupVersion              = "1.14.3"
  val MockitoVersion            = "3.11.2"
  val ScalaCheckVersion         = "1.15.4"
  val ScalaCheckRegexGenVersion = "0.1.2"
  val HmrcMongoTestPlayVersion  = "0.74.0"
  val FlexmarkVersion           = "0.62.2"
  val EnumeratumVersion         = "1.6.3"
  val QuicklensVersion          = "1.9.1"
  val ObjectStoreVersion        = "1.0.0"
  val InternalAuthVersion       = "1.2.0"
  val LibPhoneNumberVersion     = "8.12.47"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"            % PlayFrontendHmrcVersion,
    "uk.gov.hmrc"                  %% "play-conditional-form-mapping" % PlayConditionalFormMappingVersion,
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28"    % BootstrapFrontendPlayVersion,
    "uk.gov.hmrc"                  %% "internal-auth-client-play-28"  % InternalAuthVersion,
    "uk.gov.hmrc.objectstore"      %% "object-store-client-play-28"   % ObjectStoreVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"            % HmrcMongoPlayVersion,
    "org.typelevel"                %% "cats-core"                     % CatsVersion,
    "com.beachape"                 %% "enumeratum-play-json"          % EnumeratumVersion,
    "com.googlecode.libphonenumber" % "libphonenumber"                % LibPhoneNumberVersion
  )

  val test = Seq(
    "org.scalatest"              %% "scalatest"               % ScalaTestVersion,
    "org.scalatestplus"          %% "scalacheck-1-15"         % ScalaTestPlusVersion,
    "org.scalatestplus"          %% "mockito-3-4"             % ScalaTestPlusVersion,
    "org.scalatestplus.play"     %% "scalatestplus-play"      % ScalaTestPlusPlayVersion,
    "org.pegdown"                 % "pegdown"                 % PegdownVersion,
    "org.jsoup"                   % "jsoup"                   % JsoupVersion,
    "com.typesafe.play"          %% "play-test"               % PlayVersion.current,
    "org.mockito"                %% "mockito-scala"           % MockitoScalaVersion,
    "org.scalacheck"             %% "scalacheck"              % ScalaCheckVersion,
    "wolfendale"                 %% "scalacheck-gen-regexp"   % ScalaCheckRegexGenVersion,
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-test-play-28" % HmrcMongoPlayVersion,
    "uk.gov.hmrc"                %% "bootstrap-test-play-28"  % BootstrapFrontendPlayVersion,
    "com.vladsch.flexmark"        % "flexmark-all"            % FlexmarkVersion,
    "com.softwaremill.quicklens" %% "quicklens"               % QuicklensVersion
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
