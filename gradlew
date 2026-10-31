#!/usr/bin/env bash
export GRADLE_HOME=/home/workdir/gradle-dist/gradle-8.9
exec "$GRADLE_HOME/bin/gradle" "$@"
