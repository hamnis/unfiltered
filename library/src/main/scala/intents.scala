package unfiltered

import unfiltered.request.HttpRequest
import unfiltered.response.{Pass, ResponseFunction}

object Cycle {
  /** A roundtrip intent is a set of instructions for producing
   * a complete response to a request. Plans that contain intents
   * of this type can be run against a general set of tests. */
  type Intent[-A,-B] = PartialFunction[HttpRequest[A], ResponseFunction[B]]
  /** Object to facilitate Cycle.Intent definitions. Type annotations
   *  are another option. */
  object Intent {
    def apply[A,B](intent: Intent[A,B]) = intent
    def complete[A,B](intent: Intent[A,B]): Intent[A,B] =
      intent.orElse({ case _ => Pass })
  }
}

object Async {
  type Intent[-A,-B] =
    PartialFunction[HttpRequest[A] with Responder[B], Any]
  object Intent {
    def apply[A,B](intent: Intent[A,B]) = intent

    def fromSync[A,B](intent: Cycle.Intent[A, B]): Intent[A, B] = {
      case req if intent.isDefinedAt(req) => req.respond(intent(req))
    }
  }
  trait Responder[+R] {
    def respond(rf: ResponseFunction[R]): Unit
  }
}
