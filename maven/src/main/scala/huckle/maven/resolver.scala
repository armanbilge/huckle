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

package huckle.maven

import cats.effect.kernel.Concurrent
import fs2.io.file.Files
import fs2.io.file.Path
import org.http4s.Uri
import org.http4s.client.Client

trait Resolver[F[_]]:
  def resolve(coordinates: MavenCoordinates): F[Path]

object Resolver:
  def apply[F[_]: Files](
      cache: Path,
      repositories: List[Uri],
      client: Client[F],
  )(using F: Concurrent[F]): Resolver[F] = ???
