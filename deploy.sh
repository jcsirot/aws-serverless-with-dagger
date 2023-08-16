#! /bin/sh -eux

mvn -f ci/pom.xml -Pdeploy compile exec:java