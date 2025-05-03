#!/bin/bash

MAIN_CLASS="ar.edu.itba.pod.client.TrainClient"
java "$@" -cp 'lib/jars/*' $MAIN_CLASS

