package jamel.models.m18.r08.util;

import java.util.List;

import jamel.models.m18.r08.roles.Shareholder;

public interface Ownership {

	List<? extends Equity> getEquities();

	boolean isEmpty();

	Equity issue(Shareholder agent, long value);

	long size();

	void updateValue(long value);

	long getTotalValue();

}
