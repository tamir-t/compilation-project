###############
# DIRECTORIES #
###############
BASEDIR           = $(shell pwd)
JFlex_DIR         = ${BASEDIR}/FOLDER_0_JFlex
CUP_DIR           = ${BASEDIR}/FOLDER_1_CUP
SRC_DIR           = ${BASEDIR}/FOLDER_2_SRC
BIN_DIR           = ${BASEDIR}/FOLDER_3_BIN
INPUT_DIR         = ${BASEDIR}/FOLDER_4_INPUT
OUTPUT_DIR        = ${BASEDIR}/FOLDER_5_OUTPUT
EXTERNAL_JARS_DIR = ${BASEDIR}/FOLDER_7_EXTERNAL_JARS
MANIFEST_DIR      = ${BASEDIR}/FOLDER_8_MANIFEST

#########
# FILES #
#########
JFlex_GENERATED_FILE      = ${SRC_DIR}/Lexer.java
CUP_GENERATED_FILES       = ${SRC_DIR}/Parser.java ${SRC_DIR}/TokenNames.java
JFlex_CUP_GENERATED_FILES = ${JFlex_GENERATED_FILE} ${CUP_GENERATED_FILES}
SRC_FILES                 = ${SRC_DIR}/*.java              \
                            ${SRC_DIR}/ast/*.java          \
							${SRC_DIR}/ast/*/*.java        \
							${SRC_DIR}/ast/*/*/*.java      \
                            ${SRC_DIR}/types/*.java        \
							${SRC_DIR}/types/*/*.java      \
							${SRC_DIR}/utils/*.java        \
							${SRC_DIR}/utils/*/*.java      \
							${SRC_DIR}/ir/*/*.java         \
                            ${SRC_DIR}/symbols/*.java	   \
							${SRC_DIR}/asm/*.java	   	   \
							${SRC_DIR}/ir/*/*.java	       \
							${SRC_DIR}/ir/*/*/*.java	   \
							${SRC_DIR}/symbols/*.java	   
EXTERNAL_JAR_FILES        = ${EXTERNAL_JARS_DIR}/java-cup-11b-runtime.jar
MANIFEST_FILE             = ${MANIFEST_DIR}/MANIFEST.MF

########################
# DEFINITIONS :: JFlex #
########################
JFlex_PROGRAM  = jflex
JFlex_FLAGS    = -q
JFlex_DEST_DIR = ${SRC_DIR}
JFlex_FILE     = ${JFlex_DIR}/LEX_FILE.lex

######################
# DEFINITIONS :: CUP #
######################
CUP_PROGRAM                    = java -jar ${EXTERNAL_JARS_DIR}/java-cup-11b.jar 
CUP_FILE                       = ${CUP_DIR}/CUP_FILE.cup
CUP_GENERATED_PARSER_NAME      = Parser
CUP_GENERATED_SYMBOLS_FILENAME = TokenNames
CUP_FLAGS =                                \
-nowarn                                    \
-parser  ${CUP_GENERATED_PARSER_NAME}      \
-symbols ${CUP_GENERATED_SYMBOLS_FILENAME} 

#########################
# DEFINITIONS :: PARSER #
#########################
INPUT    	= ${INPUT_DIR}/Input.txt
OUTPUT   	= ${OUTPUT_DIR}/Output.s
OUTPUT_IR   = ${OUTPUT_DIR}/Output.ir

##########
# TARGET #
##########
compile:
	mkdir -p ${BIN_DIR}
	mkdir -p ${OUTPUT_DIR}
	clear
	@echo "*******************************"
	@echo "*                             *"
	@echo "*                             *"
	@echo "* [0] Remove COMPILER program *"
	@echo "*                             *"
	@echo "*                             *"
	@echo "*******************************"
	rm -rf COMPILER
	@echo "\n"
	@echo "************************************************************"
	@echo "*                                                          *"
	@echo "*                                                          *"
	@echo "* [1] Remove *.class files and JFlex-CUP generated files:  *"
	@echo "*                                                          *"
	@echo "*     Lexer.java                                           *"
	@echo "*     Parser.java                                          *"
	@echo "*     TokenNames.java                                      *"
	@echo "*                                                          *"
	@echo "************************************************************"
	rm -rf ${JFlex_CUP_GENERATED_FILES} ${BIN_DIR}/*.class ${BIN_DIR}/AST/*.class
	@echo "\n"
	@echo "************************************************************"
	@echo "*                                                          *"
	@echo "*                                                          *"
	@echo "* [2] Use JFlex to synthesize Lexer.java from LEX_FILE.lex *"
	@echo "*                                                          *"
	@echo "*                                                          *"
	@echo "************************************************************"
	$(JFlex_PROGRAM) ${JFlex_FLAGS} -d ${JFlex_DEST_DIR} ${JFlex_FILE}
	@echo "\n"
	@echo "*******************************************************************************"
	@echo "*                                                                             *"
	@echo "*                                                                             *"
	@echo "* [3] Use CUP to synthesize Parser.java and TokenNames.java from CUP_FILE.cup *"
	@echo "*                                                                             *"
	@echo "*                                                                             *"
	@echo "*******************************************************************************"
	$(CUP_PROGRAM) ${CUP_FLAGS} -destdir ${SRC_DIR} ${CUP_FILE}
	@echo "\n"
	@echo "********************************************************"
	@echo "*                                                      *"
	@echo "*                                                      *"
	@echo "* [4] Create *.class files from *.java files + CUP JAR *"
	@echo "*                                                      *"
	@echo "*                                                      *"
	@echo "********************************************************"
	javac -cp ${EXTERNAL_JAR_FILES} -d ${BIN_DIR} ${SRC_FILES}
	@echo "\n"
	@echo "***********************************************************"
	@echo "*                                                         *"
	@echo "*                                                         *"
	@echo "* [5] Create a JAR file from from *.class files + CUP JAR *"
	@echo "*                                                         *"
	@echo "*                                                         *"
	@echo "***********************************************************"
	jar cfm COMPILER ${MANIFEST_FILE} -C ${BIN_DIR} .
		
run: clear
	@echo "Run compiler on input"
	java -jar COMPILER ${INPUT} ${OUTPUT} ${OUTPUT_IR}
	spim -f ${OUTPUT}

clear:
	mkdir -p ${OUTPUT_DIR}
	rm -rf ${OUTPUT_DIR}/*

test: clear
	./run_tests.sh

all_tests: clear
	./run_tests.sh --all