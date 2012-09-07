/** 
 *	An enum that defines op types.
 *
 *	@author Michael Cotterell
 *	@author Terrance Medina
 *	
 *
object Op extends Enumeration {
  type Op = Value
  val Read, Write, SharedLock, ExclusiveLock, UpgradeLock, Unlock, Commit = Value
} // Op
