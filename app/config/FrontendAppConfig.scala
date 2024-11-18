/*
 * Copyright 2024 HM Revenue & Customs
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

import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.annotation.unused

@Singleton
class FrontendAppConfig @Inject() (override val configuration: Configuration, @unused servicesConfig: ServicesConfig)
    extends AppConfig(configuration) {

  private def loadConfig(key: String) =
    configuration.getOptional[String](key).getOrElse("")

  override val btaHost = loadConfig(s"business-account.host")
  override val btaBaseUrl = s"$btaHost/business-account"
  override val ptaHost = loadConfig(s"personal-account.host")
  override val ptaBaseUrl = s"$ptaHost/personal-account"

  override def getPortalPath(pathKey: String): String =
    configuration.getOptional[String](s"portal.destinationPath.$pathKey").getOrElse("")
}
