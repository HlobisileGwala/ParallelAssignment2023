BIN = bin
SRC = src

SEQUENTIAL = TerrainArea Search MonteCarloMinimization
PARALLEL =  TerrainArea SearchParallel MonteCarloMinimizationParallel

${BIN}/%.class:${SRC}/MonteCarloMini/%.java
	javac -d ${BIN} -cp ${BIN} $<

default: ${PARALLEL:%=${BIN}/MonteCarloMini/%.class}

compile_seqential: ${SEQUENTIAL:%=${BIN}/MonteCarloMini/%.class}

sequential: compile_seqential
	java -cp ${BIN} MonteCarloMini.MonteCarloMinimization 5000 5000 0 100 0 100 0.1

parallel: default
	java -cp ${BIN} MonteCarloMini.MonteCarloMinimizationParallel 5000 5000 0 100 0 100 0.1


clean:
	rm -r bin