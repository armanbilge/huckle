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

import cats.data.NonEmptyList
import cats.effect.kernel.Concurrent
import cats.effect.implicits.*
import cats.syntax.all.*
import fs2.data.xml.scalaXml.*
import fs2.io.file.Files
import fs2.io.file.Path
import org.http4s.EntityDecoder
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.client.Client

trait MavenResolver[F[_]]:
  def resolve(coordinates: MavenCoordinates): F[(MavenProject, Path)]

object MavenResolver:
  def apply[F[_]: Files](
      cache: Path,
      repositories: List[Uri],
      client: Client[F],
  )(using F: Concurrent[F]): MavenResolver[F] = coordinates =>

    def getFileName(ext: String): String =
      s"${coordinates.artifactId}-${coordinates.version}.$ext"

    def toPath =
      Path(coordinates.groupId.replace('.', '/')) / coordinates.artifactId / coordinates.version

    def toUriPath =
      Uri.Path(coordinates.groupId.split('.').map(Uri.Path.Segment(_)).toVector) /
        coordinates.artifactId / coordinates.version

    def isCached: F[Boolean] =
      val path = cache / toPath / getFileName("pom")
      Files[F].exists(path)

    def resolve(repos: NonEmptyList[Uri]): F[Option[Uri]] =
      val race = repos
        .map { repo =>
          val uri = repo.withPath(toUriPath / getFileName("pom"))
          client
            .successful(Request(Method.HEAD, uri))
            .ifM(
              F.pure(repo),
              F.canceled *> F.never,
            )
            .handleErrorWith(_ => F.canceled *> F.never)
        }
        .reduce(_.race(_).map(_.merge))

      race.background.use { oc =>
        oc.flatMap(_.fold(F.pure(None), F.raiseError(_), _.map(Some(_))))
      }

    def downloadMavenProject(repository: Uri): F[Unit] =
      List("pom", "jar").parTraverse_ { ext =>
        val fn = getFileName(ext)
        val uri = repository.withPath(toUriPath / fn)
        val path = cache / toPath / fn
        client.expect(uri)(EntityDecoder.binFile(path))
      }

    def getMavenProject: F[(MavenProject, Path)] =
      val path = cache / toPath
      Files[F]
        .readUtf8(path / getFileName("pom"))
        .through(fs2.data.xml.events[F, String](false))
        .through(fs2.data.xml.dom.documents)
        .compile
        .onlyOrError
        .flatMap(MavenProject.fromXml(_).liftTo[F])
        .tupleRight(path)

    isCached.flatMap {
      case true => F.unit
      case false =>
        NonEmptyList
          .fromList(repositories)
          .flatTraverse(resolve(_))
          .flatMap(_.liftTo(new RuntimeException(s"Failed to resolve $coordinates")))
          .flatMap(downloadMavenProject(_))
    } *> getMavenProject
