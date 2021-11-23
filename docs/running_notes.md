# Running Notes

## Choke/Unchoke on Server

Every `p` seconds, do:

1. Among interested neighbors, pick `k` that have fed data at the highest rate
2. Send unchoke message to all of them
3. Send choke message to previously unchoked neighbors

### Tasks

- Keep track of data being fed to server
- Way to send message across ALL server threads
- Way to run a function every `x` seconds

## Have Message

1. When a peer receives an entire piece, it must notify all of its connected peers that it has that piece.
2. `Have` payload contains the index of the piece that was received.

### Tasks

1. Create a `have` payload, containing the index of the piece.
2. Create a list of the sockets of the connected peers (Server.java).
3. Update handlePieceReceived on the Client to send a `have` message.
4. New method handleHaveReceived that updates the bitfield (Client.java).
