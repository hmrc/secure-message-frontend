/*
 * Copyright 2021 HM Revenue & Customs
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

import org.joda.time.DateTime
import play.api.libs.json.JodaReads.jodaDateReads
import play.api.libs.json.JodaWrites.jodaDateWrites
import play.api.libs.json.{ Format, Json, Reads }

final case class Conversation(
  client: String,
  conversationId: String,
  status: String,
  tags: Map[String, String],
  subject: String,
  language: String,
  messages: List[Message])

object Conversation {
  implicit val conversationFormat: Reads[Conversation] = Json.reads[Conversation]
}

final case class Message(senderInformation: SenderInformation, firstReader: Option[FirstReader], content: String)

object Message {
  implicit val messageReads: Reads[Message] = Json.reads[Message]
}

final case class FirstReader(name: String, read: DateTime)

object FirstReader {

  private val dateFormatString = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateFormat: Format[DateTime] = Format(jodaDateReads(dateFormatString), jodaDateWrites(dateFormatString))
  implicit val firstReaderFormat: Reads[FirstReader] = Json.reads[FirstReader]
}

final case class SenderInformation(name: String, created: DateTime)

object SenderInformation {

  private val dateFormatString = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateFormat: Format[DateTime] = Format(jodaDateReads(dateFormatString), jodaDateWrites(dateFormatString))
  implicit val senderInformationFormat: Reads[SenderInformation] = Json.reads[SenderInformation]
}
//
//object FakeData {
//  val conversations: String => List[Conversation] = (clientService: String) =>
//    List(
//      Conversation(
//        "111",
//        s"/$clientService/conversation-message/111",
//        "HMRC exports",
//        "MRN 20GB16046891253600",
//        "12 January 2021"),
//      Conversation(
//        "222",
//        s"/$clientService/conversation-message/222",
//        "HMRC exports",
//        "MRN 20GB16046891253601",
//        "13 January 2021"),
//      Conversation(
//        "333",
//        s"/$clientService/conversation-message/222",
//        "HMRC exports",
//        "MRN 20GB16046891253601",
//        "13 January 2021"),
//      Conversation(
//        "444",
//        s"/$clientService/conversation-message/222",
//        "HMRC exports",
//        "MRN 20GB16046891253601",
//        "13 January 2021"),
//      Conversation(
//        "555",
//        s"/$clientService/conversation-message/222",
//        "HMRC exports",
//        "MRN 20GB16046891253601",
//        "13 January 2021"),
//      Conversation(
//        "666",
//        s"/$clientService/conversation-message/222",
//        "HMRC exports",
//        "MRN 20GB16046891253601",
//        "13 January 2021")
//  )
//
//  val messages: Seq[ConversationMessage] = List(
//    ConversationMessage(
//      "111",
//      "<div>\n <p class=\"govuk-body\">Dear Customer</p>\n         " +
//        " <p class=\"govuk-body\">I found missing information for Customs Declaration MRN 20GB16046891253600" +
//        " for a shipment of fireworks.</p>\n" +
//        "<p class=\"govuk-body\">Please upload this form before your shipment can be cleared for export:</p>\n  " +
//        "              <p class=\"govuk-body\">C672 form. “This form must accompany shipments of waste as mentioned in" +
//        " Regulation 1013/2006 (OJ L 190) – Article 18 and Annex VII.”</p>\n       " +
//        "         <p class=\"govuk-body\">Robin Newman</p>\n                </div>"
//    ),
//    ConversationMessage(
//      "222",
//      "           <div>\n   " +
//        "             <p class=\"govuk-body\">Dear Customer</p>\n        " +
//        "        <p class=\"govuk-body\">I found missing information for Customs Declaration " +
//        "MRN 20GB16046891253600 for a shipment of fireworks.</p>\n          " +
//        "      <p class=\"govuk-body\">Please upload this form before your shipment can be cleared for export:</p>\n   " +
//        "             <p class=\"govuk-body\">C672 form. “This form must accompany shipments" +
//        " of waste as mentioned in Regulation 1013/2006 (OJ L 190) – Article 18 and Annex VII.”</p>\n     " +
//        "           <p class=\"govuk-body\">Robin Newman</p>\n                </div>"
//    ),
//    ConversationMessage(
//      "333",
//      "           <div>\n           " +
//        "     <p class=\"govuk-body\">Dear Customer</p>\n      " +
//        "          <p class=\"govuk-body\">I found missing information for Customs Declaration " +
//        "MRN 20GB16046891253600 for a shipment of fireworks.</p>\n     " +
//        "           <p class=\"govuk-body\">Please upload this form before your shipment can be cleared for export:</p>\n " +
//        "               <p class=\"govuk-body\">C672 form. " +
//        "“This form must accompany shipments of waste as mentioned in Regulation 1013/2006 (OJ L 190)" +
//        " – Article 18 and Annex VII.”</p>\n                <p class=\"govuk-body\">Robin Newman</p>\n  " +
//        "              </div>"
//    ),
//    ConversationMessage(
//      "444",
//      "           <div>\n  " +
//        "              <p class=\"govuk-body\">Dear Customer</p>\n " +
//        "               <p class=\"govuk-body\">I found missing information for Customs Declaration MRN " +
//        "20GB16046891253600 for a shipment of fireworks.</p>\n            " +
//        "    <p class=\"govuk-body\">Please upload this form before your shipment can be cleared for export:</p>\n    " +
//        "            <p class=\"govuk-body\">C672 form. “This form must accompany shipments of waste " +
//        "as mentioned in Regulation 1013/2006 (OJ L 190) – Article 18 and Annex VII.”</p>\n   " +
//        "             <p class=\"govuk-body\">Robin Newman</p>\n                </div>"
//    ),
//    ConversationMessage(
//      "555",
//      "           <div>\n     " +
//        "           <p class=\"govuk-body\">Dear Customer</p>\n     " +
//        "           <p class=\"govuk-body\">I found missing information for Customs Declaration MRN 20GB16046891253600" +
//        " for a shipment of fireworks.</p>\n          " +
//        "      <p class=\"govuk-body\">Please upload this form before your shipment can be cleared for export:</p>\n   " +
//        "             <p class=\"govuk-body\">C672 form. “This form must accompany shipments of waste as mentioned " +
//        "in Regulation 1013/2006 (OJ L 190) – Article 18 and Annex VII.”</p>\n       " +
//        "         <p class=\"govuk-body\">Robin Newman</p>\n                </div>"
//    ),
//    ConversationMessage(
//      "666",
//      "           <div>\n         " +
//        "       <p class=\"govuk-body\">Dear Customer</p>\n    " +
//        "            <p class=\"govuk-body\">I found missing information for Customs " +
//        "Declaration MRN 20GB16046891253600 for a shipment of fireworks.</p>\n     " +
//        "           <p class=\"govuk-body\">Please upload this form before your shipment can be cleared " +
//        "for export:</p>\n                <p class=\"govuk-body\">C672 form." +
//        " “This form must accompany shipments of waste as mentioned in Regulation 1013/2006 (OJ L 190) " +
//        "– Article 18 and Annex VII.”</p>\n                <p class=\"govuk-body\">Robin Newman</p>\n     " +
//        "           </div>"
//    )
//  )
//
//}
