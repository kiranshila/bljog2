#!/usr/bin/env sh
native-image \
    -jar target/bljog.jar \
    -H:Name=bljog \
    -H:+ReportExceptionStackTraces \
    --initialize-at-build-time  \
    --verbose \
    --no-fallback \
    --no-server \
    --static \
    "-J-Xmx3g" \
    target/bljog
