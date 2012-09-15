package ads

import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.mutable.ListBuffer
import scala.util.Random

import ads.message._
import ads.util._
import ads.Op._

/**
 * Companion object for Transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
object Transaction {

  private val rand = new Random()

//  private var x: Int = (System.currentTimeMillis / 1000).toInt
  private var x: Int = 0

  /** 
   * Returns the next available Transaction identifier.
   * 
   * @return The next available TID
   */
  def getNextTID: Int = synchronized {
    val ret = x
    x += 1
    return ret
  } // getNextTID

  def getRandomInt (n: Int): Int = synchronized {
    rand.nextInt(n)
  } // getRandomInt

} // Transaction

/**
 * A database transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param m The transaction manager object.
 * @param body The set of statements to be executed by this transaction.
 */
class Transaction(m: TransactionManager) extends Actor with Traceable[Transaction] {

  TRACE = true

  var timestamp = System.currentTimeMillis
    
  private var randOps = false
  private var randNum = 0
  
  /**
   * The transaction identifier.
   */
  var tid = Transaction.getNextTID

  def this(m: TransactionManager, rand: Int) {
    this(m)
    randOps = true
    randNum = rand
  } // this
  
  /**
   * The body of the transaction
   */
  def body() {}

  // implementation of act function for actor
  def act() {
    trace("T%d act()".format(tid))
    begin
    body
    commit
  } // act

  /**
   * Begin the transaction.
   */
  def begin() = {
    trace("T%d begin()".format(tid))
    m ! BeginMessage(this)
  } // begin

  /**
   * Read a value into the transaction from the database.
   *
   * @param oid The object identifier.
   * @return the value of the object.
   */
  def read(oid: Int): Any = {

    trace("T%d read(%d)".format(tid, oid))
    var ret: Any = null

    trace("T%d trying to request read".format(tid));
    m ! ReadMessage(this, oid)

    self.receive {
      case msg: PostponeReadMessage => {
	// wait for 1 sec
	// Thread sleep 1000
//	trace("T%d was instructed to postpone read request".format(tid))
	ret = this.read(oid)
      }
      case value: String => {
//	trace("T%d received value from read request".format(tid))
	ret = value
      }
    } // react

    ret
  } // read

  /**
   * Write a value into the database.
   *
   * @param oid The object identifier.
   * @param value The value to write into the database.
   */
  def write(oid: Int, value: Any): Unit = {
    trace("T%d write(%d, %s)".format(tid, oid, value))
    
    m ! WriteMessage(this, oid, value) 
    
    self.receive {
      case postpone: PostponeWriteMessage => {
	// wait for 1 sec
	//Thread sleep 1000
	this.write(oid, value)
      } // case
      case _ => { }
    } // react

  } // write


  /**
   * Commit this transaction.
   */
  def commit() = {
    trace("T%d commit()".format(tid))
    m ! CommitMessage(this)
    exit
  } // commit

  /**
   * Rollback this transaction.
   */
  def rollback() = {

    trace(Level.Warning, "T%d rollback()".format(tid))

    this.tid = Transaction.getNextTID
    this.timestamp = System.currentTimeMillis

    // make the transaction wait for a random amount of time
    Thread sleep Transaction.getRandomInt(1000)

    body
    exit

  } // rollback

  override def toString = "T%d".format(tid)

} // Transaction class
