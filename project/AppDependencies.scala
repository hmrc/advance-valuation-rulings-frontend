import sbt.*

object AppDependencies {

  val hmrcMongoVersion = "1.7.0"
  val bootstrapVersion = "7.23.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"            % "7.29.0-play-28",
    "uk.gov.hmrc"                  %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc"                  %% "internal-auth-client-play-28"  % "1.10.0",
    "uk.gov.hmrc.objectstore"      %% "object-store-client-play-28"   % "1.3.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"            % hmrcMongoVersion,
    "org.typelevel"                %% "cats-core"                     % "2.10.0",
    "com.beachape"                 %% "enumeratum-play-json"          % "1.6.3",
    "com.googlecode.libphonenumber" % "libphonenumber"                % "8.13.29"
  )

  val test: Seq[ModuleID]    = Seq(
    "org.scalatest"       %% "scalatest"               % "3.2.17",
    "org.jsoup"            % "jsoup"                   % "1.17.2",
    "org.mockito"         %% "mockito-scala"           % "1.17.30",
    "org.scalatestplus"   %% "scalacheck-1-17"         % "3.2.17.0",
    "wolfendale"          %% "scalacheck-gen-regexp"   % "0.1.2",
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"  % bootstrapVersion,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
