#!/bin/bash
javac -d _build_ src/p2p/Client.java src/p2p/MessageHandler.java
java -cp _build_ p2p.Client
