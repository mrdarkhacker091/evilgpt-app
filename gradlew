#!/bin/sh
DIR=$(dirname "$0")
DIR=$(cd "$DIR" && pwd)
exec java -Xmx64m -Xms64m -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
