/*
 * Copyright 2022 HM Revenue & Customs
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

import uk.gov.hmrc.DefaultBuildSettings.{ defaultSettings, integrationTestSettings, scalaSettings }
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import play.twirl.sbt.Import.TwirlKeys
import play.sbt.routes.RoutesKeys

val appName = "secure-message-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, BuildInfoPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.13.8",
    name := appName,
    RoutesKeys.routesImport ++= Seq("models._", "controllers.generic.models._", "controllers.binders._"),
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    TwirlKeys.templateImports ++= Seq(
      "config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
    ),
    PlayKeys.playDefaultPort := 9055,
    retrieveManaged := true,
    update / evictionWarningOptions :=
      EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := InjectedRoutesGenerator
  )
  .settings(
    resolvers += Resolver.jcenterRepo,
    inConfig(IntegrationTest)(
      scalafmtCoreSettings ++
        Seq(compile / compileInputs := Def.taskDyn {
          val task = (resolvedScoped.value.scope / scalafmt.key) / test
          val previousInputs = (compile / compileInputs).value
          task.map(_ => previousInputs)
        }.value)
    )
  )
  .settings(ScoverageSettings())
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
  )
  .settings(
    // To get rid of "version conflict" error where secure-message-frontend is using scala-xml 2.x.x
    // and both HMRC and non-HMRC dependencies are still using scala-xml 1.x.x
    libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)
  )
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:msg=multiarg infix syntax&src=BuildInfo.scala:s",
      // Silence unused warnings on Play `routes` and `twirl` files
      "-Wconf:cat=unused-imports&src=.*routes.*:s",
      "-Wconf:cat=unused-privates&src=.*routes.*:s",
      "-Wconf:src=twirl/.*:s",
      "-feature",
      "-Xlint",
      "-language:postfixOps",
      "-language:reflectiveCalls"
    )
  )

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
compileScalastyle := (Compile / scalastyle).toTask("").value
(Compile / compile) := ((Compile / compile) dependsOn compileScalastyle).value

scalafmtOnCompile := true

(Compile / compile) := ((Compile / compile) dependsOn dependencyUpdates).value
dependencyUpdatesFilter -= moduleFilter(name = "flexmark-all")
dependencyUpdatesFilter -= moduleFilter(organization = "uk.gov.hmrc")
dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang")
dependencyUpdatesFilter -= moduleFilter(organization = "com.github.ghik")
dependencyUpdatesFilter -= moduleFilter(organization = "com.typesafe.play")
dependencyUpdatesFilter -= moduleFilter(organization = "org.scalatest")
dependencyUpdatesFilter -= moduleFilter(organization = "org.scalatestplus.play")
dependencyUpdatesFilter -= moduleFilter(name = "enumeratum-play")
dependencyUpdatesFailBuild := false
Compile / doc / sources := Seq.empty

val codeStyleIntegrationTest = taskKey[Unit]("enforce code style then integration test")

// and then in settings...
Project.inConfig(IntegrationTest)(ScalastylePlugin.rawScalastyleSettings()) ++
  Seq(
    IntegrationTest / scalastyleConfig := (scalastyle / scalastyleConfig).value,
    IntegrationTest / scalastyleTarget := target.value / "scalastyle-it-results.xml",
    IntegrationTest / scalastyleFailOnError := (scalastyle / scalastyleFailOnError).value,
    (IntegrationTest / scalastyleFailOnWarning) := (scalastyle / scalastyleFailOnWarning).value,
    IntegrationTest / scalastyleSources := (IntegrationTest / unmanagedSourceDirectories).value,
    codeStyleIntegrationTest := (IntegrationTest / scalastyle).toTask("").value,
    (IntegrationTest / test) := ((IntegrationTest / test) dependsOn codeStyleIntegrationTest).value
  )
