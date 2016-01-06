import akka.actor._
import com.typesafe.config.ConfigFactory
import java.net.InetAddress
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks
import scala.util.Random
import akka.routing.RoundRobinRouter
import java.security.MessageDigest


case class MiningWorkers(zeros:Int) 
case class startMining(zeros :Int) 
case class aggregateCoins(coins: ArrayBuffer[String]) 
case class printresult(coins: ArrayBuffer[String]) 
case class LocalMessage(coins:ArrayBuffer[String]) 
case class RemoteMessage(coins:ArrayBuffer[String]) 
case class assignWork(zeros:Int) 


object BitcoinClient {

  def main(args: Array[String]) {
  
	val ip = args(0)
    val system = ActorSystem("ClientSystem")
    val Reporter = system.actorOf(Props(new Reporter(ip)), name = "Reporter")
   
    val listener = system.actorOf(Props(new Listener(Reporter)), name = "listener")
    val master =  system.actorOf(Props(new Master(10, listener)),name = "master")
    
    val GetWork = system.actorOf(Props(new GetWork(ip,master)), name = "GetWork") 
    
     GetWork ! "givework" 
    
     
  }
}
class GetWork(ip: String , master:ActorRef) extends Actor {
 
  val server = context.actorSelection("akka.tcp://BitcoinServer@" + ip + ":21000/user/AssignWorkActor")

  def receive = {
    case "givework" =>
      print("s")
       server ! "AssignWorkToMe"
    case assignWork(zeros) =>
       println("Work from server : to generate Bitcoins which start with " + zeros +" Zeros")
       master ! MiningWorkers(zeros)
  }
}

class Reporter(ip: String) extends Actor {
  println("akka.tcp://BitcoinServer@" + ip + ":21000/user/AssignWorkActor")
  val remote = context.actorSelection("akka.tcp://BitcoinServer@" + ip + ":21000/user/AssignWorkActor")

  def receive = {
    case LocalMessage(coins) =>
       remote ! RemoteMessage(coins)
       println("Sent result to the server") 
    case _ =>
      println("received unknown message ")
  }
}

class Master(WorkersCount: Int, listener: ActorRef) extends Actor {

  var total_mined: ArrayBuffer[String] = new ArrayBuffer[String]()
  var messages: Int = _
  val start: Long = System.currentTimeMillis

  val workerRouter = context.actorOf(
    Props[Worker].withRouter(RoundRobinRouter(WorkersCount)), name = "workerRouter")

  def receive = {

    case MiningWorkers(numberOfZeros) =>
      for (i <- 0 until MessageCount) workerRouter ! startMining(numberOfZeros)
    case aggregateCoins(coins) =>
      total_mined ++= coins

        listener ! printresult(total_mined)

	  total_mined = new ArrayBuffer[String]()
  }

}

class Worker extends Actor {

  def receive = {

    case startMining(zeros) => {
	mineCoins(zeros)
	}			
				
  def hash( s:String) : String ={
    var m = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))
    var Bitcoin = m.map("%02x".format(_)).mkString
    Bitcoin
  }
	def mineCoins(zeros:Int) = {
	var mined:ArrayBuffer[String] = ArrayBuffer[String]()
    var count:Int = 0
    var startTime = System.currentTimeMillis
    var rand :String=Random.alphanumeric.take(5).mkString
      
    while(System.currentTimeMillis - startTime<3000000){
        var string:String = "client" +rand+count
        var Bitcoin = hash(string)
        var leading = Bitcoin.substring(0,zeros)
        var zeroString = "0" * zeros
        if(leading.equals(zeroString)){
          mined += string + "    ----   " + Bitcoin
        }
        count+=1
		
		if(count%1000000==0){
			sender ! aggregateCoins(mined)
		}
    }
	
  }
    }

  

}

class Listener(Reporter: ActorRef) extends Actor {
  def receive = {
    case printresult(coins) =>
      {
		for(i <- 0 until coins.length){
           println(coins(i))
		}
        println("Assigned Work done")
        Reporter ! LocalMessage(coins)
      }
      
  }
}