#!/bin/bash

MAIN_CLASS="ar.edu.itba.pod.client.PlatformClient"
java "$@" -cp 'lib/jars/*' $MAIN_CLASS
