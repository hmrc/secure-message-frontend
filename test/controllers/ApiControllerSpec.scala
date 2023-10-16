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

import akka.util.Timeout
import config.AppConfig
import connectors.SecureMessageConnector
import controllers.generic.models.{ CustomerEnrolment, Tag }
import models.Count
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import play.api.test.Helpers.{ GET, contentAsString, status }
import play.api.test.{ FakeRequest, Helpers }
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class ApiControllerSpec extends PlaySpec with MockitoSugar with MockAuthConnector {

  "Api Controller" must {

    "returns a count of unread and total messages" in new TestCase {
      val totalMessagesCount: Long = 5
      val unreadMessagesCount: Long = 2
      when(
        mockAuthConnector
          .authorise(any[Predicate], any[Retrieval[Enrolments]])(any[HeaderCarrier](), any[ExecutionContext]()))
        .thenReturn(Future.successful(Enrolments(Set())))
      when(
        mockSecureMessageConnector.getCount(
          ArgumentMatchers.eq(Some(List("HMRC-CUS-ORG"))),
          ArgumentMatchers.eq(Some(List(CustomerEnrolment("HMRC-CUS-ORG", "EORIName", "GB7777777777")))),
          ArgumentMatchers.eq(Some(List(Tag("notificationType", "CDS Exports"))))
        )(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future(Count(total = totalMessagesCount, unread = unreadMessagesCount)))
      private val controller = new ApiController(
        mockAppConfig,
        Helpers.stubMessagesControllerComponents(),
        mockSecureMessageConnector,
        mockAuthConnector)
      private val result = controller.count(
        Some(List("HMRC-CUS-ORG")),
        Some(List(CustomerEnrolment("HMRC-CUS-ORG", "EORIName", "GB7777777777"))),
        Some(List(Tag("notificationType", "CDS Exports")))
      )(FakeRequest(method = GET, path = "/messages/count?enrolmentKey=HMRC-CUS-ORG"))
      status(result) mustBe OK
    }

    "return BAD REQUEST when query filter parameters are not valid" in new TestCase {
      val totalMessagesCount: Long = 0
      val unreadMessagesCount: Long = 0
      mockAuthorise[Unit]()(Future.successful(()))
      when(
        mockSecureMessageConnector.getCount(
          ArgumentMatchers.eq(None),
          ArgumentMatchers.eq(None),
          ArgumentMatchers.eq(None)
        )(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future(Count(total = totalMessagesCount, unread = unreadMessagesCount)))
      private val controller = new ApiController(
        mockAppConfig,
        Helpers.stubMessagesControllerComponents(),
        mockSecureMessageConnector,
        mockAuthConnector)
      private val result = controller.count(None, None, None)(
        FakeRequest(method = GET, path = "/messages?x=3&abc=test&some_key=some_value"))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Invalid query parameter(s) found: [abc, some_key, x]"
    }
  }

  class TestCase {
    implicit val duration: Timeout = 5 seconds
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    val mockSecureMessageConnector: SecureMessageConnector = mock[SecureMessageConnector]

  }
}
