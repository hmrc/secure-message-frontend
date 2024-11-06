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

import model.utils.DateFormats
import play.api.libs.json.*

import java.time.{Instant, LocalDate}

case class MessageListItem(
  id: String,
  subject: String,
  validFrom: LocalDate,
  taxpayerName: Option[TaxpayerName] = None,
  readTime: Option[Instant],
  sentInError: Boolean,
  replyTo: Option[String] = None,
  messageDesc: Option[String] = None,
  counter: Option[Int] = None,
  language: Option[String] = None
) {
  def lang: String = language.getOrElse("en")
}

object MessageListItem {
  implicit val localDateFormat: Format[LocalDate] =
    Format(Reads.DefaultLocalDateReads, Writes.DefaultLocalDateWrites) // JavaFormat .localDateFormats
  implicit val dateTimeFormat: Format[Instant] = DateFormats.instantFormats
  implicit val taxpayerNameFmt: Format[TaxpayerName] = Json.format[TaxpayerName]
  implicit val messageListItemFormat: Format[MessageListItem] = Json.format[MessageListItem]
}
