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

package views.helpers

import play.api.i18n.Messages

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateFormat {

  private val messageDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def messageDateFormat(date: LocalDate)(implicit messages: Messages): String = {
    val formatter = if (messages.lang.language == "cy") {
      DateTimeFormatter.ofPattern(s"d '${messages(s"month.${date.getMonthValue}")}' yyyy")
    } else {
      messageDateFormatter
    }
    formatter.format(date)
  }
}
