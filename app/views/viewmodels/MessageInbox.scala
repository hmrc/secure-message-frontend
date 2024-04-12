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

package views.viewmodels

import models.MessageHeader
import play.api.libs.functional.syntax._
import play.api.libs.json.{ Json, OWrites, Reads, __ }

final case class MessageInbox(
  clientService: String,
  heading: String,
  total: Int,
  unread: Int,
  conversationHeaders: List[MessageHeader]
)
object MessageInbox {

  def defaultObject: MessageInbox = MessageInbox("", "", 0, 0, List.empty[MessageHeader])

  implicit def jsonReads: Reads[MessageInbox] =
    (
      (__ \ "clientService").readWithDefault[String](defaultObject.clientService) and
        (__ \ "heading").readWithDefault[String](defaultObject.heading) and
        (__ \ "total").readWithDefault[Int](defaultObject.total) and
        (__ \ "unread").readWithDefault[Int](defaultObject.unread) and
        (__ \ "conversationHeaders").readWithDefault[List[MessageHeader]](defaultObject.conversationHeaders)
    )(MessageInbox.apply _)

  implicit def jsonWrites: OWrites[MessageInbox] = Json.writes[MessageInbox]

}
