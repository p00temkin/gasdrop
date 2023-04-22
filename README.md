## GASDROP

Tool to automatically airdrop EVM gas to specified accounts, used for testing purposes.  

| ![alt text](https://github.com/p00temkin/gasdrop/blob/master/img/gasdrop.png?raw=true) |
| :--: |

### Step 1: Create a funding wallet

We can create a named 'funder' using the WALLETCONFIG action

   ```
	java -jar ./gasdrop.jar 
	--action WALLETCONFIG 
	--walletname funder
	--privkey 0x59c6..
   ```

This creates a walletfile in your local .evm/wallets folder (with the walletname 'funder' in this case) with address 0x70997970c51812dc3a010c7d01b50e0d17dc79c8. 

### Step 2: Fund your 'funder' wallet

This can be done simply by transferring some/all gas tokens from the pre-funded accounts when running Hardhat or Ganache as test nodes. 

### Step 3: Create a file with all accounts to fund

   ```
   echo '0x6e4cc65a90de0985bc34d4d17d8bf87b204b2e44' >> wallets.topup
   echo '0xaf4311d557fbc876059e39306ec1f3343753df29' >> wallets.topup
   echo '0xf9b8d95b9e9c17e3d923a68e8773bc323594dbc9' >> wallets.topup
   ..
   ```

### Step 4: Start the infinite funding loop

To keep all account balances in the 'wallets.topup' file above above 1 ETH run the following command:

   ```
	java -jar ./gasdrop.jar 
	--action FUNDLOOP 
	--chain HARDHAT8545_31337 
	--walletname funder
	--topupfile ./wallets.tofund
	--minbalance 1.0
	--sleeplen 10
   ```

The chain 'HARDHAT8545_31337' here represents a Hardhat test node with RPC on TCP port 8545 and configured with chainid 31337. The gasdrop tool will check the account balances every 10 seconds and topup with a new 'minbalance' transfer if needed. 

### Prerequisites

[Java 17+, Maven 3.x]

   ```
   java -version # jvm 17+ required
   mvn -version # maven 3.x required
   git clone https://github.com/p00temkin/forestfish
   mvn clean package install
   ```

### Building the application

   ```
   mvn clean package
   mv target/gasdrop-0.0.1-SNAPSHOT-jar-with-dependencies.jar ./gasdrop.jar
   ```

### Usage

   ```
   java -jar ./gasdrop.jar 
   ```

Options:

   ```
   --chain				The EVM chain: ETHEREUM, POLYGON, .. (see forestfish docs for network aliases)
   --action			Action to perform: WALLETCONFIG, FUNDLOOP
   --walletname			Wallet name to use for specified action
   --mnemonic			Mnemonic to use for creating an EVM account. Use with --walletname
   --privkey			Mnemonic to use for creating an EVM account. Use with --walletname
   --minbalance			Minimum balance for target accounts (no decimals included, so 1.0d amounts to 1 ETH)
   --topupfile			Path to file with target accounts, one EVM account per line
   --sleeplen			Number of seconds to sleep between account balance checks (defaults to 60 seconds)
   ```

  ### Support/Donate

To support this project directly:

   ```
   Ethereum/EVM: forestfish.x / 0x207d907768Df538F32f0F642a281416657692743
   Algorand: forestfish.x / 3LW6KZ5WZ22KAK4KV2G73H4HL2XBD3PD3Z5ZOSKFWGRWZDB5DTDCXE6NYU
   ```

Or please consider donating to EFF:
[Electronic Frontier Foundation](https://supporters.eff.org/donate)