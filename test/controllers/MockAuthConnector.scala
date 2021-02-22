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

import org.mockito.ArgumentMatchers.{ any, eq => eqTo }
import org.mockito.Mockito.{ reset, when }
import org.scalatest.{ BeforeAndAfterEach, Suite }
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.authorise.{ EmptyPredicate, Predicate }
import uk.gov.hmrc.auth.core.retrieve.{ EmptyRetrieval, Retrieval }
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ ExecutionContext, Future }

@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments", "org.wartremover.warts.NonUnitStatements"))
trait MockAuthConnector extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockAuthConnector = mock[AuthConnector]

  def mockAuthorise[T](predicate: Predicate = EmptyPredicate, retrievals: Retrieval[T] = EmptyRetrieval)(
    response: Future[T]): Unit = {
    when(
      mockAuthConnector.authorise(
        eqTo(predicate),
        eqTo(retrievals)
      )(any[HeaderCarrier], any[ExecutionContext])).thenReturn(response)
    ()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

}