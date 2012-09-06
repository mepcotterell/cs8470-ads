import collection.mutable.ListBuffer

class Transaction (tid: Int, m: TransactionManager, ops: ListBuffer[(Char, Long)]) extends Thread with ReadWrite
{
    private var randOps = false
    private var randNum = 0

    def this (tid: Int, m: TransactionManager, rand: Int) {
      this(tid, m, null)
      randOps = true
      randNum = rand
    } // this

    /*******************************************************************************
     * Run the transaction: begin, reads/writes, commit.
     */
    override def run ()
    {
        begin ()
	if (randOps) {
	  
	} else {

	}
        commit ()
    } // run

    /*******************************************************************************
     * Begin this transaction.
     */
    def begin ()
    {
        println ("begin transaction " + tid)
        m.begin (tid)
    } // begin

    /*******************************************************************************
     * Read data object oid.
     * @param oid  the database object
     */
    def read (oid: Int): Array[Any] =
    {
        val value = m.read (tid, oid)
	println ("read " + tid + " ( " + oid + " ) value = " + value)
        value
    } // read

    /*******************************************************************************
     * Write data object oid.
     * @param oid  the database object
     */
    def write (oid: Int, value: Array[Any])
    {
        println ("write " + tid + " ( " + oid + " ) value = " + value)
        m.write (tid, oid, value)
    } // write

    /*******************************************************************************
     * Commit this transaction.
     */
    def commit ()
    {
        println ("commit transaction " + tid)
        m.commit (tid)
    } // commit

} // Transaction class

