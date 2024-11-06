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


import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.0.0"

  val compile = Seq(
    "uk.gov.hmrc"    %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"    %% "play-frontend-hmrc-play-30" % "10.0.0",
    "org.typelevel"  %% "cats-core"                  % "2.12.0",
    "com.beachape"   %% "enumeratum"            % "1.7.3",
    "com.beachape"   %% "enumeratum-play-json"  % "1.8.0",
    "net.codingwell" %% "scala-guice"                % "6.0.0",
    "uk.gov.hmrc"    %% "play-partials-play-30"      % "10.0.0",
    "org.jsoup"       % "jsoup"                      % "1.17.2",
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"     % "7.0.1"          % Test,
    "org.scalatestplus"      %% "mockito-3-4"            % "3.2.10.0"       % Test,
    "org.mockito"            % "mockito-core"            % "5.10.0"         % Test
  )
}
