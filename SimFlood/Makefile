all:
	javac -cp .:./OWL/owlapi-bin.jar $(find . -name "*.java")

match:
	javac -cp .:./OWL/owlapi-bin.jar \
		./org/w3c/rdf/util/SFConsumer.java \
		./com/interdataworking/mm/alg/Match.java \
		./org/w3c/rdf/implementation/syntax/sirpac/SiRPAC.java

run:
	java -cp .:./OWL/owlapi-bin.jar \
					com.interdataworking.mm.alg.Match onto.rdf onto_synonym.rdf

editMatch:
	vim ./com/interdataworking/mm/alg/Match.java

editConsumer:
	vim ./org/w3c/rdf/util/SFConsumer.java

editSirpac:
	vim	./org/w3c/rdf/implementation/syntax/sirpac/SiRPAC.java

editMatch2:
	emacs ./com/interdataworking/mm/alg/Match.java

editConsumer2:
	emacs ./org/w3c/rdf/util/SFConsumer.java

editSirpac2:
	emacs	./org/w3c/rdf/implementation/syntax/sirpac/SiRPAC.java

