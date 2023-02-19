/*
 * Copyright 2023 Arman Bilge
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

package huckle
package maven

import cats.effect.IO
import cats.effect.Resource
import fs2.io.file.Files
import org.http4s.client.Client
import org.http4s.syntax.all.*

object ResolverTest:
  def apply(client: Client[IO]): Resource[IO, Test] = Files[IO].tempDirectory.map { cache =>

    val resolver = MavenResolver(
      cache,
      List(
        uri"https://repo1.maven.org/maven2/",
        uri"https://s01.oss.sonatype.org/content/repositories/snapshots/",
      ),
      client,
    )

    val resolveCats = Test("resolve Cats v2.9.0") {
      resolver.resolve(MavenCoordinates("org.typelevel", "cats-core_3", "2.9.0")).void
    }

    resolveCats
  }
