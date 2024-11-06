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

package views.helpers

import config.AppConfig
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.annotation.tailrec

@Singleton
class PortalUrlBuilder @Inject()(appConfig: AppConfig) {

  private val logger = Logger(getClass.getName)

  def buildPortalUrl(saUtr: Option[String], destinationPathKey: String): String =
    resolvePlaceHolder(
      url = appConfig.getPortalPath(destinationPathKey),
      tagsToBeReplacedWithData = Seq(
        ("<utr>", saUtr) // Add other placeholder strategies here as required
      )
    )

  @tailrec
  private def resolvePlaceHolder(url: String, tagsToBeReplacedWithData: Seq[(String, Option[Any])]): String =
    if (tagsToBeReplacedWithData.isEmpty) {
      url
    } else {
      resolvePlaceHolder(replace(url, tagsToBeReplacedWithData.head), tagsToBeReplacedWithData.tail)
    }

  private def replace(url: String, tagToBeReplacedWithData: (String, Option[Any])): String = {
    val (tagName, tagValueOption) = tagToBeReplacedWithData
    tagValueOption match {
      case Some(valueOfTag) => url.replace(tagName, valueOfTag.toString)
      case _ =>
        if (url.contains(tagName)) {
          logger.error(s"Failed to populate parameter $tagName in URL $url")
        }
        url
    }
  }
}
