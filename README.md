# literate-octo-chainsaw

CNT4007 group project

Diagram: https://drive.google.com/file/d/170t8bSQvOByvuJmGKxeqEdJOYjQb3RMS/view?usp=sharing

## How to Run

1. Make sure `Common.cfg` and `PeerInfo.cfg` into the `_build_`.
2. Compile the source code with `javac -d _build_ '@sources.txt'`.
3. Go into the `_build_` directory with `cd ./_build_`.
4. Run the first peer with `java peerProcess PEER_ID1`.
5. In a separate terminal, `java peerProcess PEER_ID2`.

### How to Run Remote Peers

1. Make sure that all of the compiled java code, config files, and file to transfer are on all of the linux servers.
2. Compile the "StartRemotePeers.java" by running `javac src/StartRemotePeers.java -cp libraries/jsch-0.1.54.jar` in the root directory.
3. Run the command `java -cp .:../libraries/jsch-0.1.54.jar StartRemotePeers` inside of the `src/` directory.

#### Other

- `scp -r ~/Development/literate-octo-chainsaw/_build_/* zcowan@lin114-01.cise.ufl.edu:/cise/homes/zcowan/networking`

### Zipping for Midpoint Submission

Run the command `tar cvf proj1.tar src README_MIDPOINT.md sources.txt`.

## Deadlines

### October 22

Have 800 lines or more and no compilation errors. Runs and terminates.

### December 7

Final due date. Everything works.
