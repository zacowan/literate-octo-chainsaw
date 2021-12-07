# Project Midpoint Submission

## Group Members

- Joel John
- Sahir Limage
- Zachary Cowan

## Things that Do Not Work

Everything in our project works as intended. However, we were not able to get either provided script for running peers remotely through a Java program to work. We were able to manually SSH into the servers and run our project perfectly fine, just not with the Java script. As a result, we ran the demo by manually SSHing into the Linux servers.

## Demo Video

Link: TODO

### Demo Issues

We encountered some issues during multiple runs of our demo video where servers were running slowly and some of our logs were out of sync as a result. Running when there is less traffic from students should resolve this issue, as well as running locally.

## Running the Project

### Compiling

1. Ensure you are in the root directory of our project (the root directory is outside of `src`) and that this directory contains the file `sources.txt`.
2. Run the command `javac -d _build_ '@sources.txt'` to compile our code.
3. The compiled code will be in a directory labeled `_build_` in our project root directory.

### Setup Scenario

Before running the program, ensure that **ALL** of the following conditions are true:

1. Ensure that `Common.cfg` and `PeerInfo.cfg` are in the same directory as our compiled code.
2. Ensure that the folders for all peers are in the same directory as our compiled code. NOTE: the directories should be named `peer_{PEER_ID}`, where `{PEER_ID}` is the ID of the peer. This notation is pulled directly from the project specification.
3. Ensure that the peers that have the file actually contain the file in their respective sub-directory. For example, if peer 1001 has the file, then the directory `peer_1001` should have a file with the name specified in `Common.cfg`.

### Run the Program

1. Ensure you are in the same directory as the config files, peer sub-directories, and our source code.

2. To run a particular peer, run the command `java peerProcess {PEER_ID}`, where `{PEER_ID}` is the ID of the peer. NOTE: all peers specified in `PeerInfo.cfg` must be started before the program will start to attempt to transfer piece data.
