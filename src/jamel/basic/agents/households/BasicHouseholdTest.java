package jamel.basic.agents.households;

import static org.junit.Assert.*;

import java.util.Arrays;

import jamel.basic.agents.roles.Worker;
import jamel.basic.util.JobContract;
import jamel.basic.util.JobOffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class BasicHouseholdTest {

	private static JobOffer createJobOffer(final int wage) {
		return new JobOffer(){
			@Override
			public JobContract apply(Worker worker) {
				return null;
			}
			@Override
			public long getWage() {
				return wage;
			}
			@Override
			public int getVacancies() {
				return 0;
			}			
		};
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Tests the jobComparator.
	 */
	@Test
	public void test1BasicHousehold2() {
		final JobOffer[] jobs  =  new JobOffer[5];
		jobs[0] = createJobOffer(1);
		jobs[1] = createJobOffer(10);
		jobs[2] = createJobOffer(100);
		jobs[3] = createJobOffer(5);
		jobs[4] = createJobOffer(50);
		Arrays.sort(jobs,BasicHousehold.jobComparator);
		assertEquals("Wage",100,jobs[0].getWage());
		assertEquals("Wage",50,jobs[1].getWage());
		assertEquals("Wage",10,jobs[2].getWage());
		assertEquals("Wage",5,jobs[3].getWage());
		assertEquals("Wage",1,jobs[4].getWage());
	}

}

//***
