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

package models

import models.utils.HTMLEncoder
import play.api.libs.json.{ JsPath, Writes }

final case class CustomerMessage(content: String)

object CustomerMessage {
  implicit val writes: Writes[CustomerMessage] =
    (JsPath \ "content").write[String].contramap((m: CustomerMessage) => HTMLEncoder.encode(m.content))
}
