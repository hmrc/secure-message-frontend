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

package config

import play.api.Logging

import play.api.http.HeaderNames.CACHE_CONTROL
import javax.inject.{ Inject, Singleton }
import play.api.i18n.MessagesApi
import play.api.mvc.{ Request, RequestHeader, Result }
import play.api.mvc.Results.{ NotFound, Unauthorized }
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.ErrorTemplate

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ErrorHandler @Inject() (errorTemplate: ErrorTemplate, val messagesApi: MessagesApi)(implicit
  val ec: ExecutionContext,
  appConfig: AppConfig
) extends FrontendErrorHandler with Logging {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
    request: RequestHeader
  ): Future[Html] = {
    implicit val req: Request[_] = Request(request, "")
    Future.successful(errorTemplate(pageTitle, heading, message))
  }

  override def resolveError(rh: RequestHeader, ex: Throwable): Future[Result] =
    ex match {
      case _: MissingBearerToken =>
        logger.debug("[AuthenticationPredicate][async] Missing Bearer Token.")
        Future.successful(Unauthorized("Unauthorised request received - Missing Bearer Token"))
      case _: BearerTokenExpired =>
        logger.debug("[AuthenticationPredicate][async] Bearer Token Timed Out.")
        Future.successful(Unauthorized("Unauthorised request received - Bearer Token Timed Out"))
      case _: NoActiveSession =>
        logger.debug("[AuthenticationPredicate][async] No Active Auth Session.")
        Future.successful(Unauthorized("Unauthorised request received - No Active Auth Session"))
      case _: AuthorisationException =>
        logger.debug("[AuthenticationPredicate][async] Unauthorised request.")
        Future.successful(Unauthorized("Unauthorised request received"))
      case _: NotFoundException =>
        notFoundTemplate(rh)
          .map(html => NotFound(html).withHeaders(CACHE_CONTROL -> "no-cache"))
      case _ => super.resolveError(rh, ex)
    }
}
