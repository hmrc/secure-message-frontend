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
import forms.MessageFormProvider
import javax.inject.Singleton
import models.{ Conversation, CustomerMessage, Message }
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{ I18nSupport, Lang, Langs }
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents, Request, Result }
import play.twirl.api.{ Html, HtmlFormat }
import uk.gov.hmrc.auth.core.{ AuthConnector, AuthorisedFunctions }
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.panel.Panel
import uk.gov.hmrc.http.{ HeaderCarrier, NotFoundException }
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.helpers.HtmlUtil._
import views.html.partials.{ conversationView, letterView, messageContent, messageReply, messageResult }
import views.viewmodels.{ ConversationView, MessageReply, MessageView }

import scala.concurrent.{ ExecutionContext, Future }

@SuppressWarnings(Array("org.wartremover.warts.All"))
@Singleton
class MessageController @Inject()(
  controllerComponents: MessagesControllerComponents,
  secureMessageConnector: SecureMessageConnector,
  messageContent: messageContent,
  messageReply: messageReply,
  messageResult: messageResult,
  conversationView: conversationView,
  letterView: letterView,
  val authConnector: AuthConnector,
  formProvider: MessageFormProvider)(implicit ec: ExecutionContext, langs: Langs)
    extends FrontendController(controllerComponents) with I18nSupport with AuthorisedFunctions with Logging {

  implicit val lang: Lang = langs.availables.head

  def displayMessage(
    clientService: String,
    rawId: String,
    showReplyForm: Boolean
  ): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    def contentHelper[A](content: String => Future[A], view: A => Html): Future[Result] =
      content(rawId).map { a =>
        Ok(view(a))
      }
    rawId match {
      case Id("letter", _) =>
        authorised() { contentHelper(secureMessageConnector.getLetterContent _, letterView.apply _) }
      case Id("conversation", id) =>
        authorised() {
          contentHelper(
            (s: String) => {
              val replyFormActionUrl = s"/$clientService/messages/$id"
              val replyFormUrl = s"$replyFormActionUrl?showReplyForm=true#reply-form"
              val replyForm =
                messageReply(
                  MessageReply(showReplyForm, replyFormActionUrl, getReplyIcon(replyFormUrl), Seq.empty[String], ""))
              secureMessageConnector.getConversationContent(s).map(getConversationView(_, replyForm))
            },
            conversationView.apply _
          )
        }
      case _ => Future.successful(BadRequest("Invalid URL path"))
    }
  }

  //legacy
  def display(
    clientService: String,
    client: String,
    conversationId: String,
    showReplyForm: Boolean
  ): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    val replyFormActionUrl = s"/$clientService/conversation/$client/$conversationId"
    val replyFormUrl = s"$replyFormActionUrl?showReplyForm=true#reply-form"
    authorised() {
      secureMessageConnector
        .getConversationContent(conversationId)
        .flatMap { conversation =>
          val messages = {
            messagePartial(conversation.messages)
          }
          val firstMessage = messages.headOption.getOrElse(
            throw new NotFoundException("There can't be a conversation without a message"))
          val replyForm =
            messageReply(
              MessageReply(showReplyForm, replyFormActionUrl, getReplyIcon(replyFormUrl), Seq.empty[String], ""))
          Future.successful(
            Ok(
              conversationView(
                ConversationView(
                  conversation.subject,
                  firstMessage,
                  replyForm,
                  messages.tail,
                  Seq.empty[String]
                ))))
        }
    }
  }

  private def getConversationView(conversation: Conversation, replyForm: Html)(
    implicit request: Request[_]): ConversationView = {

    val messages = {
      messagePartial(conversation.messages)
    }
    val firstMessage =
      messages.headOption.getOrElse(throw new NotFoundException("There can't be a conversation without a message"))
    ConversationView(
      conversation.subject,
      firstMessage,
      replyForm,
      messages.tail,
      Seq.empty[String]
    )
  }

  val form: Form[CustomerMessage] = formProvider()

  def saveReplyMessage(clientService: String, id: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    val replyFormActionUrl = s"/$clientService/conversation/$id"
    val replyFormUrl = s"$replyFormActionUrl?showReplyForm=true#reply-form"
    authorised() {
      form
        .bindFromRequest()
        .fold(
          form => {
            secureMessageConnector
              .getConversationContent(id)
              .flatMap {
                conversation =>
                  val messages = {
                    messagePartial(conversation.messages)
                  }
                  val firstMessage = messages.headOption.getOrElse(
                    throw new NotFoundException("There can't be a conversation without a message"))
                  val replyForm =
                    messageReply(
                      MessageReply(
                        showReplyForm = true,
                        replyFormActionUrl,
                        getReplyIcon(replyFormUrl),
                        form.errors.map(_.message),
                        content = form.data.getOrElse("content", "")))
                  Future.successful(
                    BadRequest(
                      conversationView(
                        ConversationView(
                          conversation.subject,
                          firstMessage,
                          replyForm,
                          messages.tail,
                          form.errors.map(_.message)))))
              }

          },
          message =>
            secureMessageConnector.saveCustomerMessage(id, message).map { sent =>
              val redirectURL = s"/$clientService/messages/$id/result"
              if (sent) Ok(redirectURL) else BadGateway("Failed to send message")
          }
        )
    }
  }

  //legacy
  def saveReply(clientService: String, client: String, encodedId: String): Action[AnyContent] = Action.async {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
      val replyFormActionUrl = s"/$clientService/conversation/$client/$encodedId"
      val replyFormUrl = s"$replyFormActionUrl?showReplyForm=true#reply-form"
      authorised() {
        form
          .bindFromRequest()
          .fold(
            form => {
              secureMessageConnector
                .getConversationContent(encodedId)
                .flatMap {
                  conversation =>
                    val messages = {
                      messagePartial(conversation.messages)
                    }
                    val firstMessage = messages.headOption.getOrElse(
                      throw new NotFoundException("There can't be a conversation without a message"))
                    val replyForm =
                      messageReply(
                        MessageReply(
                          showReplyForm = true,
                          replyFormActionUrl,
                          getReplyIcon(replyFormUrl),
                          form.errors.map(_.message),
                          content = form.data.getOrElse("content", "")))
                    Future.successful(
                      BadRequest(
                        conversationView(
                          ConversationView(
                            conversation.subject,
                            firstMessage,
                            replyForm,
                            messages.tail,
                            form.errors.map(_.message)))))
                }

            },
            message =>
              secureMessageConnector.saveCustomerMessage(encodedId, message).map { sent =>
                val redirectURL = s"/$clientService/conversation/$client/$encodedId/result"
                if (sent) Ok(redirectURL) else BadGateway("Failed to send message")
            }
          )
      }
  }

  def displayResult(clientService: String): Action[AnyContent] = Action.async { implicit request: Request[_] =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    authorised() {
      Future.successful(
        Ok(
          messageResult(
            s"/$clientService/messages",
            Panel(
              title = Text(messagesApi("confirmation.panel.title")),
              content = Text(messagesApi("confirmation.panel.content"))
            ))))
    }
  }

  //legacy
  def result(clientService: String, client: String, conversationId: String): Action[AnyContent] = Action.async {
    implicit request: Request[_] =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
      authorised() {
        // FIXME - log statement is just to keep compiler happy - do we really need these parameters?
        logger.debug(s"service: $clientService, client: $client, conversation: $conversationId")
        Future.successful(
          Ok(
            messageResult(
              s"/$clientService/messages",
              Panel(
                title = Text(messagesApi("confirmation.panel.title")),
                content = Text(messagesApi("confirmation.panel.content"))
              ))))
      }
  }

  private[controllers] def messagePartial(messages: List[Message])(
    implicit request: Request[_]): List[HtmlFormat.Appendable] =
    messages
      .sortBy(_.senderInformation.sent.getMillis)(Ordering[Long].reverse)
      .map(
        message =>
          messageContent(
            MessageView(
              message.senderInformation.name,
              message.senderInformation.sent,
              message.firstReader.map(_.read),
              decodeBase64String(message.content)
            )))

  object Id {
    def apply(messageType: String, id: String): String =
      encodeBase64String(s"$messageType/$id")

    def unapply(s: String): Option[(String, String)] = decodeBase64String(s).split("/").toList match {
      case messageType :: id :: _ => Some((messageType, id))
      case _                      => None
    }
  }

}
