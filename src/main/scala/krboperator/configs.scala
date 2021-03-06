package krboperator

import com.typesafe.config.{ConfigFactory, ConfigParseOptions}
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigSource}

import java.io.File
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._

final case class KeytabCommand(randomKey: String, noRandomKey: String)
final case class Commands(addPrincipal: String, addKeytab: KeytabCommand)
final case class AdminPassword(secretName: String, secretKey: String)

final case class KrbOperatorCfg(
    krb5Image: String,
    k8sSpecsDir: String,
    adminPrincipal: String,
    commands: Commands,
    kadminContainer: String,
    k8sResourcesPrefix: String,
    adminPwd: AdminPassword,
    reconcilerInterval: FiniteDuration,
    operatorPrefix: String,
    crdVersion: String,
    parallelSecretCreation: Boolean
)

object AppConfig {
  private lazy val parseOptions =
    ConfigParseOptions.defaults().setAllowMissing(false)

  private def cfgPath: String =
    sys.env.getOrElse("APP_CONFIG_PATH", "src/main/resources/application.conf")

  implicit def hint[T]: ProductHint[T] =
    ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  def load: Either[ConfigReaderFailures, KrbOperatorCfg] = {
    val path = cfgPath
    val config = ConfigFactory
      .parseFile(new File(path), parseOptions)
      .withFallback(ConfigFactory.parseMap(sys.env.asJava))
      .resolve()

    ConfigSource.fromConfig(config).at("operator").load[KrbOperatorCfg]
  }
}
