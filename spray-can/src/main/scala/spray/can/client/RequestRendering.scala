/*
 * Copyright © 2011-2013 the spray project <http://spray.io>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spray.can.client

import spray.http.HttpHeaders.`User-Agent`
import spray.http.HttpDataRendering
import spray.can.rendering._
import spray.io._
import spray.util._

object RequestRendering {

  def apply(settings: ClientConnectionSettings): PipelineStage =
    new PipelineStage with RequestRenderingComponent {
      val userAgent = settings.userAgentHeader.toOption.map(`User-Agent`(_))

      def apply(context: PipelineContext, commandPL: CPL, eventPL: EPL): Pipelines =
        new Pipelines {
          val commandPipeline: CPL = {
            case RequestPartRenderingContext(requestPart, ack) ⇒
              val rendering = new HttpDataRendering(settings.requestHeaderSizeHint)
              renderRequestPart(rendering, requestPart, context.remoteAddress, context.log)
              commandPL(toTcpWriteCommand(rendering.get, ack))

            case cmd ⇒ commandPL(cmd)
          }

          val eventPipeline = eventPL
        }
    }
}