import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.2"

ThisBuild / scalacOptions ++= Seq(
  "-Xprint:postInlining", // this option is used to print the inlined code from the sbt output once we add macros or inlines in the code
  "-Xmax-inlines:100000"
)

lazy val root = (project in file("."))
  .settings(
    name := "scala-macros-metaprogramming",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.7.5"
    )
  )
