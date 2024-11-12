import uk.gov.hmrc.DefaultBuildSettings.*

lazy val appName: String = "advance-valuation-rulings-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.5.1"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(
    JUnitXmlReportPlugin
  ) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    Test / unmanagedSourceDirectories += baseDirectory.value / "test-utils"
  )
  .settings(
    routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._",
      "viewmodels.govuk.all._"
    ),
    PlayKeys.playDefaultPort := 12600,
    CodeCoverageSettings.settings,
    scalacOptions ++= List(
      "-feature",
      "-Wconf:msg=unused import&src=conf/.*:s",
      "-Wconf:msg=unused import&src=views/.*:s",
      "-Wconf:msg=unused explicit parameter&src=views/.*:s",
      "-Wconf:src=routes/.*:s"
    ),
    libraryDependencies ++= AppDependencies(),
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(
          Seq(
            "javascripts/app.js"
          )
        )
    ),
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat)
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(itSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
