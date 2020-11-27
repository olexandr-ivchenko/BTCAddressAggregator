## Needs update

## Configuring your bitcoind to accept RPC calls
In order to let your bitcoind cli or Bitcoin QT accept RPC calls, following sipme configuration may be enough
```
# Allow getting all transactions
txindex=1
# server=1 tells Bitcoin-Qt and bitcoind to accept JSON-RPC commands
server=1
rpcuser=user
rpcpassword=password
```
