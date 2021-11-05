package com.portal.conf
import com.portal.auth.{JwtAccessClientTokenKeyConfig, PasswordSalt}
import io.circe.generic.JsonCodec

object app {
  @JsonCodec
  final case class AppConf(
    server:    ServerConf,
    scheduler: SchedulerConf,
    db:        DbConf,
    redis:     RedisConf,
    tokenConf: TokenConfig
  )

  @JsonCodec
  final case class DbConf(
    provider:          String,
    driver:            String,
    url:               String,
    user:              String,
    password:          String,
    migrationLocation: String
  )
  @JsonCodec
  final case class SchedulerConf(
    host: String,
    port: Int
  )

  @JsonCodec
  final case class RedisConf(url: String)

  @JsonCodec
  final case class TokenConfig(
    jwtAccessClientTokenKeyConfig:  JwtAccessClientTokenKeyConfig,
    jwtAccessCourierTokenKeyConfig: JwtAccessCourierTokenKeyConfig,
    adminTokenKeyConfig:            AdminTokenKeyConfig,
    passwordSalt:                   PasswordSalt,
    expiration:                     Int
  )

  @JsonCodec
  final case class AdminTokenKeyConfig(
    value: String
  )

  @JsonCodec
  final case class JwtAccessCourierTokenKeyConfig(
    value: String
  )

  @JsonCodec
  final case class ServerConf(
    host: String,
    port: Int
  )
}
