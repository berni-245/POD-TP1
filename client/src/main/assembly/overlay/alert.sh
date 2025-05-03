#!/bin/bash

MAIN_CLASS="ar.edu.itba.pod.client.NotificationClient"
java "$@" -cp 'lib/jars/*' $MAIN_CLASS
