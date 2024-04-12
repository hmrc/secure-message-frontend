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

package models.utils

import java.util.Base64

import scala.xml.{ Node, NodeBuffer, Text }

object HTMLEncoder {

  def encode(text: String): String = {
    // format: off
    val xhtml = splitParas(text).map { para =>
      <p>{text2XML(para)}</p>
    }
    // format: on
    val xhtmlText = xhtml.mkString
    base64Encode(xhtmlText)
  }

  private def base64Encode(text: String): String =
    new String(Base64.getEncoder.encodeToString(text.getBytes("UTF-8")))

  private def splitParas(text: String): Seq[String] =
    text.replaceAll("\r", "").split("[\\n]{2,}").toIndexedSeq

  private def text2XML(text: String): Seq[Node] = {

    def build(c: Char): Node =
      c match {
        case '<'  => Text("<")
        case '>'  => Text(">")
        case '&'  => Text("&")
        case '\n' => <br/>
        case _    => Text(c.toString)
      }

    text.foldLeft[NodeBuffer](new NodeBuffer()) { (s, c) =>
      s += build(c)
    }
  }

}
