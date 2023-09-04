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

import play.core.PlayVersion.current
import sbt._

object  AppDependencies {

  private val bootstrapVersion = "6.4.0"

  val compile = Seq(
    "uk.gov.hmrc"            %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc"            %% "play-frontend-hmrc"         % "7.7.0-play-28",
    "uk.gov.hmrc"            %% "play-language"              % "6.2.0-play-28",
    "com.typesafe.play"      %% "play-json-joda"             % "2.9.4",
    "com.iheart"             %% "play-swagger"               % "1.0.2",
    "org.typelevel"          %% "cats-core"                  % "2.9.0",
    "org.scala-lang.modules" %% "scala-xml"                  % "2.1.0",
    "com.beachape"           %% "enumeratum-play"            % "1.7.0",
    "net.codingwell"         %% "scala-guice"               % "5.1.1"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % bootstrapVersion         % Test,
    "org.jsoup"              % "jsoup"                     % "1.16.1"        % Test,
    "com.typesafe.play"      %% "play-test"                % current         % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0"         % "test, it",
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.10.0"      % "test, it",
    "org.jsoup"              % "jsoup"                     % "1.16.1"        % "test, it",
    "org.mockito"            % "mockito-core"              % "5.3.1"         % "test, it",
    "com.vladsch.flexmark"   % "flexmark-all"              % "0.36.8"        % "test, it",
    "org.pegdown"            % "pegdown"                   % "1.6.0"         % "test, it"
)

  val dependencyOverrides = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8" // swagger requires an older version of jackson than alpakka...
  )
}
