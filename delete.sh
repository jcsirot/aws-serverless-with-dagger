#! /bin/sh -eux

mvn -f ci/pom.xml -Pdelete compile exec:java