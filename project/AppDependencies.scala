import play.core.PlayVersion
import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-27" % "3.3.0",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"         % "0.35.0-play-27",
    "uk.gov.hmrc"             %% "play-frontend-govuk"        % "0.57.0-play-27",
    "uk.gov.hmrc"             %% "play-language"              % "4.7.0-play-27"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % "3.3.0" % Test,
    "org.scalatest"           %% "scalatest"                % "3.0.7"  % Test,
    "org.jsoup"               %  "jsoup"                    % "1.13.1" % Test,
    "com.typesafe.play"       %% "play-test"                % current  % Test,
    "org.pegdown" % "pegdown" % "1.6.0",
    "org.mockito"             % "mockito-core"                             % "3.7.7",
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.36.8" % "test, it",
    "com.typesafe.play" %% "play-test" % PlayVersion.current ,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"  % "test, it"
  )
}
