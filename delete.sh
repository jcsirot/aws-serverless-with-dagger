#! /bin/sh -eux

mvn -f ci/pom.xml -Pdelete exec:java