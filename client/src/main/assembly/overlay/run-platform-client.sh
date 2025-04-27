#!/bin/bash

MAIN_CLASS="ar.edu.itba.pod.client.management.PlatformClient"
java "$JAVA_OPTS" -cp 'lib/jars/*' $MAIN_CLASS "$@"

# tar -xf "client/target/tpe1-g3-client-1.0-SNAPSHOT-bin.tar.gz" -C "client/target/"
# java -cp "client/target/tpe1-g3-client-1.0-SNAPSHOT/lib/jars/*" "ar.edu.itba.pod.client.management.PlatformClient" "$@"
