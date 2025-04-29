#!/bin/bash

MAIN_CLASS="ar.edu.itba.pod.client.management.TrainClient"
java "$@" -cp 'lib/jars/*' $MAIN_CLASS
