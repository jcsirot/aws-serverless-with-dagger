#! /bin/sh -eux

mvn -f ci/pom.xml -Pdeploy exec:java