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
import play.api.i18n.I18nSupport
import play.api.mvc.{ MessagesControllerComponents, _ }
import uk.gov.hmrc.auth.core.{ AuthConnector, AuthorisedFunctions }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{ Inject, Singleton }
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ApiController @Inject()(
  appConfig: AppConfig,
  controllerComponents: MessagesControllerComponents,
  secureMessageConnector: SecureMessageConnector,
  val authConnector: AuthConnector)(implicit ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with AuthorisedFunctions
    with QueryStringValidation {

  implicit val config: AppConfig = appConfig

  def count(
    enrolmentKeys: Option[List[String]],
    customerEnrolments: Option[List[CustomerEnrolment]],
    tags: Option[List[Tag]]): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    validateQueryParameters(request.queryString, "enrolment", "enrolmentKey", "tag", "sent") match {
      case Left(e) => Future.successful(BadRequest(e.getMessage))
      case _ =>
        authorised().retrieve(Retrievals.allEnrolments) { enrolments =>
          val enrolmentKeysToCheck =
            if (request.queryString.isEmpty) Some(enrolments.enrolments.map(_.key).toList) else enrolmentKeys
          secureMessageConnector.getCount(enrolmentKeysToCheck, customerEnrolments, tags).flatMap { messageCount =>
            Future.successful(Ok(Json.toJson(messageCount)))
          }
        }
    }
  }
}
