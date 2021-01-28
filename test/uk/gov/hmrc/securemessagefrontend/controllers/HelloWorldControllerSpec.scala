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

package uk.gov.hmrc.securemessagefrontend.controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{ Configuration, Environment }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.securemessagefrontend.config.AppConfig
import uk.gov.hmrc.securemessagefrontend.views.html.HelloWorldPage

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class HelloWorldControllerSpec extends PlaySpec with GuiceOneAppPerSuite {
  private val fakeRequest = FakeRequest("GET", "/hello-world")

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val serviceConfigs = new ServicesConfig(configuration)
  private val appConfig = new AppConfig(serviceConfigs)

  val helloWorldPage: HelloWorldPage = app.injector.instanceOf[HelloWorldPage]

  private val controller = new HelloWorldController(appConfig, stubMessagesControllerComponents(), helloWorldPage)

  "GET /hello-world" should {
    "return 200" in {
      val result = controller.helloWorld(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return HTML" in {
      val result = controller.helloWorld(fakeRequest)
      contentType(result) mustBe Some("text/html")
      charset(result) mustBe Some("utf-8")
    }
  }
}
