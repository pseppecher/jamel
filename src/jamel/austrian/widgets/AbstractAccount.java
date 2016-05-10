package jamel.austrian.widgets;


import jamel.austrian.banks.CommercialBank;
import jamel.austrian.roles.AccountHolder;

public abstract class AbstractAccount {

	
	//TODO: revise description
	/**
	 * A class for cheques.<br>
	 * A cheque is transferable note which constitutes a claim to the deposits of the issuing account.<br>
	 * Cheques are circulated by banks. Before circulating a cheque the banks verify the liquidity of the issuer.
	 * Hence, all circulating cheques are covered.  
	 */
	private class aCheque extends AbstractCheque{
		

		/**
		 * Creates a new regular cheque. 
		 * Debits the account from which the cheque is drawn.
		 */
		private aCheque(int amount, CommercialBank issuingBank) {
			super(amount, issuingBank);
			debit(amount);
		}

		public AccountHolder getIssuer() {
			return AbstractAccount.this.getHolder();
		}
		
	}


	/**
	 * Creates a new account.
	 */
	public AbstractAccount(AccountHolder accountHolder){
		this.accountHolder = accountHolder;
		this.balance = 0;
	}


	/** The account holder. */
	private final AccountHolder accountHolder;
	

	/** The account balance. */
	protected int balance;


	/** Deposits cheque. */
	public void deposit(AbstractCheque cheque) {
		credit(cheque.getAmount());	
	}
	

	/**
	 * Returns a new cheque from this account.<br>
	 * The new cheque constitutes a claim to the deposits of the account which calls the method.
	 */
	public AbstractCheque newCheque(int amount, CommercialBank issuingBank) {
		return new aCheque(amount, issuingBank);
	}


	/**
	 * Returns the available balance of the account.
	 */
	public int getBalance(){
		return balance;
	}


	/**
	 * Returns the holder of the account.
	 */
	public AccountHolder getHolder(){
		return accountHolder;
	}

	
	/**
	 * Credits the account.<br>
	 * Only used for clearing purposes.
	 */
	public void credit(int amount) {
		if (amount<0) throw new RuntimeException("Negative credit amount.");
		balance += amount ;
	}
	
	
	/**
	 * Debits the account
	 */
	public abstract void debit(int amount); 
	
	
	/**
	 * Resets the account.<br>
	 * So far only used for clearing purposes.
	 */
	public void reset() {
		balance = 0;
	}
	
}




