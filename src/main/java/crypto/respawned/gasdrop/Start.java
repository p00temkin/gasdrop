package crypto.respawned.gasdrop;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Convert.Unit;

import crypto.forestfish.enums.evm.EVMChain;
import crypto.forestfish.objects.evm.EVMAccountBalance;
import crypto.forestfish.objects.evm.EVMLocalWallet;
import crypto.forestfish.objects.evm.EVMNativeValue;
import crypto.forestfish.objects.evm.connector.EVMBlockChainConnector;
import crypto.forestfish.utils.EVMUtils;
import crypto.forestfish.utils.FilesUtils;
import crypto.forestfish.utils.SystemUtils;
import crypto.respawned.gasdrop.enums.Action;

public class Start {

	private static final Logger LOGGER = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {
		LOGGER.debug("gasdrop init()");

		Settings settings = null;
		settings = parseCliArgs(args);

		boolean haltOnUnconfirmedTX = false;

		// Wallet setup
		EVMLocalWallet src_wallet = EVMUtils.initializeWallet(settings.getWalletname(), settings.getMnemonic(), settings.getPrivkey());

		// Early exit if WALLETCONFIG actions
		if (settings.getAction() == Action.WALLETCONFIG) {
			LOGGER.info("Action is " + settings.getAction() + " with walletname " + settings.getWalletname());
			LOGGER.info("Exiting ..");
			SystemUtils.halt();
		}

		if (settings.getAction() == Action.FUNDLOOP) {
			LOGGER.info("Action is " + settings.getAction() + " with walletname " + settings.getWalletname());

			EVMBlockChainConnector connector = EVMUtils.getEVMChainConnector(settings.getChain(), true);
			EVMUtils.sanityCheckWithEarlyExit(connector);


			boolean notfinished = false;
			while (!notfinished) {
				EVMUtils.ensureWalletHasFunds(connector, src_wallet, 1.0d);

				List<String> wallets = FilesUtils.readAllLinesFrom(settings.getTopupfile());
				for (String target_wallet: wallets) {

					EVMAccountBalance src_balance = EVMUtils.getAccountNativeBalance(connector, src_wallet.getAddress());
					EVMAccountBalance target_balance = EVMUtils.getAccountNativeBalance(connector, target_wallet);

					BigDecimal src_balanceBD = new BigDecimal(src_balance.getBalanceInETH());
					BigDecimal target_balanceBD = new BigDecimal(target_balance.getBalanceInETH());

					if ((target_balanceBD.compareTo(BigDecimal.valueOf(settings.getMinbalance())) < 0)) {

						System.out.println("---------------------------------------------");
						System.out.println(src_wallet.getAddress() + " src_balanceBD    : " + src_balanceBD);
						System.out.println(target_wallet + " target_balanceBD : " + target_balanceBD);
						System.out.println(" --> will fund " + target_wallet + " with " + settings.getMinbalance() + " " + connector.getChaininfo().getNativeCurrency().getSymbol() + " from " + src_wallet.getAddress());
						System.out.println("---------------------------------------------");

						// Transfer
						System.out.println("-> send from " + src_wallet.getAddress() + " to " + target_wallet + " value: " + settings.getMinbalance() + " " + connector.getChaininfo().getNativeCurrency().getSymbol());
						String txhash1 = EVMUtils.sendTXWithNativeCurrency_LegacyPricingMechanism(connector, src_wallet.getCredentials(), target_wallet, new EVMNativeValue(BigDecimal.valueOf(settings.getMinbalance()), Unit.ETHER), haltOnUnconfirmedTX);
						if (null == txhash1) {
							System.out.println(" * TX attempted but got no tx hash, lets cool down ..");
							SystemUtils.sleepInSeconds(5);
						} else {
							System.out.println(" * TX completed with tx hash: " + txhash1);
						}

					} else {
						System.out.println("OK: " + target_wallet + " target_balanceBD : " + target_balanceBD);
					}

				}
				
				LOGGER.info("Sleeping " + settings.getSleeplen() + "..");
				SystemUtils.sleepInSeconds(settings.getSleeplen());

			}

			LOGGER.info("fin.");
			SystemUtils.halt();

		}
	}

	private static Settings parseCliArgs(String[] args) {

		Settings settings = new Settings();
		Options options = new Options();

		// EVM chain shortname
		Option chainopt = new Option(null, "chain", true, "ETHEREUM, POLYGON, .. see forestfish docs for supported chains");
		options.addOption(chainopt);

		// action
		Option actionOption = new Option(null, "action", true, "Action to perform");
		actionOption.setRequired(true);
		options.addOption(actionOption);

		// walletname
		Option walletnameOption = new Option(null, "walletname", true, "Wallet name to use for specified action");
		walletnameOption.setRequired(true);
		options.addOption(walletnameOption);

		// privkey
		Option privkeyOption = new Option(null, "privkey", true, "Private key for the funding account. Use with --walletname");
		options.addOption(privkeyOption);

		// mnemonic
		Option mnemonicOption = new Option(null, "mnemonic", true, "Mnemonic for the funding account. Use with --walletname");
		options.addOption(mnemonicOption);
		
		// minbalance
		Option minbalanceOption = new Option(null, "minbalance", true, "Minimum balance for target accounts (no decimals included, so 1.0d amounts to 1 ETH)");
		options.addOption(minbalanceOption);
		
		// topupfile
		Option topupfileOption = new Option(null, "topupfile", true, "Path to file with target accounts, one EVM account per line");
		options.addOption(topupfileOption);
		
		// sleeplen
		Option sleeplenOption = new Option(null, "sleeplen", true, "Number of seconds to sleep between account balance checks (defaults to 60 seconds)");
		options.addOption(sleeplenOption);

		HelpFormatter formatter = new HelpFormatter();
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			if (cmd.hasOption("action")) {
				if (cmd.getOptionValue("action").equalsIgnoreCase("WALLETCONFIG")) settings.setAction(Action.WALLETCONFIG);
				if (cmd.getOptionValue("action").equalsIgnoreCase("FUNDLOOP")) settings.setAction(Action.FUNDLOOP);
			}

			if (cmd.hasOption("walletname")) settings.setWalletname(cmd.getOptionValue("walletname"));
			if (cmd.hasOption("topupfile")) settings.setTopupfile(cmd.getOptionValue("topupfile"));

			if (cmd.hasOption("chain")) {
				try {
					String chain_str = cmd.getOptionValue("chain");
					try {
						EVMChain chain = EVMChain.valueOf(chain_str);
						settings.setChain(chain);
					} catch (Exception e) {
						LOGGER.error("Invalid chain specified: " + chain_str);
						SystemUtils.halt();
					}
				} catch (Exception e) {
					LOGGER.error("Unable to parse the mnemonic parameter");
					SystemUtils.halt();
				}
			}

			if (cmd.hasOption("mnemonic")) {
				try {
					String mnemonic = cmd.getOptionValue("mnemonic");
					if (!EVMUtils.isValidMnemonic(mnemonic)) {
						LOGGER.error("Invalid mnemonic specified, will halt");
						SystemUtils.halt();
					}
					settings.setMnemonic(mnemonic);
				} catch (Exception e) {
					LOGGER.error("Unable to parse the mnemonic parameter");
					SystemUtils.halt();
				}
			}

			if (cmd.hasOption("privkey")) {
				try {
					String privkey = cmd.getOptionValue("privkey");
					if (!EVMUtils.isValidPrivateKey(privkey)) {
						LOGGER.error("Invalid private key specified, will halt");
						SystemUtils.halt();
					}
					settings.setPrivkey(privkey);
				} catch (Exception e) {
					LOGGER.error("Unable to parse the privkey parameter");
					SystemUtils.halt();
				}
			}
			
			if (cmd.hasOption("minbalance")) {
				try {
					String minbalance_str = cmd.getOptionValue("minbalance");
					Double minbalance = Double.valueOf(minbalance_str);
					settings.setMinbalance(minbalance);
				} catch (Exception e) {
					LOGGER.error("Unable to parse the minbalance parameter");
					SystemUtils.halt();
				}
			}
			
			if (cmd.hasOption("sleeplen")) {
				try {
					String sleeplen_str = cmd.getOptionValue("sleeplen");
					Integer sleeplen = Integer.valueOf(sleeplen_str);
					settings.setSleeplen(sleeplen);
				} catch (Exception e) {
					LOGGER.error("Unable to parse the sleeplen parameter");
					SystemUtils.halt();
				}
			}

			settings.sanityCheck();

		} catch (ParseException e) {
			LOGGER.error("ParseException: " + e.getMessage());
			formatter.printHelp(" ", options);
			SystemUtils.halt();
		}

		return settings;
	}

}
