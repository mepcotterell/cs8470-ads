package ads

import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer

import message._
import Op._

/**
 * Companion object for Transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
object Transaction {

  private var x: Int = (System.currentTimeMillis / 1000).toInt

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

} // Transaction

/**
 * A database transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param m The transaction manager object.
 * @param body The set of statements to be executed by this transaction.
 */
class Transaction(m: TransactionManager) extends Actor {

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
    begin
    body
    commit
  } // act

  /**
   * Begin the transaction.
   */
  def begin() = m ! BeginMessage(this)

  /**
   * Read a value into the transaction from the database.
   *
   * @param oid The object identifier.
   * @return the value of the object.
   */
  def read(oid: Int): Any = {
    m ! ReadMessage(this, oid)
    var ret: Any = null
    react {
      case value: Any => ret = value
    }
    ret
  }

  /**
   * Write a value into the database.
   *
   * @param oid The object identifier.
   * @param value The value to write into the database.
   */
  def write(oid: Int, value: Any) = m ! WriteMessage(this, oid, value)

  /**
   * Commit this transaction.
   */
  def commit() = {
    m ! CommitMessage(this)
    exit
  } // commit

  /**
   * Rollback this transaction.
   */
  def rollback() = {

    this.tid = Transaction.getNextTID

    // TODO make it wait some random amount of time

    restart

  } // rollback

  override def toString = "T%d".format(tid)

} // Transaction class

