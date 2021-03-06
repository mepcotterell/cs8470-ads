package ads

import scala.collection.mutable.ListBuffer

import akka.actor.{ Actor, ActorRef, ActorSystem, Props, TypedActor }
import akka.dispatch.{ Await, Future }
import akka.event.Logging
import akka.pattern.{ ask, pipe }
import akka.util.duration._
import akka.util.Timeout

import ads.concurrency.ConcurrencyControl
import ads.message.{ BeginMessage, CheckResponse, CommitMessage, ReadMessage, ReadResponse, WriteMessage, WriteResponse, StatMessage }
import ads.message.CheckResponses._
import ads.util._
import ads.Op._

// only to be used by the transaction manager (unless shutdown)
object TMStats {
  var start: Long = System.currentTimeMillis
  var end: Long = 0
  var count: Int = 0
  var txns: Int= 0
  var rolls: Int = 0
} // TMStats

/**
 * Handles operation requests from the various Transaction threads.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
class TransactionManager(db: Database) extends Actor with ConcurrencyControl {

  /**
   * Used for trace statements
   */
  private val trace = Logging(context.system, this) 

  /**
   * 	Incoming operation buffer.
   */
  private val opBuffer = new ListBuffer[(Transaction, Op, Int)]()

  def statTally = {
    val totalTime = System.currentTimeMillis - TMStats.start
    TMStats.count += 1
    
  } // statTally

  def receive = {
    
    case statMsg: StatMessage => {
      TMStats.end = System.currentTimeMillis
      val totalTime = TMStats.end - TMStats.start
      println("STATS!");
      println("transaction committed = " + TMStats.count)
      println("total time = " + totalTime + "ms")
      println("throughput = " + (TMStats.count.toDouble / (totalTime.toDouble / 1000.0)) + " tps")   
    } // stats

    case bMsg: BeginMessage => {
      trace.info("Message recieved: %s".format(bMsg))
      TMStats.txns += 1
    }

    case rMsg: ReadMessage => {
      trace.info("Message recieved: %s".format(rMsg))

      check(rMsg.t, Op.Read, rMsg.oid) match {
	case Granted    => {
	  trace.info("read request granted: %s".format(rMsg))
	  val future: Future[Option[Any]] = ask(db.sm, Read(rMsg.t.getTID.get, rMsg.table, rMsg.oid, rMsg.prop)).mapTo[Option[Any]]
	  val response: Option[Any] = Await.result(future, 10 second)
	  sender ! ReadResponse(response.get)
	} // case
	case Denied     => {
	  trace.info("read request denied: %s".format(rMsg))
	  sender ! ReadResponse(value = "hello", denied = true)
	} // case
	case Postponed  => {
	  trace.info("read request postponed: %s".format(rMsg))
	  sender ! ReadResponse(value = "hello", postpone = true)
	} // case
	case Rollbacked => {
	  trace.info("read request resulted in rollback: %s".format(rMsg))
	  sender ! ReadResponse(value = "hello", rollback = true)
          TMStats.rolls += 1
	} // case
	case _ => {
	  trace.info("read request resulted in unknown message: %s".format(rMsg))
	} // case
      } // match

      // sender ! "hello"

    } // case

    case wMsg: WriteMessage => {

      trace.info("Message recieved: %s".format(wMsg))

      check(wMsg.t, Op.Write, wMsg.oid) match {
	case Granted    => {
	  trace.info("write request granted: %s".format(wMsg))
	  db.sm ! Write(wMsg.t.getTID.get, wMsg.table, wMsg.oid, wMsg.prop, wMsg.value)
	  sender ! WriteResponse(false, false, false)
	} // case
	case Denied     => {
	  trace.info("write request denied: %s".format(wMsg))
	  sender ! WriteResponse(false, true, false)
	} // case
	case Postponed  => {
	  trace.info("write request postponed: %s".format(wMsg))
	  sender ! WriteResponse(true, false, false)
	} // case
	case Rollbacked => {
	  trace.info("write request resulted in rollback: %s".format(wMsg))
	  sender ! WriteResponse(false, false, true)
          TMStats.rolls += 1
	} // case
	case Thomas     => {
	  trace.info("write request invoked Thomas Write Rule?: %s".format(wMsg))
	  sender ! WriteResponse(false, false, false)
	} // case
      } // match

    } // case

    case cMsg: CommitMessage => {
      trace.info("Message recieved: %s".format(cMsg))
      db.sm ! Commit(cMsg.t.getTID.get)
      TypedActor(db.system).poisonPill(sender)
      statTally
      TMStats.txns -= 1
    } // case

  } // recieve

} // TransactionManager

