# Project Midpoint Submission

## Group Members

- Joel John
- Sahir Limage
- Zachary Cowan

## Getting Started

1. To compile our project, make sure you have a recent version of Java installed (we used Java 16, but it should work for Java 8+).
2. To compile our code, please run `javac -d _build_ @sources.txt`. This will compile all of the Java files we have specified in our `sources.txt` file into a directory named `_build_`.
3. To run a "peer", go into the `_build_` directory with `cd _build_` and run the command `java peerProcess PEER_ID`, where `PEER_ID` is a number representing the peer ID specified in the `PeerInfo.cfg` file. For example, `java peerProcess 1001`.

> Note: our project **requires** the `Common.cfg`, `PeerInfo.cfg`, and "the file" to be transferred specified in `Common.cfg` to be in the `_build_` directory when a "peer" is run.
