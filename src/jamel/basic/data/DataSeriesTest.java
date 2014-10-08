package jamel.basic.data;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test case for {@link jamel.basic.data.DataSeries}.
 */
public class DataSeriesTest {

	@SuppressWarnings("javadoc")
	@Before
	public void setUp() throws Exception {
	}

	@SuppressWarnings("javadoc")
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Tests.
	 */
	@Test
	public void test() {
		final DataSeries series = new DataSeries(5);
		series.add(10);
		assertEquals("Average", 10, series.getMean(),0);
		series.add(20);
		assertEquals("Average", 15, series.getMean(),0);
		series.add(20);
		assertEquals("Average", 16.6, series.getMean(),0.1);
		series.add(10);
		assertEquals("Average", 15, series.getMean(),0);
		series.add(15);
		assertEquals("Average", 15, series.getMean(),0);
		series.add(15);
		assertEquals("Average", 16, series.getMean(),0);
	}

}
