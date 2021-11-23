# literate-octo-chainsaw

CNT4007 group project

Diagram: https://drive.google.com/file/d/170t8bSQvOByvuJmGKxeqEdJOYjQb3RMS/view?usp=sharing

## How to Run

1. Make sure `Common.cfg` and `PeerInfo.cfg` into the `src/`.
2. Go into the src directory with `cd src`.
3. Compile the source code with `javac -d _build_ @sources.txt`.
4. Go into the `_build_` directory with `cd ./_build_`.
5. Run the first peer with `java peerProcess PEER_ID1`.
6. In a separate terminal, `java peerProcess PEER_ID2`.

### Using `run_dev.sh`

1. Make sure you have GNU's `parallel` installed.
2. Give the script the proper permissions with `chmod +x run_dev.sh`.
3. Run the script with `sh run_dev.sh`.

### Zipping for Midpoint Submission

Run the command `tar cvf proj1.tar src README_MIDPOINT.md sources.txt`.

## Deadlines

### October 22

Have 800 lines or more and no compilation errors. Runs and terminates.

### December 7

Final due date. Everything works.
