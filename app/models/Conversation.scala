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
import play.api.libs.json.{ Format, Json, Reads, Writes }

final case class Conversation(
  client: String,
  conversationId: String,
  status: String,
  tags: Option[Map[String, String]],
  subject: String,
  language: String,
  messages: List[Message])

object Conversation {
  implicit val conversationFormat: Reads[Conversation] = Json.reads[Conversation]
}

final case class Message(
  senderInformation: SenderInformation,
  firstReader: Option[FirstReaderInformation],
  content: String)

object Message {
  implicit val messageReads: Reads[Message] = Json.reads[Message]
}

final case class FirstReaderInformation(name: Option[String], read: Instant)

object FirstReaderInformation {

  private val dateFormatString = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateFormat: Format[Instant] = Format(Reads.instantReads(dateFormatString), Writes.DefaultInstantWrites)
  implicit val firstReaderFormat: Format[FirstReaderInformation] = Json.format[FirstReaderInformation]
}

final case class SenderInformation(name: Option[String], sent: Instant, self: Boolean)

object SenderInformation {

  private val dateFormatString = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateFormat: Format[Instant] = Format(Reads.instantReads(dateFormatString), Writes.DefaultInstantWrites)
  implicit val senderInformationFormat: Reads[SenderInformation] = Json.reads[SenderInformation]
}
