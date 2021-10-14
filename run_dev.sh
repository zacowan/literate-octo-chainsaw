#!/bin/bash
javac -d _build_ src/*.java
cp test/*.cfg _build_
cp test/thefile _build_

cd _build_

parallel --halt now,fail=1 --lb java peerProcess ::: 1001 1002
