#!/bin/bash
# Driver script for the ASPIREdb CLI.
# $Id$

# You must define $ASPIREDB_LIB in your env or here.
# ASPIREDB_LIB=~/aspiredb-lib
JARS=$(echo ${ASPIREDB_LIB}/* | tr ' ' ':') 

APPARGS=$@

JAVACMD="${JAVA_HOME}/bin/java $JAVA_OPTS"

CMD="$JAVACMD -classpath ${ASPIREDB_LIB}:${JARS} $APPARGS"
CMD_DEFAULT="$JAVACMD -classpath ${ASPIREDB_LIB}:${JARS} ubc.pavlab.aspiredb.cli.AspiredbCLI"

if [ -z "$1" ]
	then
		$CMD_DEFAULT
		exit
fi
 
$CMD
