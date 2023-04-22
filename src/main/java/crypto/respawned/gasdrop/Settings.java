package crypto.respawned.gasdrop;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crypto.forestfish.enums.evm.EVMChain;
import crypto.forestfish.utils.SystemUtils;
import crypto.respawned.gasdrop.enums.Action;


public class Settings {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);

	private EVMChain chain;
	private Action action;
	private String walletname;
	private String mnemonic;
	private String privkey;
	private Double minbalance;
	private String topupfile;
	private int sleeplen = 60;
	
	public Settings() {
		super();
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getWalletname() {
		return walletname;
	}

	public void setWalletname(String walletname) {
		this.walletname = walletname;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public String getPrivkey() {
		return privkey;
	}

	public void setPrivkey(String privkey) {
		this.privkey = privkey;
	}

	public EVMChain getChain() {
		return chain;
	}

	public void setChain(EVMChain chain) {
		this.chain = chain;
	}

	public Double getMinbalance() {
		return minbalance;
	}

	public void setMinbalance(Double minbalance) {
		this.minbalance = minbalance;
	}

	public String getTopupfile() {
		return topupfile;
	}

	public void setTopupfile(String topupfile) {
		this.topupfile = topupfile;
	}

	public int getSleeplen() {
		return sleeplen;
	}

	public void setSleeplen(int sleeplen) {
		this.sleeplen = sleeplen;
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	public void sanityCheck() {
		
		// action
		if (null == this.action) {
			LOGGER.error("Invalid action argument provided");
			SystemUtils.halt();
		}
		
		// walletname
		if (null == this.walletname) {
			LOGGER.error("Invalid walletname argument provided");
			SystemUtils.halt();
		}
		
		if (this.action == Action.FUNDLOOP) {
			
			// chain
			if (null == this.chain) {
				LOGGER.error("Chain argument must be used when using action " + Action.FUNDLOOP.toString());
				SystemUtils.halt();
			}
			
			// minbalance
			if (null == this.minbalance) {
				LOGGER.error("No minbalance argument provided");
				SystemUtils.halt();
			}
			
			// topupfile
			if (null == this.topupfile) {
				LOGGER.error("No topupfile file path provided");
				SystemUtils.halt();
			} else {
				File f = new File(this.topupfile);
				if (!f.exists()) {
					LOGGER.error("Specified topup file does not exist");
					SystemUtils.halt();
				}
			}
			
		}
		
	}
	
}
