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

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{ BearerTokenExpired, InsufficientConfidenceLevel, InvalidBearerToken, SessionRecordNotFound }

class ErrorHandlerSpec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite {

  trait Setup {
    val errorHandler = app.injector.instanceOf[ErrorHandler]
  }

  "The Error Handler" should {

    "return unauthorised in the case of an invalid bearer token" in new Setup {
      val exception = InvalidBearerToken()
      val result = errorHandler.resolveError(FakeRequest(), exception).futureValue

      result.header.status must be(Status.UNAUTHORIZED)
    }

    "return unauthorised in the case of a bearer token expiring" in new Setup {
      val exception = BearerTokenExpired()
      val result = errorHandler.resolveError(FakeRequest(), exception).futureValue

      result.header.status must be(Status.UNAUTHORIZED)
    }

    "return unauthorised in the case of an inactive session" in new Setup {
      val exception = SessionRecordNotFound()
      val result = errorHandler.resolveError(FakeRequest(), exception).futureValue

      result.header.status must be(Status.UNAUTHORIZED)
    }

    "return unauthorised in the case of an unauthorised request" in new Setup {
      val exception = InsufficientConfidenceLevel()
      val result = errorHandler.resolveError(FakeRequest(), exception).futureValue

      result.header.status must be(Status.UNAUTHORIZED)
    }
  }
}
