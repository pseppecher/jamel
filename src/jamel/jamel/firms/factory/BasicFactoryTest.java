package jamel.jamel.firms.factory;

import static org.junit.Assert.*;
import jamel.basic.data.AgentDataset;
import jamel.basic.util.BasicTimer;
import jamel.basic.util.Timer;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.LaborPower;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class BasicFactoryTest {

	private static LaborPower[] getLaborPowers(int n, final long wage,
			final Timer timer) {
		LaborPower[] laborPowers = new LaborPower[n];
		for (int i = 0; i < n; i++) {
			laborPowers[i] = new LaborPower() {

				private boolean exhausted = false;

				private final int period = timer.getPeriod().intValue();

				private void checkAnachronism() {
					if (timer.getPeriod().intValue() != period) {
						throw new AnachronismException();
					}
				}

				@Override
				public void expend() {
					checkAnachronism();
					this.exhausted = true;
				}

				@Override
				public long getValue() {
					checkAnachronism();
					throw new RuntimeException("Not used");
				}

				@Override
				public long getWage() {
					checkAnachronism();
					return wage;
				}

				@Override
				public boolean isExhausted() {
					checkAnachronism();
					return this.exhausted;
				}

			};
		}
		return laborPowers;
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Are we able to sort machines according to their productivity in
	 * descending order ?
	 */
	@Test
	public void test1() {
		final Timer timer = new BasicTimer(0);
		final Random random = new Random();
		final List<Machine> machinery = new LinkedList<Machine>();
		machinery.add(new BasicMachine(1, 10, 0, timer, random));
		machinery.add(new BasicMachine(1, 5, 0, timer, random));
		machinery.add(new BasicMachine(1, 100, 0, timer, random));
		machinery.add(new BasicMachine(1, 9, 0, timer, random));
		machinery.add(new BasicMachine(1, 2, 0, timer, random));
		machinery.add(new BasicMachine(1, 50, 0, timer, random));
		Collections.sort(machinery, BasicFactory.productivityComparator);
		assertEquals("machinery.get(0).getProductivity()", 100, machinery
				.get(0).getProductivity());
		assertEquals("machinery.get(1).getProductivity()", 50, machinery.get(1)
				.getProductivity());
		assertEquals("machinery.get(2).getProductivity()", 10, machinery.get(2)
				.getProductivity());
		assertEquals("machinery.get(3).getProductivity()", 9, machinery.get(3)
				.getProductivity());
		assertEquals("machinery.get(4).getProductivity()", 5, machinery.get(4)
				.getProductivity());
		assertEquals("machinery.get(5).getProductivity()", 2, machinery.get(5)
				.getProductivity());
	}

	@Test
	public void test2() {
		final Timer timer = new BasicTimer(0);
		final Random random = new Random();
		final Factory factory = new BasicFactory(6, 10, 100, timer, random);
		LaborPower[] laborPowers = getLaborPowers(5, 7, timer);
		factory.open();
		factory.process(laborPowers);
		factory.close();
		final AgentDataset data = factory.getData();
		String key;
		key = "capacity";
		assertEquals(key, 10, data.get(key), 0);
		key = "production.vol";
		assertEquals(key, 0, data.get(key), 0);
		key = "production.val";
		assertEquals(key, 0, data.get(key), 0);
		key = "inventories.fg.val";
		assertEquals(key, 0, data.get(key), 0);
		key = "inventories.fg.vol";
		assertEquals(key, 0, data.get(key), 0);
		key = "inventories.inProcess.val";
		assertEquals(key, 35, data.get(key), 0);
	}

	@Test
	public void test3() {
		final BasicTimer timer = new BasicTimer(0);
		final Random random = new Random();
		final Factory factory = new BasicFactory(6, 10, 100, timer, random);

		for (int i = 0; i < 6; i++) {
			timer.next();
			final LaborPower[] laborPowers = getLaborPowers(5, 7, timer);
			factory.open();
			factory.process(laborPowers);
			factory.close();
		}

		final AgentDataset data = factory.getData();
		String key;
		key = "capacity";
		assertEquals(key, 10, data.get(key), 0);
		key = "production.vol";
		assertEquals(key, 600, data.get(key), 0);
		key = "production.val";
		assertEquals(key, 42, data.get(key), 0);
		key = "inventories.fg.vol";
		assertEquals(key, 600, data.get(key), 0);
		key = "inventories.fg.val";
		assertEquals(key, 42, data.get(key), 0);
		key = "inventories.inProcess.val";
		assertEquals(key, 168, data.get(key), 0);
	}

	@Test
	public void test4() {
		final BasicTimer timer = new BasicTimer(0);
		final Random random = new Random();
		final Factory factory = new BasicFactory(6, 10, 100, timer, random);

		for (int i = 0; i < 100; i++) {
			factory.open();
			final int capacity=factory.getCapacity();
			final LaborPower[] laborPowers = getLaborPowers(capacity, 10, timer);
			factory.process(laborPowers);
			factory.close();
			final AgentDataset data = factory.getData();
			System.out.println(data.get("production.vol")+", "+data.get("production.vol.atFullCapacity"));
			timer.next();
		}
	}

}

// ***
