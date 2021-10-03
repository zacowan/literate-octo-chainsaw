#!/bin/bash
javac -d _build_ src/p2p/Server.java
java -cp _build_ p2p.Server
