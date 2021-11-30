SHELL=/bin/sh
OUTDIR=out

JFLAGS=-d $(OUTDIR) -cp src --enable-preview --release 17 -Xlint:preview
JAR=automatonLoader.jar
MANIFEST=META-INF/Manifest.txt
JC=javac

PREFIX=src/me/kokokotlin/main
.SUFFIXES: .java .class

SRC = \
	$(PREFIX)/Main.java \
	$(PREFIX)/utils/Tuple.java \
	$(PREFIX)/engine/regex/Type.java \
	$(PREFIX)/engine/Symbol.java \
	$(PREFIX)/engine/State.java \
	$(PREFIX)/engine/AutomatonBase.java \
	$(PREFIX)/engine/DFA.java \
	$(PREFIX)/engine/NFA.java \
	$(PREFIX)/engine/ENFA.java \
	$(PREFIX)/engine/Loader.java \
	$(PREFIX)/engine/graphviz/DotEncoder.java \
	$(PREFIX)/engine/graphviz/DotUtils.java \
	$(PREFIX)/engine/regex/RegexStack.java \
	$(PREFIX)/engine/regex/RegexState.java \
	$(PREFIX)/engine/regex/RegularExpressionLoader.java \
	$(PREFIX)/engine/regex/SymbolFrequency.java \

CLASSES = $(patsubst src/%.java,out/%.class, $(SRC))
CLASSES_O = $(patsubst src/%.java,%.class, $(SRC))

all: $(OUTDIR) | $(CLASSES)

$(OUTDIR):
	mkdir -p $@
 
out/%.class : src/%.java
	$(JC) $(JFLAGS) $<

jar: $(OUTDIR) | $(CLASSES)
	cd out && jar --create --file $(JAR) --main-class me.kokokotlin.main.Main $(CLASSES_O) && mv $(JAR) ..

clean:
	rm -rf $(OUTDIR) $(JAR)