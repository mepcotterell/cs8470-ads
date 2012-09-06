src = src
dest = classes
classpath = $(dest)
flags = -deprecation -cp $(classpath) -sourcepath $(src) -d $(dest)

all: ConcurrencyControl LockTable Page PrecedenceGraph Record StorageManager Transaction TransactionManager

classes:
	mkdir -p $(dest)

%.scala: $(src)/%.scala
	@touch $(src)/%.scala

ConcurrencyControl: classes ConcurrencyControl.scala
	scalac $(flags) $(src)/ConcurrencyControl.scala

LockTable: classes LockTable.scala
	scalac $(flags) $(src)/LockTable.scala

Page: classes Record Page.scala
	scalac $(flags) $(src)/Page.scala

PrecedenceGraph: classes PrecedenceGraph.scala
	scalac $(flags) $(src)/PrecedenceGraph.scala

Record: classes Record.scala
	scalac $(flags) $(src)/Record.scala

StorageManager: classes StorageManager.scala
	scalac $(flags) $(src)/StorageManager.scala

Transaction: classes LockTable Transaction.scala
	scalac $(flags) $(src)/Transaction.scala

TransactionManager: classes ConcurrencyControl StorageManager Transaction TransactionManager.scala
	scalac $(flags) $(src)/TransactionManager.scala

clean:
	rm -rf $(dest)