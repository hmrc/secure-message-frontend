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

import uk.gov.hmrc.DefaultBuildSettings.{ defaultSettings, scalaSettings }
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import play.twirl.sbt.Import.TwirlKeys
import play.sbt.routes.RoutesKeys

val appName = "secure-message-frontend"

Global / majorVersion := 0
Global / scalaVersion := "2.13.12"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, BuildInfoPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
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
    inConfig(Test)(
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
  .settings(tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement)

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(`microservice` % "test->test")
  .settings(tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement)

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
compileScalastyle := (Compile / scalastyle).toTask("").value
(Compile / compile) := ((Compile / compile) dependsOn compileScalastyle).value

scalafmtOnCompile := true
