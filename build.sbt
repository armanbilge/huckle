ThisBuild / tlBaseVersion := "0.0"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2023)

ThisBuild / tlSonatypeUseLegacyHost := false

ThisBuild / crossScalaVersions := Seq("3.2.2")

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / tlJdkRelease := Some(8)

val CatsEffectVersion = "3.4.7"
val Fs2Version = "3.6.1"
val Http4sVersion = "0.23.18"
val Fs2DataVersion = "1.6.1"
val Http4sFs2DataVersion = "0.1.0"

ThisBuild / scalacOptions ++= Seq("-new-syntax", "-indent", "-source:future")

val commonJvmSettings = Seq(
  fork := true,
)

lazy val root = tlCrossRootProject.aggregate(
  maven,
)

lazy val maven = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("maven"))
  .settings(
    name := "huckle-maven",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "co.fs2" %%% "fs2-io" % Fs2Version,
      "org.http4s" %%% "http4s-client" % Http4sVersion,
      "org.http4s" %%% "http4s-fs2-data-xml-scala" % Http4sFs2DataVersion,
    ),
  )

lazy val tests = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("tests"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(maven)
  .settings(
    (Test / test) := (Compile / run).toTask("").value,
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-ember-client" % Http4sVersion,
    ),
  )
