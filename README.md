# literate-octo-chainsaw

CNT4007 group project

Diagram: https://drive.google.com/file/d/170t8bSQvOByvuJmGKxeqEdJOYjQb3RMS/view?usp=sharing

## How to Run

1. Make sure `Common.cfg` and `PeerInfo.cfg` into the `src/`.
2. Go into the src directory with `cd src`.
3. Compile the source code with `javac *.java`.
4. Run the first peer with `java peerProcess PEER_ID1`.
5. In a separate terminal, `java peerProcess PEER_ID2`.

### Using `run_dev.sh`

1. Make sure you have GNU's `parallel` installed.
2. Give the script the proper permissions with `chmod +x run_dev.sh`.
3. Run the script with `./run_dev.sh`.

## Deadlines

### October 22

Have 800 lines or more and no compilation errors. Runs and terminates.

### December 7

Final due date. Everything works.
