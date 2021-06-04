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

package base

import play.api.i18n.Messages.UrlMessageSource
import play.api.i18n.{ DefaultMessagesApi, Lang, Messages, MessagesApi, MessagesImpl }

@SuppressWarnings(Array("org.wartremover.warts.EitherProjectionPartial"))
trait LanguageStubs {

  val messagesResourceEn: Map[String, String] =
    Messages
      .parse(UrlMessageSource(this.getClass.getClassLoader.getResource("messages")), "")
      .right
      .get

  val messagesResourceCy: Map[String, String] =
    Messages
      .parse(UrlMessageSource(this.getClass.getClassLoader.getResource("messages.cy")), "")
      .right
      .get

  val messagesApi: MessagesApi = new DefaultMessagesApi(
    messages = Map(
      "en" -> messagesResourceEn,
      "cy" -> messagesResourceCy
    )
  )

  val messagesEn = MessagesImpl(Lang("en"), messagesApi)
  val messagesCy = MessagesImpl(Lang("cy"), messagesApi)
}
