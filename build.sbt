import uk.gov.hmrc.DefaultBuildSettings._
import scoverage.ScoverageKeys

lazy val appName: String = "advance-valuation-rulings-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

// To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(
    JUnitXmlReportPlugin
  ) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings))
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    play.sbt.routes.RoutesKeys.routesImport ++= Seq(
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
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*components.*;" +
      ".*Routes.*;.*viewmodels.govuk.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 87,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq(
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-rootdir",
      baseDirectory.value.getCanonicalPath,
      "-Wconf:cat=deprecation:ws,cat=feature:ws,cat=optimizer:ws,src=target/.*:s",
      "-deprecation", // Emit warning when using deprecated APIs.
      "-explaintypes", // Explain type errors in more detail.
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      "-language:higherKinds", // Allow higher-kinded types
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Wunused:imports", // Warn if an import selector is not referenced.
      "-Wunused:privates", // Warn if a private member is unused.
      "-Wunused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Wvalue-discard", // Warn when non-Unit expression results are unused.
      "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
      "-Ycache-macro-class-loader:last-modified" // and macro definitions. This can lead to performance improvements.
      // "-Xfatal-warnings" // Fail the compilation if there are any warnings.
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
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat, uglify),
    // only compress files generated by concat
    uglify / includeFilter := GlobFilter("application.js")
  )

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(itSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle it/Test/scalastyle")
