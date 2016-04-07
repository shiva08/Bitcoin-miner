Shivdeep Nutheti - shiva.0891@gmail.com     
  
The goal of this rst project is to use Scala and the actor model to build a
good solution to this problem that runs well on multi-core machines.

  1) Go to server folder which has build.sbt file, run the code from here to execute server. 
  2) Pass number of the leading zeroes .
  3) To start the client, Change the server / client IPs in the application.config files accordingly
  4) Go to client folder which has build.sbt file, compile and run the code from here to execute client. 
  5) Pass IP address of the server to get the work from server.

Server has a Master-actor who assigns the work to the workers. The workers will run sha function on the server node to mine the bitcoins. Mined coins will
be aggregated at the Master-actor and then passed to the listener-actor to print the results to the console. 
If the client comes in the middle and ask for the work. Server's Master-actor assigns the work to Client's Master-actor. This master does the job similar to what 
server does till aggregation. After Aggregation, all the coins will be passed to Server-Master, who passes the coins to it's Listener-actor to print
the bitcoins to console.


The following result was tested on Windows OS, one i5 processor of 4 cores and another one i4 processor of 4 cores

1)We determined results for work units of 100000 and 300000. Here, work unit is the number of inputs for which Bitcoins were mined

2) On running the code with k=4, managed to mine more than 100 Bitcoins in a duration of 10 seconds. 
The result file (four_zeros.txt) has been included in the DOS_Project1 zip.

3) For k=5, we managed to get 5 Bitcoins with a duration of 20 seconds.

4) The coins with the most 0s we managed to find were 6: refer Maxmium_zeros.txt

5) We tested the project by deploying on 2 machines.

References:
http://alvinalexander.com/
http://doc.akka.io/docs/akka/2.2-M2/scala/remoting.html
http://doc.akka.io/docs/akka/2.0/intro/getting-started-first-scala.html
http://www.scala-lang.org/documentation/
