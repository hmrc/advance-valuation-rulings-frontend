import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

import sbt.Def
import scoverage.ScoverageKeys

lazy val appName: String = "advance-valuation-rulings-frontend"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(
    JUnitXmlReportPlugin
  ) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(SbtDistributablesPlugin.publishingSettings: _*)
  .settings(inConfig(Test)(testSettings): _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(majorVersion := 0)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    scalaVersion := "2.13.10",
    name := appName,
    RoutesKeys.routesImport ++= Seq(
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
    PlayKeys.playDefaultPort := 9000,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*components.*;" +
      ".*Routes.*;.*viewmodels.govuk.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 78,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq(
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-rootdir",
      baseDirectory.value.getCanonicalPath,
      "-Wconf:cat=deprecation:ws,cat=feature:ws,cat=optimizer:ws,src=target/.*:s",
      // "-deprecation",                      // Emit warning when using deprecated APIs. cannot enable due to twirl...
      "-encoding",
      "utf-8", // Specify character encoding used by source files.
      "-explaintypes", // Explain type errors in more detail.
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      "-language:higherKinds", // Allow higher-kinded types
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.

      // Warns and errors
      "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
      // "-Xfatal-warnings",                  // Disabled asFail the compilation if there are any warnings.

      "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
      "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
      "-Xlint:option-implicit", // Option.apply used implicit view.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
      "-Xlint:unused", // TODO check if we still need -Wunused below
      "-Xlint:nonlocal-return", // A return statement used an exception for flow control.
      "-Xlint:implicit-not-found", // Check @implicitNotFound and @implicitAmbiguous messages.
      "-Xlint:serial", // @SerialVersionUID on traits and non-serializable classes.
      "-Xlint:valpattern", // Enable pattern checks in val definitions.
      "-Xlint:eta-zero", // Warn on eta-expansion (rather than auto-application) of zero-ary method.
      "-Xlint:eta-sam", // Warn on eta-expansion to meet a Java-defined functional interface that is not explicitly annotated with @FunctionalInterface.
      "-Xlint:deprecation", // Enable linted deprecations.
      "-Wdead-code", // Warn when dead code is identified.
      "-Wextra-implicit", // Warn when more than one implicit parameter section is defined.
      "-Wmacros:both", // Lints code before and after applying a macro
      "-Wnumeric-widen", // Warn when numerics are widened.
      "-Woctal-literal", // Warn on obsolete octal syntax.
      "-Xlint:implicit-recursion", // Warn when an implicit resolves to an enclosing self-definition.
      "-Wunused:imports", // Warn if an import selector is not referenced.
      "-Wunused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Wunused:privates", // Warn if a private member is unused.
      "-Wunused:locals", // Warn if a local definition is unused.
      "-Wunused:explicits", // Warn if an explicit parameter is unused.
      "-Wunused:implicits", // Warn if an implicit parameter is unused.
      "-Wunused:params", // Enable -Wunused:explicits,implicits.
      "-Wunused:linted",
      "-Wvalue-discard", // Warn when non-Unit expression results are unused.

      "-Ybackend-parallelism",
      "8", // Enable paralellisation — change to desired number!
      "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
      "-Ycache-macro-class-loader:last-modified" // and macro definitions. This can lead to performance improvements.
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    resolvers ++= Seq(Resolver.jcenterRepo),
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
    uglify / includeFilter := GlobFilter("application.js"),
    scalafmtOnCompile := true
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it",
    baseDirectory.value / "test-utils"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  parallelExecution := false,
  fork := true
)
