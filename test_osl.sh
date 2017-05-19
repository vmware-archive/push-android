#!/bin/sh

docker run -v `pwd`:/push  programmers/osl-ci /bin/bash -c "cd push && ./install_android.sh && ./gradlew --info printVersion clean uploadArchives"
