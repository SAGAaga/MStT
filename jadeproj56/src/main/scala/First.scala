import akka.actor.{ActorRef, _}

case class Request(query: String, replyTo: ActorRef)

case class Propose(query: String, replyTo: ActorRef)

case class Response(query: String)

case object Req

case object Prop

case object Resp

class Navigator() extends Actor {
  def receive: Receive = {
    case Request(query, replyTo) => replyTo ! new Propose(Singleton.processNav(query), replyTo)
  }
}

class Speleologist(nav: ActorRef, env: ActorRef) extends Actor {
  def receive: Receive = {
    case Propose(msg, dest) => env ! new Propose(Singleton.processSp(msg), self)
    case Request(msg, dest) => nav ! new Request(Singleton.processSp(msg), self)
    case Response(msg) => {
      if(msg.contains(legacy.KB.END)){
        println(msg)
        context.stop(nav)
        context.stop(env)
        context.stop(self)
        println("Actors stopped")
      }
      env ! new Request(Singleton.processSp(msg), self)
    }
  }
}

class Environment() extends Actor {
  def receive: Receive = {
    case Request(msg, dest) => dest ! new Request(Singleton.processEnv(msg), dest)
    case Propose(msg, dest) => dest ! new Response(Singleton.processEnv(msg))
  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")
  val nav = system.actorOf(Props(new Navigator()), name = "nav1")
  val env = system.actorOf(Props(new Environment()), "env1")
  val sp = system.actorOf(Props(new Speleologist(nav, env)), name = "sp1")

  sp ! new Response(legacy.KB.OK)
}