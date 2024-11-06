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

import play.api.libs.json.*

final case class MessagesWithCount(items: Seq[MessageListItem], count: MessagesCounts)
object MessagesWithCount {
  implicit val format: Format[MessagesWithCount] = Json.format[MessagesWithCount]
}

final case class MessagesCounts(total: Int, unread: Int) {
  lazy val read = total - unread
}
object MessagesCounts {
  def transformReadPreference(readPreference: Option[ReadPreference.Value]): MessagesCounts => MessageCount =
    count =>
      readPreference match {
        case Some(ReadPreference.Both) | None => MessageCount(count.total)
        case Some(ReadPreference.No)          => MessageCount(count.unread)
        case _                                => MessageCount(count.read)
      }

  implicit val format: Format[MessagesCounts] = Json.format[MessagesCounts]
}
