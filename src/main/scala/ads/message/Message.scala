package ads.message

import akka.actor.ActorRef

import ads.Op._

/**
 * Represents a message.
 * 
 * @author Michael E. Cotterell
 * @author Terrance Medina	
 */
abstract class Message () { }

/**
 * A general purpose message indicating an okay.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina

 */
case class OkayMessage () extends Message ()

/**
 * A message idicating that a Transaction has begun.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param t A Transaction object.
 */
case class BeginMessage (t: ActorRef) extends Message ()

case class TimestampRequest () extends Message ()
case class TIDRequest () extends Message ()

/**
 * A message indicating that a Transaction requests to read a value from
 * the database.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param t A Transaction object.
 * @param oid An object identifier.
 */
case class ReadMessage (t: ActorRef, oid: Int) extends Message ()

/**
 * A message indicating that a Transaction requests to write a value into
 * the database.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param t A Transaction object.
 * @param oid An object identifier.
 * @param value The value that is to be written.
 */
case class WriteMessage (t: ActorRef, oid: Int, value: Any) extends Message ()

/**
 * A message indicating that a Transaction requests to commit itself to the
 * database.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param t A Transaction object.
 */
case class CommitMessage (t: ActorRef) extends Message ()

/**
 * A message indicating that a Transaction should postone a read for some amount
 * of time.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
case class PostponeReadMessage () extends Message ()

/**
 * A message indicating that a Transaction should postone a write for some amount
 * of time.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
case class PostponeWriteMessage () extends Message ()

