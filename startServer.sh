#!/bin/bash

SCRIPTBASEPATH=`readlink -f $0`
BASEDIR=`dirname $SCRIPTBASEPATH`
SCRIPTNAME=`basename $0`


CLASS_PATH="/usr/share/java/commons-daemon.jar":$BASEDIR'/MCDaemon.jar'
CLASS=org.finomnis.mcdaemon.Main
USER=root
PIDFILE=$BASEDIR"/mcdaemon.pid"
LOG=$BASEDIR"/mcdaemon.log"
JAVA_HOME="$( readlink -f "$( which java )" | sed "s:bin/.*$::" )"


do_exec()
{
	if [ "$1" == "-stop" ]; then
		echo "Stopping server..."
	else
		echo "Starting server..."
	fi

	jsvc -home "$JAVA_HOME" -cp $CLASS_PATH -user $USER -outfile $LOG -errfile $LOG -pidfile $PIDFILE $1 $CLASS
}

case $1 in
	start)
		if [ -f $PIDFILE ]; then
			echo "MCDaemon already running."
		else
			do_exec
		fi
		;;
	stop)
		if [ -f $PIDFILE ]; then
			do_exec "-stop"
		else
			echo "MCDaemon not running."
		fi
		;;
	restart)
		if [ -f $PIDFILE ]; then
			do_exec "-stop"
		fi
		do_exec
		;;
	status)
		if [ -f $PIDFILE ]; then
			PID=`cat $PIDFILE`
			echo "MCDaemon is running. (PID: $PID)" 
		else
			echo "MCDaemon is not running."
			exit 1
		fi
		;;
	*)
		echo "Usage:"
		echo
		echo "      ./${SCRIPTNAME} [start|stop|restart|status]"
		echo
		exit 3
		;;
esac
