import sbt.*

object AppDependencies {

  private val hmrcMongoVersion = "1.9.0"
  private val bootstrapVersion = "8.6.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30"            % "10.1.0",
    "uk.gov.hmrc"                  %% "play-conditional-form-mapping-play-30" % "2.0.0",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc"                  %% "internal-auth-client-play-30"          % "2.0.0",
    "uk.gov.hmrc.objectstore"      %% "object-store-client-play-30"           % "1.4.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "org.typelevel"                %% "cats-core"                             % "2.12.0",
    "com.beachape"                 %% "enumeratum-play-json"                  % "1.8.0",
    "com.googlecode.libphonenumber" % "libphonenumber"                        % "8.13.39"
  )

  private val test: Seq[ModuleID] = Seq(
    "org.jsoup"          % "jsoup"                   % "1.17.2",
    "org.mockito"       %% "mockito-scala"           % "1.17.31",
    "org.scalatestplus" %% "scalacheck-1-17"         % "3.2.18.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID]      = compile ++ test
}
