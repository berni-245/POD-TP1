#!/bin/bash

PATH_TO_CODE_BASE=`pwd`
MAIN_CLASS="ar.edu.itba.pod.client.management.PlatformClient"
java "$@" -cp 'lib/jars/*' $MAIN_CLASS