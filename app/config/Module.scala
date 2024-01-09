/*
 * Copyright 2024 HM Revenue & Customs
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

package config
import controllers.actions._
import play.api.inject.{Binding, bind => binding}
import play.api.{Configuration, Environment}

import java.time.{Clock, ZoneOffset}

class Module extends play.api.inject.Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val authTokenInitialiserBindings: Seq[Binding[_]] =
      if (configuration.get[Boolean]("create-internal-auth-token-on-start")) {
        Seq(binding[InternalAuthTokenInitialiser].to[InternalAuthTokenInitialiserImpl].eagerly())
      } else {
        Seq(binding[InternalAuthTokenInitialiser].to[NoOpInternalAuthTokenInitialiser].eagerly())
      }

    Seq(
      binding[DataRequiredAction].to[DataRequiredActionImpl].eagerly(),
      binding[Clock].to(Clock.systemDefaultZone.withZone(ZoneOffset.UTC)),
      binding[IdentifierAction].to[AuthenticatedIdentifierAction].eagerly()
    ) ++ authTokenInitialiserBindings
  }
}
