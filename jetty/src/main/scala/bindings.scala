package unfiltered.jetty

import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.server.ssl.SslSocketConnector
import org.eclipse.jetty.util.ssl.SslContextFactory

import unfiltered.util

/** Convenience methods for adding connector providers. */
trait PortBindings {
  val allInterfacesHost = "0.0.0.0"
  val localInterfaceHost = "127.0.0.1"
  val defaultHttpPort = 80
  val defaultHttpsPort = 443
  val defaultKeystorePathProperty = "jetty.ssl.keyStore"
  val defaultKeystorePasswordProperty = "jetty.ssl.keyStorePassword"

  def portBinding(portBinding: PortBinding): Server

  def http(port: Int = defaultHttpPort, host: String = allInterfacesHost) =
    portBinding(SocketPortBinding(port, host))

  def local(port: Int): Server =
    http(port, localInterfaceHost)

  def anylocal: Server = local(util.Port.any)

  def https(
    port: Int = defaultHttpsPort,
    host: String = allInterfacesHost,
    keyStorePath: String,
    keyStorePassword: String
  ) = portBinding(
    SslSocketPortBinding(
      port,
      host,
      SslContextProvider.Simple(
        keyStorePath = keyStorePath,
        keyStorePassword = keyStorePassword
      )
    )
  )

  def httpsSysProperties(
    port: Int = defaultHttpsPort,
    host: String = allInterfacesHost,
    keyStorePathProperty: String = defaultKeystorePathProperty,
    keyStorePasswordProperty: String = defaultKeystorePasswordProperty
  ) = portBinding(
    SslSocketPortBinding(
      port,
      host,
      PropertySslContextProvider(
        keyStorePathProperty = keyStorePathProperty,
        keyStorePasswordProperty = keyStorePasswordProperty
      )
    )
  )
}

trait PortBinding extends util.PortBindingInfo {
  def connector: Connector
}

case class SocketPortBinding (
  port: Int,
  host: String
) extends PortBinding with util.HttpPortBinding {
  lazy val connector = {
    val c = new SocketConnector
    c.setPort(port)
    c.setHost(host)
    c
  }
}

trait SslContextProvider {
  def keyStorePath: String
  def keyStorePassword: String
  lazy val sslContextFactory = {
    val factory = new SslContextFactory
    factory.setKeyStorePath(keyStorePath)
    factory.setKeyStorePassword(keyStorePassword)
    factory
  }
}

object SslContextProvider {
  case class Simple(
    keyStorePath: String,
    keyStorePassword: String
  ) extends SslContextProvider
}

case class PropertySslContextProvider(
  keyStorePathProperty: String,
  keyStorePasswordProperty: String
) extends SslContextProvider {
  private lazy val props = new sys.SystemProperties
  lazy val keyStorePath = props(keyStorePathProperty)
  lazy val keyStorePassword = props(keyStorePasswordProperty)
}

case class SslSocketPortBinding (
  port: Int,
  host: String,
  sslContextProvider: SslContextProvider
) extends PortBinding with util.HttpsPortBinding {
  lazy val connector = {
    val c = new SslSocketConnector(sslContextProvider.sslContextFactory)
    c.setPort(port)
    c.setHost(host)
    c
  }
}
