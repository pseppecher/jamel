package jamel.austrian.widgets;


import jamel.austrian.roles.AccountHolder;

public class OverdrawAccount extends AbstractAccount {


	/**
	 * Creates a new account.
	 */
	public OverdrawAccount(AccountHolder accountHolder){
		super(accountHolder);
	}


	
	/**
	 * Debits the account.<br>
	 * Only used for clearing purposes.
	 */
	public void debit(int amount) {
		if (amount<=0) throw new RuntimeException("Null or negative debit: "+amount);
		balance-=amount;
	}
	
}




