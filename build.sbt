import sbt.internal.IvyConsole.Dependencies._

name := "Final-Task"

version := "0.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Ymacro-annotations",
  "-Xfatal-warnings"
)

scalaVersion := "2.13.6"
val http4sVersion           = "0.21.7"
val circeVersion            = "0.13.0"
val circeConfigVersion      = "0.8.0"
val doobieVersion           = "0.9.0"
val catsVersion             = "2.2.0"
val catsTaglessVersion      = "0.11"
val catsEffectVersion       = "2.2.0"
val epimetheusVersion       = "0.4.2"
val catsScalacheckVersion   = "0.2.0"
val log4CatsVersion         = "1.1.1"
val scalaTestVersion        = "3.1.0.0-RC2"
val h2Version               = "1.4.200"
val enumeratumVersion       = "1.6.1"
val dtoMapperChimneyVersion = "0.6.1"
val ciris                   = "2.2.0"

libraryDependencies ++= Seq(
  "org.typelevel"            %% "cats-core"                     % catsVersion,
  "org.typelevel"            %% "cats-effect"                   % catsEffectVersion,
  "org.http4s"               %% "http4s-dsl"                    % http4sVersion,
  "org.http4s"               %% "http4s-blaze-server"           % http4sVersion,
  "org.http4s"               %% "http4s-blaze-client"           % http4sVersion,
  "org.http4s"               %% "http4s-circe"                  % http4sVersion,
  "org.http4s"               %% "http4s-jdk-http-client"        % "0.3.6",
  "io.chrisdavenport"        %% "log4cats-slf4j"                % log4CatsVersion,
  "ch.qos.logback"            % "logback-classic"               % "1.2.3",
  "com.codecommit"           %% "cats-effect-testing-scalatest" % "0.4.1"               % Test,
  "io.chrisdavenport"        %% "epimetheus-http4s"             % epimetheusVersion,
  "io.chrisdavenport"        %% "cats-scalacheck"               % catsScalacheckVersion % Test,
  "org.scalatestplus"        %% "scalatestplus-scalacheck"      % scalaTestVersion      % Test,
  "org.scalatestplus"        %% "selenium-2-45"                 % scalaTestVersion      % Test,
  "org.typelevel"            %% "simulacrum"                    % "1.0.0",
  "org.tpolecat"             %% "atto-core"                     % "0.8.0",
  "io.circe"                 %% "circe-core"                    % circeVersion,
  "io.circe"                 %% "circe-generic"                 % circeVersion,
  "io.circe"                 %% "circe-generic-extras"          % circeVersion,
  "io.circe"                 %% "circe-optics"                  % circeVersion,
  "io.circe"                 %% "circe-parser"                  % circeVersion,
  "org.fusesource.leveldbjni" % "leveldbjni-all"                % "1.8",
  "org.tpolecat"             %% "doobie-core"                   % doobieVersion,
  "org.tpolecat"             %% "doobie-h2"                     % doobieVersion,
  "org.tpolecat"             %% "doobie-hikari"                 % doobieVersion,
  "org.mockito"              %% "mockito-scala"                 % "1.15.0"              % Test,
  "org.scalaj"               %% "scalaj-http"                   % "2.4.2"               % Test,
  "org.tpolecat"             %% "doobie-scalatest"              % doobieVersion         % Test,
  "org.typelevel"            %% "cats-tagless-macros"           % catsTaglessVersion,
  "com.h2database"            % "h2"                            % "1.4.200",
  "eu.timepit"               %% "refined"                       % "0.9.17",
  "org.slf4j"                 % "slf4j-nop"                     % "1.6.4",
  "eu.timepit"               %% "refined"                       % "0.9.21",
  "com.beachape"             %% "enumeratum"                    % enumeratumVersion,
  "com.beachape"             %% "enumeratum-circe"              % enumeratumVersion,
  "io.scalaland"             %% "chimney"                       % dtoMapperChimneyVersion,
  "com.github.pureconfig"    %% "pureconfig"                    % "0.14.0",
  "io.circe"                 %% "circe-config"                  % circeConfigVersion,
  "io.circe"                 %% "circe-core"                    % circeVersion,
  "io.circe"                 %% "circe-generic"                 % circeVersion,
  "io.circe"                 %% "circe-generic-extras"          % circeVersion,
  "io.circe"                 %% "circe-optics"                  % circeVersion,
  "io.circe"                 %% "circe-parser"                  % circeVersion,
  "org.http4s"               %% "http4s-json4s-native"          % http4sVersion,
  "org.tpolecat"             %% "doobie-postgres"               % doobieVersion,
  "org.flywaydb"              % "flyway-core"                   % "6.2.4",
  "org.postgresql"            % "postgresql"                    % "42.2.6",
  "dev.profunktor"           %% "redis4cats-effects"            % "0.14.0",
  "io.estatico"              %% "newtype"                       % "0.4.4",
  "dev.profunktor"           %% "http4s-jwt-auth"               % "0.0.7",
  "org.typelevel"            %% "log4cats-core"                 % "1.2.0",
  "org.typelevel"            %% "log4cats-slf4j"                % "1.2.0",
  "is.cir"                   %% "ciris"                         % ciris,
  "is.cir"                   %% "ciris-enumeratum"              % ciris,
  "is.cir"                   %% "ciris-refined"                 % ciris,
  "com.comcast"              %% "ip4s-core"                     % "1.2.1",
  "org.slf4j"                 % "slf4j-nop"                     % "1.6.4",
  "org.quartz-scheduler"      % "quartz"                        % "2.3.0"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"    % "3.2.9"  % "test",
  "org.mockito"    % "mockito-core" % "3.12.4" % "test"
)
addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.13.0" cross CrossVersion.full
)
