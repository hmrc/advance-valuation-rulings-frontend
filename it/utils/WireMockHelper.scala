package utils

import java.net.ServerSocket

import scala.jdk.CollectionConverters._
import scala.util.Using

import com.github.tomakehurst.wiremock.{WireMockServer, client}
import com.github.tomakehurst.wiremock.client.{MappingBuilder, ResponseDefinitionBuilder, WireMock}
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.http.{HttpHeader, HttpHeaders, RequestMethod}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.openqa.selenium.InvalidArgumentException
import utils.WireMockHelper.{MappingBuilderExt, ResponseDefinitionBuilderExt, wireMockPort}

trait WireMockHelper {
  val wireMockServer = new WireMockServer(wireMockConfig.port(wireMockPort))

  def startWireMock(): Unit = {
    WireMock.configureFor(wireMockPort)
    wireMockServer.start()
  }

  def stopWireMock(): Unit =
    wireMockServer.stop()

  def resetWireMock(): Unit =
    wireMockServer.resetAll()

  def stub(
    requestMethod: RequestMethod,
    url: String,
    statusCode: Int,
    responseBody: String,
    requestBody: Option[String] = None,
    requestHeaders: Set[(String, String)] = Set.empty,
    responseHeaders: Set[(String, String)] = Set.empty
  ): StubMapping = {
    val mappingBuilder = requestMethod match {
      case RequestMethod.GET  => get(urlEqualTo(url))
      case RequestMethod.POST =>
        post(urlEqualTo(url)).withRequestBody(equalTo(requestBody.getOrElse("")))
      case unexpected         =>
        throw new InvalidArgumentException(s"Unexpected wiremock stub request method $unexpected")
    }

    stubFor(
      mappingBuilder
        .withRequestHeaders(requestHeaders)
        .willReturn(
          aResponse()
            .withStatus(statusCode)
            .withResponseHeaders(responseHeaders)
            .withBody(responseBody)
        )
    )
  }
}

object WireMockHelper {

  val wireMockPort: Int = Using(new ServerSocket(0))(_.getLocalPort)
    .getOrElse(throw new Exception("Failed to find random free port"))

  implicit class MappingBuilderExt(builder: client.MappingBuilder) {

    def withRequestHeaders(headers: Set[(String, String)]): MappingBuilder =
      headers.foldLeft(builder) { (builder, header) =>
        val (key, value) = header
        builder.withHeader(key, equalTo(value))
      }
  }

  implicit class ResponseDefinitionBuilderExt(builder: ResponseDefinitionBuilder) {

    def withResponseHeaders(headers: Set[(String, String)]): ResponseDefinitionBuilder = {
      val responseHeadersWithContentType = Set("Content-Type" -> "application/json; charset=utf-8")
        .union(headers)
        .toList
        .map { case (key, value) => HttpHeader.httpHeader(key, value) }
      builder.withHeaders(new HttpHeaders(responseHeadersWithContentType.asJava))
    }
  }
}
