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

package controllers

import com.google.inject.Inject
import connectors.SecureMessageConnector
import models.{ ConversationView, Message, MessageView }
import play.api.i18n.I18nSupport
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents, Request }
import uk.gov.hmrc.auth.core.{ AuthConnector, AuthorisedFunctions }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.helpers.HtmlUtil.{ readableTime, _ }
import views.html.partials.{ conversation, messageContent }
import javax.inject.Singleton
import scala.concurrent.{ ExecutionContext, Future }

@SuppressWarnings(Array("org.wartremover.warts.All"))
@Singleton
class ConversationController @Inject()(
  controllerComponents: MessagesControllerComponents,
  secureMessageConnector: SecureMessageConnector,
  messageContent: messageContent,
  conversation: conversation,
  val authConnector: AuthConnector
)(implicit ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with AuthorisedFunctions {

  def display(clientService: String, client: String, conversationId: String): Action[AnyContent] = Action.async {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
      authorised() {
        secureMessageConnector
          .getConversation(client, conversationId)
          .flatMap { conversationMessage =>
            Future.successful(
              Ok(
                conversation(
                  ConversationView(
                    s"${conversationMessage.subject}",
                    messagePartial(conversationMessage.messages),
                    backToConversationsLink(clientService)))))
          }
      }
  }

  def saveReply(clientService: String, client: String, conversationId: String): Action[AnyContent] = Action {

    Created(s"Saved reply successfull with client $clientService client $client and conversationId $conversationId")
  }

  def response(clientService: String, client: String, conversationId: String): Action[AnyContent] = Action {

    Ok(s"$clientService with client $client with conversationId $conversationId")
  }

  private[controllers] def messagePartial(messages: List[Message])(implicit request: Request[_]) =
    messages.map(
      message =>
        messageContent(
          MessageView(
            message.senderInformation.name,
            sentMessageConversationText(readableTime(message.senderInformation.created)),
            firstReadMessageConversationText(message.firstReader),
            readMessageConversationText,
            message.content
          )))
}
