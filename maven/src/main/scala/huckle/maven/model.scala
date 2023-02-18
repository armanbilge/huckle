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

import cats.syntax.all.*
import fs2.data.xml.dom.DocumentBuilder

final case class MavenCoordinates(
    groupId: String,
    artifactId: String,
    version: String,
)

object MavenCoordinates:
  def fromXml(node: xml.NodeSeq): Either[Throwable, MavenCoordinates] =
    Either.catchNonFatal {
      MavenCoordinates(
        (node \ "groupId").text,
        (node \ "artifactId").text,
        (node \ "version").text,
      )
    }

final case class MavenProject(
    coordinates: MavenCoordinates,
    dependencies: List[MavenDependency],
)

object MavenProject:
  def fromXml(node: xml.Document): Either[Throwable, MavenProject] =
    val coordinates = MavenCoordinates.fromXml(node)
    val dependencies = (node \ "dependencies").toList.traverse(MavenDependency.fromXml(_))
    (coordinates, dependencies).mapN(MavenProject(_, _))

final case class MavenDependency(
    coordinates: MavenCoordinates,
)

object MavenDependency:
  def fromXml(node: xml.Node): Either[Throwable, MavenDependency] =
    MavenCoordinates.fromXml(node).map(MavenDependency(_))
