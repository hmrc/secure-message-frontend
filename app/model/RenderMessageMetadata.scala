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

package model

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import java.time.Instant

sealed trait RenderMessageMetadata extends Product with Serializable
object RenderMessageMetadata {
  final case class ReadMessageMetadata(rendererUrl: ServiceUrl) extends RenderMessageMetadata
  val readMessageReads: Reads[RenderMessageMetadata] =
    ((__ \ "renderUrl").read[ServiceUrl] and (__ \ "readTime").read[Instant]) { (url, _) =>
      ReadMessageMetadata(url)
    }

  final case class UnreadMessageMetadata(rendererUrl: ServiceUrl, setReadTimeUrl: ServiceUrl)
      extends RenderMessageMetadata
  val unreadMessageReads: Reads[RenderMessageMetadata] =
    ((__ \ "renderUrl").read[ServiceUrl] and (__ \ "markAsReadUrl").read[ServiceUrl])(UnreadMessageMetadata(_, _))

  implicit val reads: Reads[RenderMessageMetadata] = readMessageReads orElse unreadMessageReads
}
