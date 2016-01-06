import akka.actor._
import akka.routing.RoundRobinRouter
import scala.collection.mutable.ArrayBuffer
import java.net.InetAddress
import com.typesafe.config.ConfigFactory
import scala.util.Random
import scala.util.control._
import scala.collection.mutable.ArrayBuffer
import java.security.MessageDigest

case class MiningWorkers(zeros:Int) 
case class startMining(zeros :Int) 
case class aggregateCoins(coins: ArrayBuffer[String] ) 
case class PrintResult(coins: ArrayBuffer[String]) 
case class LocalMessage(coins:ArrayBuffer[String]) 
case class RemoteMessage(coins:ArrayBuffer[String]) 
case class assignWork(zeros:Int) 


object BitcoinServer extends App{
  override def main(args: Array[String]) {
	
   val zeros = args(0).toInt
   val system = ActorSystem("BitcoinServer")
   val AssignWorkActor = system.actorOf(Props(new AssignWorkActor(zeros)), name = "AssignWorkActor")
   
   //start the Assign work actor which assigns work to a client
   AssignWorkActor ! "startserver"
   val listener = system.actorOf(Props[Listener], name = "listener")
   val master = system.actorOf(Props(new Master(10,zeros, listener)), name = "master")

   // Start the mining workers
   master ! MiningWorkers    
  }
}


class Master(WorkersCount: Int,NumberOfZeros:Int, listener: ActorRef) extends Actor {
  //ArrayBuffer to store results
  var total_mined: ArrayBuffer[String] = new ArrayBuffer[String]()
  var messages: Int = _

  val workerRouter = context.actorOf(
    Props[Worker].withRouter(RoundRobinRouter(WorkersCount)), name = "workerRouter")

  def receive = {
	 
    //Start the worker
    case MiningWorkers =>
      for (i <- 0 until WorkersCount) workerRouter ! startMining(NumberOfZeros)
    //Sent the result to listener
    case aggregateCoins(coins) =>
      total_mined ++= coins
      //messages += 1
      //if (messages == MessageCount) {
        listener ! PrintResult(total_mined)
        //println("Server completed its work")
      //}
	  total_mined = new ArrayBuffer[String]()
  }
}

//The actor which assigns work to the Client
class AssignWorkActor(zeros : Int) extends Actor {
  def receive = {
    case "AssignWorkToMe" => {
      sender ! assignWork(zeros)
    }
    //Receive Message from client
    case RemoteMessage(buffer) => {
      println(buffer.length + " Bitcoins Received from client ")
      for (i <- 0 until buffer.length) {
        println(buffer(i))
      }
    }
    case "startserver" =>{
      println("Server Started")
    }
    case _ => {
      println("Unknown Message Received at Server")
    }

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
      
    while(System.currentTimeMillis - startTime<100000){
        var string:String = "shivdeep" +rand+count
        var Bitcoin = hash(string)
        var leading = Bitcoin.substring(0,zeros)
        var zeroString = "0" * zeros
        if(leading.equals(zeroString)){
          mined += string + "    ----   " + Bitcoin
        }
        count+=1
		
		if(count%100000==0){
			sender ! aggregateCoins(mined)
		}
    }
	
  }
    }

  

}

class Listener extends Actor {
  def receive = {
    case PrintResult(coins) =>
      {
       
    	  println("printing the Bit Coins generated by the Server")
    	  for(i <- 0 until coins.length){
           println(coins(i))
        }  
      }
  }
}