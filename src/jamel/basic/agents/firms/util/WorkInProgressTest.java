package jamel.basic.agents.firms.util;

import static org.junit.Assert.*;
import jamel.basic.agents.util.LaborPower;
import jamel.basic.util.AnachronismException;
import jamel.basic.util.BasicPeriod;
import jamel.basic.util.Commodities;
import jamel.util.Circuit;
import jamel.util.Period;
import jamel.util.Timer;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class WorkInProgressTest {

	@SuppressWarnings("javadoc")
	private static LaborPower createLaborPower(final long wage) {
		return new LaborPower() {
			private float energy = 1;
			private long value = wage;
			@Override
			public void expend() {
				energy=0;
				value=0;
			}
			public void expend(float work) {
				if (work>energy) {
					if ((work-energy)<0.001) {
						expend();
					} else {
						System.out.println("expend:"+work);
						System.out.println("availability:"+energy);
						throw new IllegalArgumentException();
					}
				} else {
					value-=(long) (wage*work);
					energy-=work;
					if (energy<0.001) {
						expend();
					}
				}
			}
			public float getEnergy() {
				return energy;
			}
			public long getWage() {
				return wage;
			}
			public boolean isExhausted() {
				return energy==0;
			}
			@Override
			public long getValue() {
				return value;
			}
		};
	}


	/** The current period. */
	private Period period;

	/** The random. */
	private Random random;

	/** Number of random tests. */
	private int tests = 10000;

	/**
	 * Setup.
	 * @throws Exception exception.
	 */
	@Before
	public void setUp() throws Exception {
		new Circuit(new Timer(){
			@Override public Period getPeriod() {return period;}
			@Override public void next() {}
		},new Random()){
			public Object forward(String message, Object... args) {return null;}
			public String getParameter(String... keys) {return null;
			}
			public boolean isPaused() {
				return false;
			}
			public void run() {				
			}
			@Override
			public String[] getStartingWith(String string) {
				return null;
			}
			@Override
			public String[] getParameterArray(String... keys) {
				return null;
			}
		};
		this.period= new BasicPeriod(0);
	}

	/**
	 * Tear down.
	 * @throws Exception exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Value of inventories.
	 */
	@Test
	public void testWorkInProgress1() {
		for(int j=0;j<tests;j++) {
			final int wage = this.random.nextInt(1000);
			final int productionTime = 1+this.random.nextInt(24);
			final float productivity = 1+this.random.nextFloat()*999f;
			final int workforce = 1+this.random.nextInt(99);
			final int capacity = workforce+this.random.nextInt(100-workforce);
			final WorkInProgress workInProgress = new WorkInProgress(productionTime, capacity, productivity);
			final LaborPower[] laborPowers = new LaborPower[workforce];
			for(int k=0;k<workforce;k++) {
				laborPowers[k]=createLaborPower(wage);
			}
			final Commodities product = workInProgress.process(laborPowers);
			assertEquals("Value", workforce*wage, workInProgress.getValue()+product.getValue(),0);
		}
	}

	/**
	 * Over capacity.
	 */
	@Test(expected=RuntimeException.class)
	public void testWorkInProgress2() {
		final int wage = this.random.nextInt(1000);
		final int productionTime = 1+this.random.nextInt(24);
		final float productivity = 1+this.random.nextFloat()*999f;
		final int capacity = 1+this.random.nextInt(99);
		final int workforce = capacity+1+this.random.nextInt(capacity);
		final WorkInProgress workInProgress = new WorkInProgress(productionTime, capacity, productivity);
		final LaborPower[] laborPowers = new LaborPower[workforce];
		for(int k=0;k<workforce;k++) {
			laborPowers[k]=createLaborPower(wage);
		}
		workInProgress.process(laborPowers);
	}

	/**
	 * Time Inconsistency.
	 */
	@Test(expected=AnachronismException.class)
	public void testWorkInProgress3() {
		final int wage = this.random.nextInt(1000);
		final int productionTime = 1+this.random.nextInt(24);
		final float productivity = 1+this.random.nextFloat()*999f;
		final int workforce = 1+this.random.nextInt(99);
		final int capacity = workforce+this.random.nextInt(100-workforce);
		final WorkInProgress workInProgress = new WorkInProgress(productionTime, capacity, productivity);
		final LaborPower[] laborPowers = new LaborPower[workforce];
		for(int k=0;k<workforce;k++) {
			laborPowers[k]=createLaborPower(wage);
		}
		final Commodities product = workInProgress.process(laborPowers);
		assertEquals("Value", workforce*wage, workInProgress.getValue()+product.getValue(),0);
		// Forgets to increment the time period.
		for(int k=0;k<workforce;k++) {
			laborPowers[k]=createLaborPower(wage);
		}
		// Tries to process again.
		workInProgress.process(laborPowers);
	}

	/**
	 * Successive processes (1).
	 */
	@Test()
	public void testWorkInProgress4() {
		for(int j=0;j<tests;j++) {
			final int wage = this.random.nextInt(1000);
			final int productionTime = 1+this.random.nextInt(24);
			final float productivity = 1+this.random.nextFloat()*999f;
			final int workforce = 1+this.random.nextInt(99);
			final int capacity = workforce+this.random.nextInt(100-workforce);
			final WorkInProgress workInProgress = new WorkInProgress(productionTime, capacity, productivity);
			final LaborPower[] laborPowers = new LaborPower[workforce];
			for(int k=0;k<workforce;k++) {
				laborPowers[k]=createLaborPower(wage);
			}
			final Commodities product1 = workInProgress.process(laborPowers);
			assertEquals("Value", workforce*wage, workInProgress.getValue()+product1.getValue(),0);
			// Increments the time period.
			this.period=this.period.getNext();		
			for(int k=0;k<workforce;k++) {
				laborPowers[k]=createLaborPower(wage);
			}
			// Tries to process again.
			final Commodities product2 = workInProgress.process(laborPowers);
			assertEquals("Value", 2*workforce*wage, workInProgress.getValue()+product1.getValue()+product2.getValue(),0);
		}
	}


	/**
	 * Successive processes (2).
	 */
	@Test()
	public void testWorkInProgress5() {
		for(int j=0;j<tests;j++) {
			final int wage = this.random.nextInt(1000);
			final int duration = 1+this.random.nextInt(11);
			final int productionTime = duration+1+this.random.nextInt(11);
			final float productivity = 1+this.random.nextFloat()*999f;
			final int workforce = 1+this.random.nextInt(99);
			final int capacity = workforce+this.random.nextInt(100-workforce);
			final WorkInProgress workInProgress = new WorkInProgress(productionTime, capacity, productivity);

			for (int t=1; t<duration; t++) {
				final LaborPower[] laborPowers = new LaborPower[workforce];
				for(int k=0;k<workforce;k++) {
					laborPowers[k]=createLaborPower(wage);
				}
				workInProgress.process(laborPowers);
				assertEquals("Value", t*workforce*wage, workInProgress.getValue(),0);
				this.period=this.period.getNext();
			}
		}
	}

	/**
	 * Change in productivity.
	 */
	@Test()
	public void testWorkInProgress6() {
		final int wage = 100;
		final int productionTime = 6;
		float productivity = 100;
		final int capacity = 10;
		final WorkInProgress workInProgress = new WorkInProgress(productionTime, capacity, productivity);
		
		// First round.
		workInProgress.process(createLaborPower(wage));
		assertEquals("Value", wage, workInProgress.getValue(),0);
		assertEquals("Value", wage, workInProgress.getValueAt(1),0);
		assertEquals("Volume", productionTime*productivity, workInProgress.getVolumeAt(1),0);
		assertEquals("Value", 0, workInProgress.getValueAt(2),0);
		assertEquals("Volume", 0, workInProgress.getVolumeAt(2),0);
		
		// Second round.
		this.period=this.period.getNext();
		productivity=150;
		workInProgress.setProductivity(productivity);
		workInProgress.process(createLaborPower(wage));
		assertEquals("Value", 200, workInProgress.getValue(),1);
		assertEquals("Volume", productionTime*100, workInProgress.getVolumeAt(2),0);
		assertEquals("Value", 166, workInProgress.getValueAt(2),0);
		assertEquals("Volume", 300, workInProgress.getVolumeAt(1),1);
		assertEquals("Value", 34, workInProgress.getValueAt(1),0);
		this.period=this.period.getNext();
	}

}

// ***
