#!/bin/bash
javac -d _build_ @sources.txt

cp -a test/ _build_

cd _build_

parallel --halt now,fail=1 --lb java peerProcess ::: 1001 1002
