package jamel.austrian.widgets;


import jamel.austrian.roles.AccountHolder;

public class RegularAccount extends AbstractAccount {


	/**
	 * Creates a new account.
	 */
	public RegularAccount(AccountHolder accountHolder){
		super(accountHolder);
	}
	
	/**
	 * Debits the account.
	 */
	public void debit(int amount) {
		if (amount<=0) throw new RuntimeException("Null or negative debit: "+amount);
		if (balance < amount) throw new RuntimeException("Not enough money. Account balance: "+balance+" , Debit amount: "+amount);
		balance-=amount;
	}
	

}




