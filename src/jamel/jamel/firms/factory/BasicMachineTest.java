package jamel.jamel.firms.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jamel.basic.util.BasicTimer;
import jamel.basic.util.Timer;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.LaborPower;

import java.util.List;
import java.util.Random;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class BasicMachineTest {

	private static LaborPower getNewLabourPower(final long wage,
			final Timer timer) {

		return new LaborPower() {

			private final int creation = timer.getPeriod().intValue();

			private boolean isExhausted = false;

			@Override
			public void expend() {
				if (this.isExhausted) {
					throw new RuntimeException("Exhausted.");
				}
				if (timer.getPeriod().intValue() != creation) {
					throw new AnachronismException();
				}
				this.isExhausted = true;
			}

			@Override
			public long getValue() {
				throw new RuntimeException("Not used.");
			}

			@Override
			public long getWage() {
				return wage;
			}

			@Override
			public boolean isExhausted() {
				return this.isExhausted;
			}

		};
	}

	@Test
	public void testWork1() {
		final BasicTimer timer = new BasicTimer(0);
		final Random random = new Random();
		final Machine machine = new BasicMachine(5, 100, 0, timer, random);
		final LaborPower laborPower = getNewLabourPower(100, timer);
		final List<Materials> outputs = machine.work(laborPower);
		assertEquals("outputs.size()", 1, outputs.size());
		final Materials stuff = outputs.get(0);
		assertEquals("stuff.getBookValue()", 100, (long) stuff.getBookValue());
		assertEquals("stuff.getVolume()", 500, stuff.getVolume());
		assertEquals("stuff.getCompletion()", 0.2, stuff.getCompletion()
				.doubleValue(), 0);
		timer.next();
		final LaborPower laborPower2 = getNewLabourPower(100, timer);
		final List<Materials> outputs2 = machine.work(laborPower2, stuff);
		assertEquals("outputs2.size()", 1, outputs2.size());
		final Materials stuff2 = outputs2.get(0);
		assertEquals("stuff.getVolume()", 0, stuff.getVolume());
		assertEquals("stuff.getCompletion()", 0.2, stuff.getCompletion()
				.doubleValue(), 0);
		assertEquals("stuff.getBookValue()", 0, (long) stuff.getBookValue());
		assertEquals("stuff2.getVolume()", 500, stuff2.getVolume());
		assertEquals("stuff2.getCompletion()", 0.4, stuff2.getCompletion()
				.doubleValue(), 0);
		assertEquals("stuff2.getBookValue()", 200, (long) stuff2.getBookValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWork2() {
		final Timer timer = new BasicTimer(0);
		final Random random = new Random();
		final Machine machine = new BasicMachine(6, 100, 0, timer, random);
		final LaborPower laborPower = getNewLabourPower(100, timer);
		machine.work(laborPower);
		assertTrue(laborPower.isExhausted());
		// As the labor power is now exhausted, an IllegalArgumentException
		// should be thrown.
		machine.work(laborPower);
	}

	@Test(expected = AnachronismException.class)
	public void testWork3() {
		final Timer timer = new BasicTimer(0);
		final Random random = new Random();
		final Machine machine = new BasicMachine(6, 100, 0, timer, random);
		final LaborPower laborPower = getNewLabourPower(100, timer);
		final LaborPower laborPower2 = getNewLabourPower(100, timer);
		machine.work(laborPower);
		// As the machine has already been used in this period, an
		// AnachronismException should be thrown.
		machine.work(laborPower2);
	}

	@Test
	public void testWork4() {
		final BasicTimer timer = new BasicTimer(0);
		final Random random = new Random();
		final Machine machine = new BasicMachine(5, 100, 0, timer, random);
		final LaborPower laborPower1 = getNewLabourPower(100, timer);
		final List<Materials> outputs1 = machine.work(laborPower1);
		final Materials stuff1 = outputs1.get(0);
		timer.next();
		final LaborPower laborPower2 = getNewLabourPower(100, timer);
		final List<Materials> outputs2 = machine.work(laborPower2);
		final Materials stuff2 = outputs2.get(0);
		stuff1.add(stuff2);

		assertEquals("stuff1.getVolume()", 1000, stuff1.getVolume());
		assertEquals("stuff1.getCompletion()", 0.2, stuff1.getCompletion()
				.doubleValue(), 0);
		assertEquals("stuff1.getBookValue()", 200, (long) stuff1.getBookValue());
		assertEquals("stuff2.getVolume()", 0, stuff2.getVolume());
		assertEquals("stuff2.getCompletion()", 0.2, stuff2.getCompletion()
				.doubleValue(), 0);
		assertEquals("stuff2.getBookValue()", 0, (long) stuff2.getBookValue());

		timer.next();

		final LaborPower laborPower3 = getNewLabourPower(100, timer);
		final List<Materials> outputs3 = machine.work(laborPower3, stuff1);
		final Materials stuff3 = outputs3.get(0);

		assertEquals("stuff1.getVolume()", 500, stuff1.getVolume());
		assertEquals("stuff1.getCompletion()", 0.2, stuff1.getCompletion()
				.doubleValue(), 0);
		assertEquals("stuff1.getBookValue()", 100, (long) stuff1.getBookValue());
		assertEquals("stuff3.getVolume()", 500, stuff3.getVolume());
		assertEquals("stuff3.getCompletion()", 0.4, stuff3.getCompletion()
				.doubleValue(), 0);
		assertEquals("stuff3.getBookValue()", 200, (long) stuff3.getBookValue());

	}

}

// ***
