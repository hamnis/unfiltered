package unfiltered.specs2
package jetty

import org.specs2.mutable._
import org.specs2.specification.BeforeAfterAll


trait Planned extends Served {

  def setup = _.plan(unfiltered.filter.Planify(intent))

  def intent[A, B]: unfiltered.Cycle.Intent[A, B]
}

trait Served extends Hosted with SpecificationLike with BeforeAfterAll {

  import unfiltered.jetty._

  def setup: (Server => Server)

  lazy val server = setup(Server.http(port))

  override def afterAll(): Unit = {
    server.stop()
    server.destroy()
  }

  override def beforeAll(): Unit = {
    server.start()
  }
}
