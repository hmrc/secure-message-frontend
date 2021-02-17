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

import config.AppConfig
import connectors.SecureMessageConnector
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{ MessagesControllerComponents, _ }
import uk.gov.hmrc.auth.core.{ AuthConnector, AuthorisedFunctions }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.partials.conversationInbox
import views.viewmodels.ConversationInbox

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ConversationInboxController @Inject()(
  appConfig: AppConfig,
  controllerComponents: MessagesControllerComponents,
  inbox: conversationInbox,
  secureMessageConnector: SecureMessageConnector,
  val authConnector: AuthConnector)(implicit ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with AuthorisedFunctions {

  private val logger = Logger(getClass)

  implicit val config: AppConfig = appConfig

  def display(clientService: String, apiFilters: Option[List[String]]): Action[AnyContent] = Action.async {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
      val apiFilterValues = apiFilters.getOrElse(List("NONE")).mkString("&apiFilter=")
      // FIXME the logging is just to keep the compiler happy until the filters are implemented
      logger.info(s"apiFilter=$apiFilterValues")
      authorised() {
        secureMessageConnector.getConversationList().flatMap { conversations =>
          val messages = this.messagesApi.preferred(request)
          Future.successful(
            Ok(inbox.apply(ConversationInbox(clientService, messages("conversation.inbox.title"), conversations))))
        }
      }
  }

}
