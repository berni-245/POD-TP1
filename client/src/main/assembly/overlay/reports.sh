#!/bin/bash

MAIN_CLASS="ar.edu.itba.pod.client.ReportClient"
java "$@" -cp 'lib/jars/*' $MAIN_CLASS
