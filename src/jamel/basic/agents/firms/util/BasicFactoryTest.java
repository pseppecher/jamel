package jamel.basic.agents.firms.util;

import static org.junit.Assert.*;

import java.awt.Component;
import java.util.Random;

import jamel.basic.agents.util.LaborPower;
import jamel.basic.util.BasicPeriod;
import jamel.util.Circuit;
import jamel.util.Period;
import jamel.util.Timer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test case for {@link jamel.basic.agents.firms.util.BasicFactory}.
 */
public class BasicFactoryTest {

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
					value-=wage*work;
					energy-=work;
					if (energy<0.001) {
						expend();
					}
				}
			}
			public float getEnergy() {
				return energy;
			}
			@Override
			public long getValue() {
				return value;
			}
			public long getWage() {
				return wage;
			}
			public boolean isExhausted() {
				return energy==0;
			}
		};
	}

	/** capacity */
	private int capacity;

	/** The current period. */
	private Period period;

	/** productionTime */
	private int productionTime;

	/** productivity */
	private float productivity;

	/** The random. */
	private Random random;

	/** Number of random tests. */
	private int tests = 10000;

	/** wage */
	private int wage;

	/** workforce */
	private int workforce;

	/**
	 * Randomizes the parameters.
	 */
	private void randomize() {
		this.wage = this.random.nextInt(1000);
		this.productionTime = 2+this.random.nextInt(22);
		this.productivity = 100+this.random.nextFloat()*900f;
		this.workforce = 1+this.random.nextInt(99);
		this.capacity = workforce+this.random.nextInt(100-workforce);
	}

	/**
	 * Setup.
	 * @throws Exception exception.
	 */
	@Before
	public void setUp() throws Exception {
		new Circuit(new Timer() {
			@Override public Period getPeriod() {return period;}
			@Override public void next() {}
			@Override public Component getCounter() {return null;}
		},new Random()){
			@Override public Object forward(String message, Object... args) {return null;}
			@Override public String getParameter(String... keys) {return null;}
			@Override public boolean isPaused() {return false;}
			@Override public void run() {}
			@Override public String[] getStartingWith(String string) {return null;}
			@Override public String[] getParameterArray(String... keys) {return null;}
		};
		this.period= new BasicPeriod(0);
		this.random = new Random();
	}

	/**
	 * Tear down.
	 * @throws Exception exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Expense of several units of labor power.
	 */
	@Test
	public void test1() {
		for(int j=0;j<tests;j++) {
			this.randomize();
			final BasicFactory basicFactory = new BasicFactory(productionTime,capacity,productivity);
			final LaborPower[] list = new LaborPower[workforce];
			for(int k=0;k<workforce;k++) {
				list[k]=createLaborPower(wage);
			}			
			basicFactory.process(list);
			assertEquals("Test: "+j+": Value", wage*workforce, basicFactory.getValue(), 0);
			assertEquals("Test: "+j+": Value", wage*workforce, basicFactory.getValueAt(1), 0);
			assertEquals("Test: "+j+": Volume", (long) (productionTime*productivity*workforce), basicFactory.getVolumeAt(1), 0.005*(long) (productionTime*productivity*workforce));
		}
	}

	/**
	 * Over capacity.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void test2() {
		final BasicFactory basicFactory = new BasicFactory(6,10,10);
		final int workforce = 20; // The workforce exceeds the capacity.
		final LaborPower[] list = new LaborPower[workforce];
		for(int k=0;k<workforce;k++) {
			list[k]=createLaborPower(100);
		}			
		basicFactory.process(list);
	}

	/**
	 * Time inconsistency.
	 */
	@Test(expected=RuntimeException.class)
	public void test5() {
		final BasicFactory basicFactory = new BasicFactory(6,10,10);
		basicFactory.process(createLaborPower(100));
		basicFactory.process(createLaborPower(100));
	}

	/**
	 * Successive expense of several units of labor power.
	 */
	@Test
	public void test6() {
		for(int j=0;j<tests;j++) {
			this.randomize();
			productionTime = 4+this.random.nextInt(20);
			capacity = 2+this.random.nextInt(98);
			final BasicFactory basicFactory = new BasicFactory(productionTime,capacity,productivity);
			basicFactory.process(createLaborPower(wage));
			assertEquals("Test: "+j+": Value", wage, basicFactory.getValue(), 0);
			assertEquals("Test: "+j+": Value", wage, basicFactory.getValueAt(1), 0);
			assertEquals("Test: "+j+": Volume", (long) (productionTime*productivity), basicFactory.getVolumeAt(1), 0);
			assertEquals("Test: "+j+": Value", 0, basicFactory.getValueAt(2), 0);
			assertEquals("Test: "+j+": Volume", 0, basicFactory.getVolumeAt(2), 0);
			this.period=this.period.getNext(); // Next period.
			basicFactory.process(createLaborPower(wage));
			assertEquals("Test: "+j+": Value", 2*wage, basicFactory.getValue(), 0);
			assertEquals("Test: "+j+": Value", 0, basicFactory.getValueAt(1), 0);
			assertEquals("Test: "+j+": Volume", 0, basicFactory.getVolumeAt(1), 0);
			assertEquals("Test: "+j+": Value", 2*wage, basicFactory.getValueAt(2), 0);
			assertEquals("Test: "+j+": Volume", (long) (productionTime*productivity), basicFactory.getVolumeAt(2), 0);
			this.period=this.period.getNext(); // Next period.
			basicFactory.process(createLaborPower(wage),createLaborPower(wage));
			assertEquals("Test: "+j+": Value", 4*wage, basicFactory.getValue(), 0);
			assertEquals("Test: "+j+": Value", wage, basicFactory.getValueAt(1), 0);
			assertEquals("Test: "+j+": Volume", (long) (productionTime*productivity), basicFactory.getVolumeAt(1), 0);
			assertEquals("Test: "+j+": Value", 0, basicFactory.getValueAt(2), 0);
			assertEquals("Test: "+j+": Volume", 0, basicFactory.getVolumeAt(2), 0);
			assertEquals("Test: "+j+": Value", 3*wage, basicFactory.getValueAt(3), 0);
			assertEquals("Test: "+j+": Volume", (long) (productionTime*productivity), basicFactory.getVolumeAt(3), 0);
		}
	}	

	/**
	 * Change in productivity.
	 */
	@Test
	public void test7() {
		for(int j=0;j<tests;j++) {
			randomize();
			productionTime = 3+this.random.nextInt(21);
			final BasicFactory basicFactory = new BasicFactory(productionTime,capacity,productivity);
			basicFactory.process(createLaborPower(wage));
			assertEquals("Test: "+j+": Value", wage, basicFactory.getValue(), 0);
			assertEquals("Test: "+j+": Value", wage, basicFactory.getValueAt(1), 0);
			assertEquals("Test: "+j+": Volume", (long) (productivity*productionTime), basicFactory.getVolumeAt(1), 0);
			assertEquals("Test: "+j+": Value", 0, basicFactory.getValueAt(2), 0);
			assertEquals("Test: "+j+": Volume", 0, basicFactory.getVolumeAt(2), 0);
			this.period=this.period.getNext(); // Next period.
			final float delta = productivity*(0.1f+this.random.nextFloat()*0.9f);
			basicFactory.setProductivity(productivity+delta);
			basicFactory.process(createLaborPower(wage));
			assertEquals("Test: "+j+": Value", 2*wage, basicFactory.getValue(), 1);
			assertEquals("Test: "+j+": Value", (long) (wage*delta/(productivity+delta)), basicFactory.getValueAt(1), 4);
			assertEquals("Test: "+j+": Volume", (long) (delta*productionTime), basicFactory.getVolumeAt(1), 1);
			assertEquals("Test: "+j+": Value", (long) (wage*(2-delta/(productivity+delta))), basicFactory.getValueAt(2), 4);
			assertEquals("Test: "+j+": Volume", (long) (productivity*productionTime), basicFactory.getVolumeAt(2), 0);
		}
	}

	/**
	 * Tests the ex-post productivity.
	 */
	@Test
	public void test8() {
		for(int l=0;l<tests;l++) {
			randomize();
			/*final int wage = this.random.nextInt(1000);
			final int productionTime = 1+this.random.nextInt(24);
			final float productivity = 100*this.random.nextFloat()*900f;
			final int workforce = 1+this.random.nextInt(99);
			final int capacity = workforce+this.random.nextInt(100-workforce);*/
			final BasicFactory basicFactory = new BasicFactory(productionTime,capacity,productivity);
			for(int k=0;k<10;k++) {
				for(int j=0;j<productionTime;j++) {
					final LaborPower[] list = new LaborPower[workforce];
					for(int i=0;i<workforce;i++) {
						list[i]=createLaborPower(wage);
					}			
					basicFactory.process(list);
					this.period=this.period.getNext(); // Next period.
				}
				assertEquals("Test: "+l+": Ex-post productivity", productivity, basicFactory.getAverageProductivity(), 0.01*productivity);
			}
		}
	}

}

// ***
