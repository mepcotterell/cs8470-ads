package ads
package concurrency

import scala.collection.mutable.ListMap

import ads.Op._
import ads.Transaction
import ads.message._

/**
 * Implementation of Time Stamp Ordering
 *
 * TODO implement recoverability
 * 
 * @author Michael E. Coterell
 * @author Terrance Medina
 */
trait TSO extends ConcurrencyControl {

  /**
   * Holds timestamp information for an object.
   * 
   * @param r The largest timestamp of any transaction that has read the object.
   * @param w The largest timestamp of any transaction that has updated the
   *          object.
   * @param c A flag that indicates whether the transaction that last wrote to
   *          the object has committed.
   */
  case class TSOTimestamp(var r: Long, var w: Long, var c: Boolean)

  /**
   * Timestamp Map
   */
  val map = ListMap.empty[Int, TSOTimestamp]

  override def check(t: Transaction, opType: Op, oid: Int): Boolean = {

    if (opType == Op.Read) map.get(oid) match {

      case None => {
	map += oid -> TSOTimestamp(0, System.currentTimeMillis, true)
	return true
      } // case

      case Some(ts) => {

	if (t.timestamp < ts.w) {

	  // If the transaction's timestamp is less than the timestamp of the the
	  // most recent write on the object, then we need to rollback the
	  // transaction

	  t.rollback
	  return false

	} else {

	  // If the transaction's timestamp is greater than the timestamp of the
	  // most recent write on the object, then we need to handle two cases:

	  if (ts.c) {

	    // If the last transaction to write to the object has committed, 
	    // then we update the read timestamp, if necessary and the read
	    // request is granted.

	    if (t.timestamp > ts.r) ts.r = t.timestamp
	    return true

	  } else {

	    // If the last transaction to write to the object has not committed,
	    // then we need to send a postpone message to the transaction in
	    // order to avoid a dirty read

	    t ! PostponeReadMessage()
	    return false

	  } // if

	} // if

      } // case

    } // if

    if (opType == Op.Write) map.get(oid) match {

      case None => {
	map += oid -> TSOTimestamp(System.currentTimeMillis, 0, true)
	return true;
      } // case

      case Some(ts) => {

	if (t.timestamp < ts.r) {

	  // If the transaction's timestamp is less than the timestamp of the
	  // last read to the object then the transaction is rolledback

	  t.rollback
	  return false

	} else if (ts.r < t.timestamp && t.timestamp < ts.w) {

	  // If the timestamp of the transaction is greater than the timestamp
	  // of the last read to the object and is less than the timestamp of
	  // the last write to the object then we need to consider two cases:

	  if (ts.c) {

	    // If the last transaction to write to the object has committed then
	    // we invoke the Thomas Write Rule! In our implementation, granting
	    // a request causes interaction with the StorageManager. If we deny
	    // the request but don't rollback or postpone the transaction, we 
	    // effectively do the same thing as granting the request without
	    // actually performing the write.

	    return false

	  } else {

	    // If the last transaction to write to the object has not committed,
	    // then postpone the write

	    t ! PostponeWriteMessage()
	    return false

	  } // if

	} else {

	  // Otherwise we have two cases:

	  if (ts.c) {

	    // If the last transaction to write to the object has committed then
	    // the request is granted. The value of this transactions timestamp
	    // is assigned to ts.w and the value of ts.c is set to false.

	    ts.w = t.timestamp
	    ts.c = false

	    return true

	  } else {

	    t ! PostponeWriteMessage()
	    return false

	  } // if

	} // if

      } // case

    } // if

   // If the operation being requested by the transaction is not a read or a
   // write then we can go ahead and grant the request.

   return true

  } // check  

} // TSO
