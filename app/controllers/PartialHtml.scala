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

package controllers

import config.AppConfig
import connectors.MessageConnector
import model.{EncryptAndEncode, MessageListItem, MessagesCounts, ReadPreference}
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Request
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import views.helpers.PortalUrlBuilder
import views.html.*

import scala.annotation.unused
import scala.concurrent.{ExecutionContext, Future}

trait PartialHtml {

  type RenderView = Seq[MessageListItem] => HtmlFormat.Appendable

  def messageConnector: MessageConnector
  def portalUrlBuilder: PortalUrlBuilder
  def encryptAndEncode: EncryptAndEncode
  val appConfig: AppConfig

  def asLinkHtml(messagesInboxUrl: String, taxIdentifiers: List[String], regimes: List[String] = List())(implicit
    hc: HeaderCarrier,
    @unused request: Request[_],
    ec: ExecutionContext,
    messages: Messages
  ): Future[HtmlFormat.Appendable] =
    messageConnector
      .messageCount(MessagesCounts.transformReadPreference(Some(ReadPreference.No)), taxIdentifiers, regimes)
      .map { mc =>
        message_inbox_link(Some(mc), messagesInboxUrl)
      }
      .recover { case _ => message_inbox_link(None, messagesInboxUrl) }

  def asListHtml(enrolments: Enrolments, taxIdentifiers: List[String] = List(), regimes: List[String] = List())(implicit
    hc: HeaderCarrier,
    request: Request[_],
    ec: ExecutionContext,
    messagesProvider: Messages
  ): Future[HtmlFormat.Appendable] = {
    implicit val lang: Lang = messagesProvider.lang
    val listHtmlView: RenderView =
      messages => {
        val saUtr = getEnrolmentIdentifier("IR-SA", enrolments)
        if (messages.isEmpty) {
          empty_list_partial(
            portalUrlBuilder,
            saUtr,
            taxIdentifiersPartial(enrolments, taxIdentifiers)
          )
        } else {
          list_partial(
            appConfig.btaBaseUrl,
            messages,
            portalUrlBuilder,
            saUtr,
            taxIdentifiersPartial(enrolments, taxIdentifiers),
            encryptAndEncode
          )
        }
      }
    listView(listHtmlView, taxIdentifiers, regimes, lang.language)
    // TODO: DC-563 content change required in order to make the explicit SA check go away here
  }

  def btaListHtml(enrolments: Enrolments, taxIdentifiers: List[String] = List(), regimes: List[String] = List())(
    implicit
    hc: HeaderCarrier,
    request: Request[_],
    ec: ExecutionContext,
    messagesProvider: Messages
  ): Future[HtmlFormat.Appendable] = {
    implicit val lang: Lang = messagesProvider.lang
    val btaListView: RenderView =
      messages =>
        bta_list_partial(
          appConfig.btaBaseUrl,
          messages,
          taxIdentifiersPartial(enrolments, taxIdentifiers),
          encryptAndEncode
        )

    listView(btaListView, taxIdentifiers, regimes, lang.language)
  }

  def listView(
    show: RenderView,
    taxIdentifiers: List[String],
    regimes: List[String] = List(),
    language: String
  )(implicit hc: HeaderCarrier, @unused request: Request[_], ec: ExecutionContext): Future[HtmlFormat.Appendable] =
    messageConnector.messages(taxIdentifiers, regimes, language).map { e =>
      show(filterRepliedTo2WM(e))
    }

  def filterRepliedTo2WM(messages: Seq[MessageListItem]): Seq[MessageListItem] = {
    val repliedToMessageIds = messages.filter(_.replyTo.isDefined).map(_.replyTo.get)
    messages.filterNot(e => repliedToMessageIds.contains(e.id))
  }

  def taxIdentifiersPartial(enrolments: Enrolments, taxIdentifiers: List[String])(implicit
    @unused hc: HeaderCarrier,
    @unused request: Request[_],
    messages: Messages
  ): Html = {
    val identifiers = Map(
      "nino"  -> "HMRC-NI",
      "sautr" -> "IR-SA",
      "ctutr" -> "IR-CT"
    )
    val identifiersToDisplay = (for {
      key                 <- taxIdentifiers
      identifier          <- identifiers.get(key)
      enrolmentIdentifier <- getEnrolmentIdentifier(identifier, enrolments)
    } yield (key, enrolmentIdentifier)).toMap
    tax_identifiers_partial(identifiersToDisplay)
  }

  def getEnrolmentIdentifier(key: String, enrolments: Enrolments): Option[String] =
    enrolments.enrolments.collectFirst { case Enrolment(`key`, enrolmentIdentifiers, _, _) =>
      enrolmentIdentifiers.head.value
    }
}
