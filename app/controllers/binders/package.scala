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

import com.typesafe.config.ConfigFactory
import controllers.generic.models.{CustomerEnrolment, Tag}
import model.*
import play.api.Logger
import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, PlainText, SymmetricCryptoFactory}

import scala.util.Try
import java.util.Base64

case class Encrypted[T](decryptedValue: T)

case class ParameterisedUrl(url: String, parameters: Map[String, Seq[String]] = Map.empty)

object ParameterisedUrl {

  private val logger = Logger(getClass.getName)

  def fromDelimitedString(value: String): ParameterisedUrl =
    value.split("::").toList match {
      case url :: step :: returnUrl :: Nil =>
        ParameterisedUrl(url, Map("step" -> Seq(step), "returnUrl" -> Seq(returnUrl)))
      case url :: step :: Nil => ParameterisedUrl(url, Map("step" -> Seq(step)))
      case url :: Nil =>
        logger.warn(s"Use of unexpected read url format (perhaps link was bookmarked?): $url")
        ParameterisedUrl(url)
      case sections =>
        logger.error(
          s"Use of unexpected read url format (assuming the first section is the url): ${sections.mkString(", ")}"
        )
        ParameterisedUrl(sections.head)
    }
}

private trait EncryptedStringPathBinder extends PathBindable[Encrypted[String]] {
  implicit val crypto: Encrypter with Decrypter
  val stringBinder: PathBindable[String]

  def bind(key: String, value: String): Either[String, Encrypted[String]] = {
    def base64Decode(s: String) =
      Try(new String(Base64.getDecoder.decode(s), "UTF-8")).map(Right(_)).getOrElse(Left(s"Could not decode $key"))

    def decrypt(s: String) = Try(crypto.decrypt(Crypted(s))).map(Right(_)).getOrElse(Left(s"Could not decrypt $key"))

    for {
      decoded <- base64Decode(value)
      bound <- stringBinder.bind(key, decoded)
      decrypted <- decrypt(bound)
    } yield Encrypted(decrypted.value)
  }

  def unbind(key: String, value: Encrypted[String]): String =
    Base64.getEncoder.encodeToString(
      stringBinder.unbind(key, crypto.encrypt(PlainText(value.decryptedValue)).value).getBytes
    )
}


package object binders {
  implicit def queryStringBindableCustomerEnrolment(implicit
    stringBinder: QueryStringBindable[String]
  ): QueryStringBindable[CustomerEnrolment] =
    new QueryStringBindable[CustomerEnrolment] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, CustomerEnrolment]] =
        stringBinder.bind("enrolment", params) map {
          case Right(customerEnrolment) => Right(CustomerEnrolment.parse(customerEnrolment))
          case _                        => Left("Unable to bind a CustomerEnrolment")
        }

      override def unbind(key: String, customerEnrolment: CustomerEnrolment): String =
        customerEnrolment.key + "~" + customerEnrolment.name + "~" + customerEnrolment.value
    }

  implicit def queryStringBindableTag(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[Tag] =
    new QueryStringBindable[Tag] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Tag]] =
        stringBinder.bind("tag", params) map {
          case Right(tag) => Right(Tag.parse(tag))
          case _          => Left("Unable to bind a Tag")
        }

      override def unbind(key: String, tag: Tag): String =
        tag.key + "~" + tag.value
    }

  implicit def encryptedStringPathBinder(implicit
                                         implStringBinder: PathBindable[String]
                                        ): PathBindable[Encrypted[String]] =
    new EncryptedStringPathBinder {
      val crypto: Encrypter with Decrypter =
        SymmetricCryptoFactory.aesCryptoFromConfig(baseConfigKey = "queryParameter.encryption", ConfigFactory.load())
      val stringBinder = implStringBinder
    }

  implicit def readPreferenceBinder(implicit
                                    stringBinder: QueryStringBindable[String]
                                   ): QueryStringBindable[ReadPreference.Value] =
    new QueryStringBindable[ReadPreference.Value] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, ReadPreference.Value]] =
        stringBinder.bind("read", params).flatMap { param =>
          param.fold(
            _ => None,
            readParam => Some(ReadPreference.validate(readParam))
          )
        }

      override def unbind(key: String, value: ReadPreference.Value): String = ""
    }

  implicit def encryptedParameterisedUrlBinder(implicit
                                               implStringBinder: PathBindable[String]
                                              ): PathBindable[Encrypted[ParameterisedUrl]] =
    new EncryptedParameterisedUrlBinder {
      val crypto: Encrypter with Decrypter =
        SymmetricCryptoFactory.aesCryptoFromConfig(baseConfigKey = "queryParameter.encryption", ConfigFactory.load())
      val stringBinder = implStringBinder
    }

  private trait EncryptedParameterisedUrlBinder extends PathBindable[Encrypted[ParameterisedUrl]] {
    val crypto: Encrypter with Decrypter
    val stringBinder: PathBindable[String]

    def bind(key: String, value: String): Either[String, Encrypted[ParameterisedUrl]] = {
      def base64Decode(s: String) =
        Try(new String(Base64.getDecoder.decode(s), "UTF-8")).map(Right(_)).getOrElse(Left(s"Could not decode $key"))
      def decrypt(s: String) = Try(crypto.decrypt(Crypted(s))).map(Right(_)).getOrElse(Left(s"Could not decrypt $key"))

      for {
        decoded   <- base64Decode(value)
        bound     <- stringBinder.bind(key, decoded)
        decrypted <- decrypt(bound)
      } yield Encrypted(ParameterisedUrl.fromDelimitedString(decrypted.value))
    }

    def unbind(key: String, value: Encrypted[ParameterisedUrl]): String = {
      val decrypted = value.decryptedValue

      val concatValues: String = (decrypted.parameters.map { case (_, value :: _) =>
        value
      } +: decrypted.url).mkString("::")

      Base64.getEncoder.encodeToString(stringBinder.unbind(key, crypto.encrypt(PlainText(concatValues)).value).getBytes)
    }

  }

}
