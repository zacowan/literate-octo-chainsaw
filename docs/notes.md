# Project Notes

## Overview

- P2P file sharing, BitTorrent
- Java (Version X)

## Protocol Description

- TCP
- Symmetrical messages between peers
- Actual protocol
  1. Establish connection between peers
  2. Send handshake message between peers (see [handshake message](#handshake-message))
  3. Send an unlimited stream of messages between peers (see [actual message](#actual-message))

### Handshake Message

- 32 bytes total
  - 18-byte header string `P2PFILESHARINGPROJ`
  - 10-byte zero bits
  - 4-byte peer ID (integer representation of peer ID)

### Actual Message

- Variable size (max of 2^32 + 4 + 1? bytes)
  - 4-byte for specifying message length, in bytes
  - 1-byte for specifying message type (see [message types](#message-types))
  - X-byte message payload

#### Message Types

| message type                      | value |
| --------------------------------- | ----- |
| [choke](#choke)                   | 0     |
| [unchoke](#unchoke)               | 1     |
| [interested](#interested)         | 2     |
| [not interested](#not-interested) | 3     |
| [have](#have)                     | 4     |
| [bitfield](#bitfield)             | 5     |
| [request](#request)               | 6     |
| [piece](#piece)                   | 7     |

##### Choke

- No payload

##### Unchoke

- No payload

##### Interested

- No payload

##### Not Interested

- No payload

##### Bitfield

- Sent as first message after handshaking is done
- Use bitfield as payload
  - Each bit represents whether peer ahs the corresponding piece of a file or not
  - First byte corresponds to piece indices 0-7 from high bit to low bit. Next byte 8-15, etc.
  - Spare bits at end are set to zero

##### Request

- Payload of 4-byte piece index field

##### Piece

- Payload of 4-byte piece index field and the content of the piece

## Behavior Description

> A connection between two peers, A and B

### Handshake and Bitfield

1. A makes a TCP connection to B
2. A sends `handshake` to B; B sends `handshake` to A; Both check `handshake` header and that the peer ID is the expected one
3. A sends `bitfield` to B; B sends `bitfield` to A
4. A sends `interested` (B has pieces A does not) or `not interested` (A has all pieces of B) message to B

### Choke and Unchoke

- Peer uploads to at most `k` preferred neighbors and 1 optimistically unchoked neighbor
  - `k` given as parameter at program start
  - Unchoked neighers = `k` preferred + 1 unchoked
  - Choked neighbors = all others
- Determine preferred neighbors every `p` seconds
  > NOTE: if A has complete file, preferred neighbors are chosen _randomly_ instead of the process described below
  - Among interested neighbors, A picks `k` neighbors that have fed A's data at the highest rate
    - More than 2 neighbors = random decision
  - A sends `unchoke` messages to newly preferred neighbors, expects a `request` message
    - Does not send `unchoke` message if neighbor is already unchoked
  - All previously unchoked neighbors that are not preferred should be choked - A sends `choke` messages to them
- Determine optimistically unchoked neighbor every `m` seconds
  - A reselects an optimistically unchoked neighbor _randomly_ among choked neighbors
  - A sends `unchoke` message to selected neighbor and expects a `request` message

#### Special Case

> Suppose that peer C is randomly chosen as the optimistically unchoked neighbor of peer A. Because peer A is sending data to peer C, peer A may become one of peer C’s preferred neighbors, in which case peer C would start to send data to peer A. If the rate at which peer C sends data to peer A is high enough, peer C could then, in turn, become one of peer A’s preferred neighbors. Note that in this case, peer C may be a preferred neighbor and optimistically unchoked neighbor at the same time. This kind of situation is allowed. In the next optimistic unchoking interval, another peer will be selected as an optimistically unchoked neighbor.

### Interested and Not Interested

- Each peer sends an `interested` message to neighbors (if they are interested in their pieces) in any state (choked and unchoked)
  - Otherwise, sends `not interested` message
- Each peer maintains bitfields for all neighbors and updates them based on `have` messages from neighbors
- After each piece is received, a peer should check the bitfields of its neighbors and decides whether to send a `not interested` message

### Request and Piece

- When unchoked by a neighbor, peer sends `request` message
  - When examining pieces to request, selects randomly from the list of pieces it does not have and has not asked neighbors for
- When receiving a `request` message, peer sends a `piece` message with the actual piece
- After downloading the _entire_ piece, a peer may send another `request` message
- Exchange continues until A is choked by B, or B does not have any more interesting pieces
- There is no `cancel` message like there is in BitTorrent

#### Special Case

> Even though peer A sends a ‘request’message to peer B, it may not receive a ‘piece’message corresponding to it. This situation happens when peer B re-determines preferred neighbors or optimistically unchoked a neighbor and peer A is choked as the result before peer B responds to peer A. Your program should consider this case.
