import sbt.*

object AppDependencies {

  private val hmrcMongoVersion = "2.6.0"
  private val bootstrapVersion = "9.11.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30"            % "12.0.0",
    "uk.gov.hmrc"                  %% "play-conditional-form-mapping-play-30" % "3.3.0",
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc"                  %% "internal-auth-client-play-30"          % "4.0.0",
    "uk.gov.hmrc.objectstore"      %% "object-store-client-play-30"           % "2.1.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "org.typelevel"                %% "cats-core"                             % "2.13.0",
    "com.beachape"                 %% "enumeratum-play-json"                  % "1.8.2",
    "com.googlecode.libphonenumber" % "libphonenumber"                        % "9.0.3"
  )

  private val test: Seq[ModuleID] = Seq(
    "org.jsoup"          % "jsoup"                   % "1.19.1",
    "org.scalatestplus" %% "scalacheck-1-18"         % "3.2.19.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
