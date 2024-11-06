/*
 * Copyright 2023 HM Revenue & Customs
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

package config

import play.api.Configuration

import javax.inject.{ Inject, Singleton }
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (val configuration: Configuration) {

  lazy val languageTranslationEnabled: Boolean =
    configuration.getOptional[Boolean]("features.languageTranslationEnabled").getOrElse(false)

  val en: String = "en"
  val cy: String = "cy"
  val defaultLanguage: Lang = Lang(en)

  private def loadConfig(key: String) =
    configuration.getOptional[String](key).getOrElse("")

  val btaHost = loadConfig(s"business-account.host")
  val btaBaseUrl = s"$btaHost/business-account"
  val ptaHost = loadConfig(s"personal-account.host")
  val ptaBaseUrl = s"$ptaHost/personal-account"

  def getPortalPath(pathKey: String): String =
    configuration.getOptional[String](s"portal.destinationPath.$pathKey").getOrElse("")
}
