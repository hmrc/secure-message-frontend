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

package models

import java.time.Instant
import play.api.libs.json.{ Format, Json, OFormat, Reads, Writes }

final case class MessageHeader(
  messageType: MessageType,
  id: String,
  subject: String,
  issueDate: Instant,
  senderName: Option[String],
  unreadMessages: Boolean,
  count: Int,
  conversationId: Option[String],
  client: Option[String]
)

object MessageHeader {

  private val dateFormatString = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateFormat: Format[Instant] =
    Format(Reads.instantReads(dateFormatString), Writes.DefaultInstantWrites)

  implicit val conversationHeaderReads: OFormat[MessageHeader] = Json.format[MessageHeader]

}
