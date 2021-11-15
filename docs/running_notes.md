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