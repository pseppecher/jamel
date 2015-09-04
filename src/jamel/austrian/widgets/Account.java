package jamel.austrian.widgets;


import jamel.austrian.roles.AccountHolder;

public class Account {


	/**
	 * A class for cheques.<br>
	 * A cheque is transferable note which constitutes a claim to the deposits of the issuing account.<br>
	 * Cheques are circulated by banks. Before circulating a cheque the banks verify the liquidity of the issuer.
	 * Hence, all circulating cheques are covered.  
	 */
	private class aCheque extends Cheque{
		

		/**
		 * Creates a new regular cheque.
		 */
		private aCheque(int amount) {
			super(amount);
			deposit.debit(amount);
		}

		public AccountHolder getDrawer() {
			return Account.this.getHolder();
		}
		
	}


	/**
	 * Creates a new account.
	 */
	public Account(AccountHolder accountHolder){
		this.accountHolder = accountHolder;
		this.deposit = new Deposit();
	}


	/** The account holder. */
	private final AccountHolder accountHolder;

	/** The deposit. */
	private final Deposit deposit;



	/** Deposits cheque. */
	public void deposit(Cheque cheque) {
		deposit.credit(cheque.getAmount());	
	}
	

	/**
	 * Returns a new cheque from this account.<br>
	 * The new cheque constitutes a claim to the deposits of the account which calls the method.
	 */
	public Cheque newCheque(int amount) {
		return new aCheque(amount);
	}


	/**
	 * Returns the available balance of the account.
	 */
	public int getAmount(){
		return deposit.getAmount();
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
		deposit.credit(amount);
	}
	
	
	/**
	 * Debits the account.<br>
	 * Only used for clearing purposes.
	 */
	public void debit(int amount) {
		deposit.debit(amount);
	}
	
	
	
	/**
	 * Resets the account.<br>
	 * So far only used for clearing purposes.
	 */
	public void reset() {
		deposit.reset();
	}
	
}




