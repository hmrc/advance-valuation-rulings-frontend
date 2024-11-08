import sbt.Setting
import scoverage.ScoverageKeys.*

object CodeCoverageSettings {
  private val excludedPackages: Seq[String] = Seq(
    "<empty>",
    ".*components.*",
    ".*Routes.*",
    ".*viewmodels.govuk.*"
  )

  val settings: Seq[Setting[?]] = Seq(
    coverageExcludedPackages := excludedPackages.mkString(","),
    coverageMinimumStmtTotal := 95,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  )
}
