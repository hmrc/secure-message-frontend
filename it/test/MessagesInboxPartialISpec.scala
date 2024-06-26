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

import com.google.inject.AbstractModule
import connectors.SecureMessageConnector
import controllers.generic.models.{ CustomerEnrolment, Tag }
import models.{ MessageHeader, MessageType }
import net.codingwell.scalaguice.ScalaModule

import java.time.Instant
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.{ BAD_REQUEST, CREATED, OK }
import play.api.http.{ ContentTypes, HeaderNames }
import play.api.i18n.Lang
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{ Json, Reads }
import play.api.libs.ws.{ WSClient, WSResponse }
import uk.gov.hmrc.http.HeaderCarrier
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import java.io.File
import scala.concurrent.{ ExecutionContext, Future }

class MessagesInboxPartialISpec extends PlaySpec with ServiceSpec with MockitoSugar with BeforeAndAfterEach {
  override def externalServices: Seq[String] = Seq.empty
  val secureMessagePort: Int = 9051
  val secureMessageFrontendPort: Int = 9055

  override protected def beforeEach(): Unit = {
    wsClient
      .url(s"http://localhost:$secureMessagePort/test-only/delete/conversation/SMF123456789/CDCM")
      .withHttpHeaders((HeaderNames.CONTENT_TYPE, ContentTypes.JSON))
      .delete()
      .futureValue
    wsClient
      .url(s"http://localhost:$secureMessagePort/test-only/delete/message/609d1359aa0200d12c73950a")
      .withHttpHeaders((HeaderNames.CONTENT_TYPE, ContentTypes.JSON))
      .delete()
      .futureValue
    ()
  }

  private val mockSecureMessageConnector = mock[SecureMessageConnector]

  private val wsClient = app.injector.instanceOf[WSClient]

  override def additionalOverrides: Seq[GuiceableModule] =
    Seq(new AbstractModule with ScalaModule {
      override def configure(): Unit =
        bind[SecureMessageConnector].toInstance(mockSecureMessageConnector)
    })

  "Getting the message inbox list partial" should {

    "return status code OK 200" in {
      when(
        mockSecureMessageConnector.getInboxList(
          ArgumentMatchers.eq(Some(List("HMRC-CUS-ORG"))),
          ArgumentMatchers.eq(Some(List(CustomerEnrolment("HMRC-CUS-ORG", "EORIName", "GB7777777777")))),
          ArgumentMatchers.eq(Some(List(Tag("notificationType", "CDS Exports")))),
          ArgumentMatchers.eq(Lang("en"))
        )(any[ExecutionContext], any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          List(
            MessageHeader(
              MessageType.Conversation,
              "123456",
              "D-80542-20201120",
              Instant.now(),
              Some("CDS Exports Team"),
              unreadMessages = true,
              1,
              Some(""),
              Some("")
            )
          )
        )
      )
      val response = wsClient
        .url(
          resource(
            "/secure-message-frontend/cdcm/messages?" +
              "enrolmentKey=HMRC-CUS-ORG&enrolment=HMRC-CUS-ORG~EORIName~GB7777777777&tag=notificationType~CDS Exports"
          )
        )
        .withHttpHeaders(List(AuthUtil.buildEoriToken, (HeaderNames.ACCEPT_LANGUAGE, "en")): _*)
        .get()
        .futureValue
      response.status mustBe OK
    }

    "return status code BAD REQUEST 400 when provided with filter parameters that are invalid (not allowed)" in {
      when(
        mockSecureMessageConnector.getInboxList(
          ArgumentMatchers.eq(None),
          ArgumentMatchers.eq(None),
          ArgumentMatchers.eq(None),
          ArgumentMatchers.eq(Lang("en"))
        )(any[ExecutionContext], any[HeaderCarrier])
      ).thenReturn(Future.successful(List()))

      import play.api.libs.ws.DefaultBodyReadables.readableAsString

      val response: WSResponse = wsClient
        .url(
          resource(
            "/secure-message-frontend/cdcm/messages?" +
              "enrolment_key=HMRC-CUS-ORG&enrolement=HMRC-CUS-ORG~EORIName~GB7777777777&tags=notificationType~CDS Exports"
          )
        )
        .withHttpHeaders(AuthUtil.buildEoriToken)
        .get()
        .futureValue
      response.status mustBe BAD_REQUEST
      response.body mustBe "Invalid query parameter(s) found: [enrolement, enrolment_key, tags]"
    }
  }

  class TestSetUp {
    val createConversationUrl =
      s"http://localhost:$secureMessagePort/secure-messaging/conversation/CDCM/SMF123456789"

    wsClient
      .url(createConversationUrl)
      .withHttpHeaders((HeaderNames.CONTENT_TYPE, ContentTypes.JSON))
      .put(new File("./it/resources/create-conversation.json"))
      .futureValue
      .status mustBe CREATED

    val createMessageUrl =
      s"http://localhost:$secureMessagePort/test-only/create/message/609d1359aa0200d12c73950a"

    val responseFromSecureMessage =
      wsClient
        .url(createMessageUrl)
        .withHttpHeaders((HeaderNames.CONTENT_TYPE, ContentTypes.JSON))
        .put(new File("./it/resources/create-letter.json"))
        .futureValue
    responseFromSecureMessage.status mustBe CREATED

  }

  object AuthUtil {

    lazy val ggAuthPort: Int = 8585

    implicit val deserialiser: Reads[GatewayToken] = Json.reads[GatewayToken]

    case class GatewayToken(gatewayToken: String)

    private val EORI_USER_PAYLOAD =
      """
        | {
        |  "credId": "1235",
        |  "affinityGroup": "Organisation",
        |  "confidenceLevel": 200,
        |  "credentialStrength": "strong",
        |  "enrolments": [
        |      {
        |        "key": "HMRC-CUS-ORG",
        |        "identifiers": [
        |          {
        |            "key": "EORINumber",
        |            "value": "GB1234567890"
        |          }
        |        ],
        |        "state": "Activated"
        |      }
        |    ]
        |  }
     """.stripMargin

    private def buildUserToken(payload: String): (String, String) = {
      val response = wsClient
        .url(s"http://localhost:$ggAuthPort/government-gateway/session/login")
        .withHttpHeaders(("Content-Type", "application/json"))
        .post(Json.parse(payload))
        .futureValue

      ("Authorization", response.header("Authorization").getOrElse(""))
    }

    def buildEoriToken: (String, String) = buildUserToken(EORI_USER_PAYLOAD)
  }
}
