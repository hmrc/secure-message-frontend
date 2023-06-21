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

import org.scalatest.{ BeforeAndAfterEach, SuiteMixin }
import org.scalatest.concurrent.{ Eventually, IntegrationPatience, ScalaFutures }
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

trait ISpec
    extends PlaySpec with SuiteMixin with ScalaFutures with IntegrationPatience with GuiceOneServerPerSuite
    with Eventually with MockitoSugar with BeforeAndAfterEach {

  lazy val secureMessageFrontendPort: Int = 9055

  def resource(path: String): String =
    s"http://localhost:$secureMessageFrontendPort/$path"
}
