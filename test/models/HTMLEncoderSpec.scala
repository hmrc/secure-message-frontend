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
import org.apache.commons.codec.binary.Base64
import org.scalatestplus.play.PlaySpec

class HTMLEncoderSpec extends PlaySpec {

  "Carriage returns in text" must {
    "be replaced with <br/>" in {
      val s =
        """Hello
          |World""".stripMargin
      val result = new String(Base64.decodeBase64(HTMLEncoder.encode(s).getBytes("UTF-8")))
      result mustEqual "<p>Hello<br/>World</p>"
    }
  }

  "Ampersands in text" must {
    "be encoded as &amp;" in {
      val s = """Hello & World"""
      val result = new String(Base64.decodeBase64(HTMLEncoder.encode(s).getBytes("UTF-8")))
      result mustEqual "<p>Hello &amp; World</p>"
    }
  }

  "Angle brackets in text" must {
    "be encoded as &lt; or &gt;" in {
      val s = """Hello <World>"""
      val result = new String(Base64.decodeBase64(HTMLEncoder.encode(s).getBytes("UTF-8")))
      result mustEqual "<p>Hello &lt;World&gt;</p>"
    }
  }

  "All Text" must {
    "be Base64 encoded" in {
      val s = """Hello & <World>"""
      val result = HTMLEncoder.encode(s)
      result mustEqual "PHA+SGVsbG8gJmFtcDsgJmx0O1dvcmxkJmd0OzwvcD4="
    }
  }

}
