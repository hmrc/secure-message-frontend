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
import connectors.SecureMessageConnector
import controllers.generic.models.{ CustomerEnrolment, Tag }
import controllers.utils.QueryStringValidation
import play.api.i18n.{ I18nSupport, Lang }
import play.api.mvc.{ MessagesControllerComponents, _ }
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{ AuthConnector, AuthorisedFunctions }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.partials.messageInbox
import views.viewmodels.MessageInbox

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class MessagesInboxController @Inject()(
  appConfig: AppConfig,
  controllerComponents: MessagesControllerComponents,
  inbox: messageInbox,
  secureMessageConnector: SecureMessageConnector,
  val authConnector: AuthConnector)(implicit ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with AuthorisedFunctions
    with QueryStringValidation {

  implicit val config: AppConfig = appConfig

  def display(
    clientService: String,
    enrolmentKeys: Option[List[String]],
    customerEnrolments: Option[List[CustomerEnrolment]],
    tags: Option[List[Tag]]): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    implicit val lang: Lang = request.lang

    validateQueryParameters(request.queryString, "enrolment", "enrolmentKey", "tag", "sent") match {
      case Left(e) => Future.successful(BadRequest(e.getMessage))
      case _ =>
        authorised().retrieve(Retrievals.allEnrolments) { enrolments =>
          val enrolmentKeysToCheck =
            if (request.queryString.isEmpty) Some(enrolments.enrolments.map(_.key).toList) else enrolmentKeys
          secureMessageConnector.getInboxList(enrolmentKeysToCheck, customerEnrolments, tags, lang).flatMap {
            inboxList =>
              val messages = this.messagesApi.preferred(request)
              Future.successful(
                Ok(
                  inbox.apply(
                    MessageInbox(
                      clientService,
                      messages("conversation.inbox.title"),
                      inboxList.size,
                      inboxList.count(_.unreadMessages),
                      inboxList))))
          }
        }
    }
  }
}
