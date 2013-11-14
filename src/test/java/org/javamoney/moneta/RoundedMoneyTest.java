/**
 * 
 */
package org.javamoney.moneta;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAdjuster;
import javax.money.MonetaryAmount;
import javax.money.MonetaryQuery;

import org.junit.Test;

/**
 * @author Anatole
 * 
 */
public class RoundedMoneyTest {

	private static final BigDecimal TEN = new BigDecimal(10.0d);
	protected static final CurrencyUnit EURO = MoneyCurrency.of("EUR");
	protected static final CurrencyUnit DOLLAR = MoneyCurrency
			.of("USD");

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#of(javax.money.CurrencyUnit, java.math.BigDecimal)}
	 * .
	 */
	@Test
	public void testOfCurrencyUnitBigDecimal() {
		RoundedMoney m = RoundedMoney.of(MoneyCurrency.of("EUR"), TEN);
		assertEquals(TEN, m.asType(BigDecimal.class));
	}

	@Test
	public void testOfCurrencyUnitDouble() {
		RoundedMoney m = RoundedMoney.of(MoneyCurrency.of("EUR"), 10.0d);
		assertTrue(TEN.doubleValue() == m.doubleValue());
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#getCurrency()}.
	 */
	@Test
	public void testGetCurrency() {
		MonetaryAmount money = RoundedMoney.of(EURO, BigDecimal.TEN);
		assertNotNull(money.getCurrency());
		assertEquals("EUR", money.getCurrency().getCurrencyCode());
	}

	@Test
	public void testSubtractMonetaryAmount() {
		RoundedMoney money1 = RoundedMoney.of(EURO, BigDecimal.TEN);
		RoundedMoney money2 = RoundedMoney.of(EURO, BigDecimal.ONE);
		RoundedMoney moneyResult = money1.subtract(money2);
		assertNotNull(moneyResult);
		assertEquals(9d, moneyResult.doubleValue(), 0d);
	}

	@Test
	public void testDivideAndRemainder_BigDecimal() {
		RoundedMoney money1 = RoundedMoney.of(EURO, BigDecimal.ONE);
		RoundedMoney[] divideAndRemainder = money1.divideAndRemainder(new BigDecimal(
				"0.50000000000000000001"));
		assertThat(divideAndRemainder[0].asType(BigDecimal.class),
				equalTo(BigDecimal.ONE));
		assertThat(divideAndRemainder[1].asType(BigDecimal.class),
				equalTo(new BigDecimal("0.49999999999999999999")));
	}

	@Test
	public void testDivideToIntegralValue_BigDecimal() {
		RoundedMoney money1 = RoundedMoney.of(EURO, BigDecimal.ONE);
		RoundedMoney result = money1.divideToIntegralValue(new BigDecimal(
				"0.50000000000000000001"));
		assertThat(result.asType(BigDecimal.class), equalTo(BigDecimal.ONE));
	}

	@Test
	public void comparePerformance() {
		RoundedMoney money1 = RoundedMoney.of(EURO, BigDecimal.ONE);
		long start = System.currentTimeMillis();
		final int NUM = 1000000;
		for (int i = 0; i < NUM; i++) {
			money1 = money1.add(RoundedMoney.of(EURO, 1234567.3444));
			money1 = money1.subtract(RoundedMoney.of(EURO, 232323));
			money1 = money1.multiply(3.4);
			money1 = money1.divide(5.456);
			// money1 = money1.with(MonetaryRoundings.getRounding());
		}
		long end = System.currentTimeMillis();
		long duration = end - start;
		System.out.println("Duration for 1000000 operations (RoundedMoney/BD): "
				+ duration + " ms (" + ((duration * 1000) / NUM)
				+ " ns per loop) -> "
				+ money1);

		FastMoney money2 = FastMoney.of(EURO, BigDecimal.ONE);
		start = System.currentTimeMillis();
		for (int i = 0; i < NUM; i++) {
			money2 = money2.add(FastMoney.of(EURO, 1234567.3444));
			money2 = money2.subtract(FastMoney.of(EURO, 232323));
			money2 = money2.multiply(3.4);
			money2 = money2.divide(5.456);
			// money2 = money1.with(MonetaryRoundings.getRounding());
		}
		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println("Duration for " + NUM
				+ " operations (IntegralMoney/long): "
				+ duration + " ms (" + ((duration * 1000) / NUM)
				+ " ns per loop) -> "
				+ money2);

		FastMoney money3 = FastMoney.of(EURO, BigDecimal.ONE);
		start = System.currentTimeMillis();
		for (int i = 0; i < NUM; i++) {
			money3 = money3.add(RoundedMoney.of(EURO, 1234567.3444));
			money3 = money3.subtract(FastMoney.of(EURO, 232323));
			money3 = money3.multiply(3.4);
			money3 = money3.divide(5.456);
			// money3 = money3.with(MonetaryRoundings.getRounding());
		}
		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println("Duration for " + NUM
				+ " operations (IntegralMoney/RoundedMoney mixed): "
				+ duration + " ms (" + ((duration * 1000) / NUM)
				+ " ns per loop) -> "
				+ money3);
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		RoundedMoney money1 = RoundedMoney.of(EURO, BigDecimal.ONE);
		RoundedMoney money2 = RoundedMoney.of(EURO, new BigDecimal("1"));
		assertEquals(money1.hashCode(), money2.hashCode());
		RoundedMoney money3 = RoundedMoney.of(DOLLAR, 1.0);
		assertTrue(money1.hashCode() != money3.hashCode());
		assertTrue(money2.hashCode() != money3.hashCode());
		RoundedMoney money4 = RoundedMoney.of(DOLLAR, BigDecimal.ONE);
		assertTrue(money1.hashCode() != money4.hashCode());
		assertTrue(money2.hashCode() != money4.hashCode());
		RoundedMoney money5 = RoundedMoney.of(DOLLAR, BigDecimal.ONE);
		RoundedMoney money6 = RoundedMoney.of(DOLLAR, 1.0);
		assertTrue(money1.hashCode() != money5.hashCode());
		assertTrue(money2.hashCode() != money5.hashCode());
		assertTrue(money1.hashCode() != money6.hashCode());
		assertTrue(money2.hashCode() != money6.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#getDefaultMathContext()}.
	 */
	@Test
	public void testGetDefaultMathContext() {
		RoundedMoney money1 = RoundedMoney.of(EURO, BigDecimal.ONE);
		assertEquals(RoundedMoney.DEFAULT_MATH_CONTEXT, money1.getMathContext());
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#of(javax.money.CurrencyUnit, java.math.BigDecimal, java.math.MathContext)}
	 * .
	 */
	@Test
	public void testOfCurrencyUnitBigDecimalMathContext() {
		RoundedMoney m = RoundedMoney.of(EURO, BigDecimal.valueOf(2.15), new MathContext(2,
				RoundingMode.DOWN));
		RoundedMoney m2 = RoundedMoney.of(EURO, BigDecimal.valueOf(2.1));
		assertEquals(m, m2);
		RoundedMoney m3 = m.multiply(100);
		assertEquals(RoundedMoney.of(EURO, 210), m3.abs());
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#of(javax.money.CurrencyUnit, java.lang.Number)}
	 * .
	 */
	@Test
	public void testOfCurrencyUnitNumber() {
		RoundedMoney m = RoundedMoney.of(EURO, (byte) 2);
		assertNotNull(m);
		assertEquals(EURO, m.getCurrency());
		assertEquals(Byte.valueOf((byte) 2), m.asType(Byte.class));
		m = RoundedMoney.of(DOLLAR, (short) -2);
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Short.valueOf((short) -2), m.asType(Short.class));
		m = RoundedMoney.of(EURO, (int) -12);
		assertNotNull(m);
		assertEquals(EURO, m.getCurrency());
		assertEquals(Integer.valueOf((int) -12), m.asType(Integer.class));
		m = RoundedMoney.of(DOLLAR, (long) 12);
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Long.valueOf((long) 12), m.asType(Long.class));
		m = RoundedMoney.of(EURO, (float) 12.23);
		assertNotNull(m);
		assertEquals(EURO, m.getCurrency());
		assertEquals(Float.valueOf((float) 12.23), m.asType(Float.class));
		m = RoundedMoney.of(DOLLAR, (double) -12.23);
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Double.valueOf((double) -12.23), m.asType(Double.class));
		m = RoundedMoney.of(EURO, (Number) BigDecimal.valueOf(234.2345));
		assertNotNull(m);
		assertEquals(EURO, m.getCurrency());
		assertEquals(BigDecimal.valueOf(234.2345), m.asType(BigDecimal.class));
		m = RoundedMoney.of(DOLLAR, (Number) BigInteger.valueOf(23232312321432432L));
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Long.valueOf(23232312321432432L), m.asType(Long.class));
		assertEquals(BigInteger.valueOf(23232312321432432L),
				m.asType(BigInteger.class));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#of(javax.money.CurrencyUnit, java.lang.Number, java.math.MathContext)}
	 * .
	 */
	@Test
	public void testOfCurrencyUnitNumberMathContext() {
		MathContext mc = new MathContext(2345, RoundingMode.CEILING);
		RoundedMoney m = RoundedMoney.of(EURO, (byte) 2, mc);
		assertNotNull(m);
		assertEquals(mc, m.getMathContext());
		assertEquals(EURO, m.getCurrency());
		assertEquals(Byte.valueOf((byte) 2), m.asType(Byte.class));
		m = RoundedMoney.of(DOLLAR, (short) -2, mc);
		assertNotNull(m);
		assertEquals(mc, m.getMathContext());
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Short.valueOf((short) -2), m.asType(Short.class));
		m = RoundedMoney.of(EURO, (int) -12, mc);
		assertNotNull(m);
		assertEquals(mc, m.getMathContext());
		assertEquals(EURO, m.getCurrency());
		assertEquals(Integer.valueOf((int) -12), m.asType(Integer.class));
		m = RoundedMoney.of(DOLLAR, (long) 12, mc);
		assertEquals(mc, m.getMathContext());
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Long.valueOf((long) 12), m.asType(Long.class));
		m = RoundedMoney.of(EURO, (float) 12.23, mc);
		assertNotNull(m);
		assertEquals(mc, m.getMathContext());
		assertEquals(EURO, m.getCurrency());
		assertEquals(Float.valueOf((float) 12.23), m.asType(Float.class));
		m = RoundedMoney.of(DOLLAR, (double) -12.23, mc);
		assertNotNull(m);
		assertEquals(mc, m.getMathContext());
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(mc, m.getMathContext());
		assertEquals(Double.valueOf((double) -12.23), m.asType(Double.class));
		m = RoundedMoney.of(EURO, (Number) BigDecimal.valueOf(234.2345), mc);
		assertNotNull(m);
		assertEquals(EURO, m.getCurrency());
		assertEquals(mc, m.getMathContext());
		assertEquals(BigDecimal.valueOf(234.2345), m.asType(BigDecimal.class));
		m = RoundedMoney.of(DOLLAR, (Number) BigInteger.valueOf(23232312321432432L),
				mc);
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(mc, m.getMathContext());
		assertEquals(Long.valueOf(23232312321432432L), m.asType(Long.class));
		assertEquals(BigInteger.valueOf(23232312321432432L),
				m.asType(BigInteger.class));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#of(java.lang.String, java.lang.Number)}
	 * .
	 */
	@Test
	public void testOfStringNumber() {
		RoundedMoney m = RoundedMoney.of("EUR", (byte) 2);
		assertNotNull(m);
		assertEquals(EURO, m.getCurrency());
		assertEquals(Byte.valueOf((byte) 2), m.asType(Byte.class));
		m = RoundedMoney.of("USD", (short) -2);
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Short.valueOf((short) -2), m.asType(Short.class));
		m = RoundedMoney.of("EUR", (int) -12);
		assertNotNull(m);
		assertEquals(EURO, m.getCurrency());
		assertEquals(Integer.valueOf((int) -12), m.asType(Integer.class));
		m = RoundedMoney.of("USD", (long) 12);
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Long.valueOf((long) 12), m.asType(Long.class));
		m = RoundedMoney.of("EUR", (float) 12.23);
		assertNotNull(m);
		assertEquals(EURO, m.getCurrency());
		assertEquals(Float.valueOf((float) 12.23), m.asType(Float.class));
		m = RoundedMoney.of("USD", (double) -12.23);
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Double.valueOf((double) -12.23), m.asType(Double.class));
		m = RoundedMoney.of("EUR", (Number) BigDecimal.valueOf(234.2345));
		assertNotNull(m);
		assertEquals(EURO, m.getCurrency());
		assertEquals(BigDecimal.valueOf(234.2345), m.asType(BigDecimal.class));
		m = RoundedMoney.of("USD", (Number) BigInteger.valueOf(23232312321432432L));
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Long.valueOf(23232312321432432L), m.asType(Long.class));
		assertEquals(BigInteger.valueOf(23232312321432432L),
				m.asType(BigInteger.class));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#of(java.lang.String, java.lang.Number, java.math.MathContext)}
	 * .
	 */
	@Test
	public void testOfStringNumberMathContext() {
		MathContext mc = new MathContext(2345, RoundingMode.CEILING);
		RoundedMoney m = RoundedMoney.of("EUR", (byte) 2, mc);
		assertNotNull(m);
		assertEquals(mc, m.getMathContext());
		assertEquals(EURO, m.getCurrency());
		assertEquals(Byte.valueOf((byte) 2), m.asType(Byte.class));
		m = RoundedMoney.of("USD", (short) -2, mc);
		assertNotNull(m);
		assertEquals(mc, m.getMathContext());
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Short.valueOf((short) -2), m.asType(Short.class));
		m = RoundedMoney.of("EUR", (int) -12, mc);
		assertNotNull(m);
		assertEquals(mc, m.getMathContext());
		assertEquals(EURO, m.getCurrency());
		assertEquals(Integer.valueOf((int) -12), m.asType(Integer.class));
		m = RoundedMoney.of("USD", (long) 12, mc);
		assertEquals(mc, m.getMathContext());
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(Long.valueOf((long) 12), m.asType(Long.class));
		m = RoundedMoney.of("EUR", (float) 12.23, mc);
		assertNotNull(m);
		assertEquals(mc, m.getMathContext());
		assertEquals(EURO, m.getCurrency());
		assertEquals(Float.valueOf((float) 12.23), m.asType(Float.class));
		m = RoundedMoney.of("USD", (double) -12.23, mc);
		assertNotNull(m);
		assertEquals(mc, m.getMathContext());
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(mc, m.getMathContext());
		assertEquals(Double.valueOf((double) -12.23), m.asType(Double.class));
		m = RoundedMoney.of("EUR", (Number) BigDecimal.valueOf(234.2345), mc);
		assertNotNull(m);
		assertEquals(EURO, m.getCurrency());
		assertEquals(mc, m.getMathContext());
		assertEquals(BigDecimal.valueOf(234.2345), m.asType(BigDecimal.class));
		m = RoundedMoney
				.of("USD", (Number) BigInteger.valueOf(23232312321432432L), mc);
		assertNotNull(m);
		assertEquals(DOLLAR, m.getCurrency());
		assertEquals(mc, m.getMathContext());
		assertEquals(Long.valueOf(23232312321432432L), m.asType(Long.class));
		assertEquals(BigInteger.valueOf(23232312321432432L),
				m.asType(BigInteger.class));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#ofZero(javax.money.CurrencyUnit)}.
	 */
	@Test
	public void testOfZeroCurrencyUnit() {
		RoundedMoney m = RoundedMoney.ofZero(MoneyCurrency.of("USD"));
		assertNotNull(m);
		assertEquals(MoneyCurrency.of("USD"), m.getCurrency());
		assertEquals(m.doubleValue(), 0d, 0d);
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#ofZero(java.lang.String)}.
	 */
	@Test
	public void testOfZeroString() {
		RoundedMoney m = RoundedMoney.ofZero("CHF");
		assertNotNull(m);
		assertEquals(MoneyCurrency.of("CHF"), m.getCurrency());
		assertEquals(m.doubleValue(), 0d, 0d);
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		RoundedMoney[] moneys = new RoundedMoney[] {
				RoundedMoney.ofZero("CHF"),
				RoundedMoney.of("CHF", BigDecimal.ONE),
				RoundedMoney.of("XXX", BigDecimal.ONE),
				RoundedMoney.of("XXX", BigDecimal.ONE.negate())
		};
		for (int i = 0; i < moneys.length; i++) {
			for (int j = 0; j < moneys.length; j++) {
				if (i == j) {
					assertEquals(moneys[i], moneys[j]);
				}
				else {
					assertNotSame(moneys[i], moneys[j]);
				}
			}
		}
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#compareTo(javax.money.MonetaryAmount)}.
	 */
	@Test
	public void testCompareTo() {
		RoundedMoney m1 = RoundedMoney.of("CHF", -2);
		RoundedMoney m2 = RoundedMoney.of("CHF", 0);
		RoundedMoney m3 = RoundedMoney.of("CHF", -0);
		RoundedMoney m4 = RoundedMoney.of("CHF", 2);
		assertEquals(0, m2.compareTo(m3));
		assertEquals(0, m2.compareTo(m2));
		assertEquals(0, m3.compareTo(m3));
		assertEquals(0, m3.compareTo(m2));
		assertTrue(m1.compareTo(m2) < 0);
		assertTrue(m2.compareTo(m1) > 0);
		assertTrue(m1.compareTo(m3) < 0);
		assertTrue(m2.compareTo(m3) == 0);
		assertTrue(m1.compareTo(m4) < 0);
		assertTrue(m3.compareTo(m4) < 0);
		assertTrue(m4.compareTo(m1) > 0);
		assertTrue(m4.compareTo(m2) > 0);
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#getMathContext()}.
	 */
	@Test
	public void testGetMathContext() {
		RoundedMoney m = RoundedMoney.of("CHF", 10);
		assertEquals(RoundedMoney.DEFAULT_MATH_CONTEXT, m.getMathContext());
		m = RoundedMoney.of("CHF", 10, MathContext.DECIMAL128);
		assertEquals(MathContext.DECIMAL128, m.getMathContext());
	}


	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#abs()}.
	 */
	@Test
	public void testAbs() {
		RoundedMoney m = RoundedMoney.of("CHF", 10);
		assertEquals(m, m.abs());
		assertTrue(m == m.abs());
		m = RoundedMoney.of("CHF", 0);
		assertEquals(m, m.abs());
		assertTrue(m == m.abs());
		m = RoundedMoney.of("CHF", -10);
		assertEquals(m.negate(), m.abs());
		assertTrue(m != m.abs());
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#add(javax.money.MonetaryAmount)} .
	 */
	@Test
	public void testAdd() {
		RoundedMoney money1 = RoundedMoney.of(EURO, BigDecimal.TEN);
		RoundedMoney money2 = RoundedMoney.of(EURO, BigDecimal.ONE);
		RoundedMoney moneyResult = money1.add(money2);
		assertNotNull(moneyResult);
		assertEquals(11d, moneyResult.doubleValue(), 0d);
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#divide(javax.money.MonetaryAmount)}.
	 */
	@Test
	public void testDivideMonetaryAmount() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals(
				RoundedMoney.of("CHF",
						BigDecimal.valueOf(100).divide(BigDecimal.valueOf(5))),
				m.divide(RoundedMoney.of("CHF", 5)));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#divide(java.lang.Number)}.
	 */
	@Test
	public void testDivideNumber() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals(
				RoundedMoney.of("CHF",
						BigDecimal.valueOf(100).divide(BigDecimal.valueOf(5))),
				m.divide(BigDecimal.valueOf(5)));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#divideAndRemainder(javax.money.MonetaryAmount)}
	 * .
	 */
	@Test
	public void testDivideAndRemainderMonetaryAmount() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		RoundedMoney[] res = m.divideAndRemainder(FastMoney.of("CHF", 33));
		assertEquals(RoundedMoney.of("CHF", 3), res[0]);
		assertEquals(RoundedMoney.of("CHF", 1), res[1]);
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#divideAndRemainder(java.lang.Number)}.
	 */
	@Test
	public void testDivideAndRemainderNumber() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals(
				RoundedMoney.of(
						"CHF",
						BigDecimal.valueOf(33)),
				m.divideAndRemainder(
						BigDecimal.valueOf(3))[0]);
		assertEquals(
				RoundedMoney.of(
						"CHF",
						BigDecimal.valueOf(1)),
				m.divideAndRemainder(
						BigDecimal.valueOf(3))[1]);
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#divideToIntegralValue(javax.money.MonetaryAmount)}
	 * .
	 */
	@Test
	public void testDivideToIntegralValueMonetaryAmount() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals(
				RoundedMoney.of(
						"CHF",
						BigDecimal.valueOf(5)),
				m.divideToIntegralValue(RoundedMoney.of(
						"CHF",
						BigDecimal.valueOf(20))));
		assertEquals(
				RoundedMoney.of(
						"CHF",
						BigDecimal.valueOf(33)),
				m.divideToIntegralValue(RoundedMoney.of(
						"CHF",
						BigDecimal.valueOf(3))));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#divideToIntegralValue(java.lang.Number)}
	 * .
	 */
	@Test
	public void testDivideToIntegralValueNumber() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals(
				RoundedMoney.of(
						"CHF",
						BigDecimal.valueOf(5)),
				m.divideToIntegralValue(
						BigDecimal.valueOf(20)));
		assertEquals(
				RoundedMoney.of(
						"CHF",
						BigDecimal.valueOf(33)),
				m.divideToIntegralValue(
						BigDecimal.valueOf(3)));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#multiply(javax.money.MonetaryAmount)}.
	 */
	@Test
	public void testMultiplyMonetaryAmount() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals(RoundedMoney.of("CHF", 400), m.multiply(RoundedMoney.of("CHF", 4)));
		assertEquals(RoundedMoney.of("CHF", 200), m.multiply(RoundedMoney.of("CHF", 2)));
		assertEquals(RoundedMoney.of("CHF", new BigDecimal("50.0")),
				m.multiply(RoundedMoney.of("CHF", 0.5)));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#multiply(java.lang.Number)}.
	 */
	@Test
	public void testMultiplyNumber() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals(RoundedMoney.of("CHF", 400), m.multiply(4));
		assertEquals(RoundedMoney.of("CHF", 200), m.multiply(2));
		assertEquals(RoundedMoney.of("CHF", new BigDecimal("50.0")), m.multiply(0.5));
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#negate()}.
	 */
	@Test
	public void testNegate() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals(RoundedMoney.of("CHF", -100), m.negate());
		m = RoundedMoney.of("CHF", -123.234);
		assertEquals(RoundedMoney.of("CHF", 123.234), m.negate());
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#plus()}.
	 */
	@Test
	public void testPlus() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals(RoundedMoney.of("CHF", 100), m.plus());
		m = RoundedMoney.of("CHF", 123.234);
		assertEquals(RoundedMoney.of("CHF", 123.234), m.plus());
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#subtract(javax.money.MonetaryAmount)}.
	 */
	@Test
	public void testSubtract() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		RoundedMoney s1 = RoundedMoney.of("CHF", 100);
		RoundedMoney s2 = RoundedMoney.of("CHF", 200);
		RoundedMoney s3 = RoundedMoney.of("CHF", 0);
		assertEquals(RoundedMoney.of("CHF", 0), m.subtract(s1));
		assertEquals(RoundedMoney.of("CHF", -100), m.subtract(s2));
		assertEquals(RoundedMoney.of("CHF", 100), m.subtract(s3));
		assertTrue(m == m.subtract(s3));
		m = RoundedMoney.of("CHF", -123.234);
		assertEquals(RoundedMoney.of("CHF", new BigDecimal("-223.234")),
				m.subtract(s1));
		assertEquals(RoundedMoney.of("CHF", new BigDecimal("-323.234")),
				m.subtract(s2));
		assertEquals(RoundedMoney.of("CHF", new BigDecimal("-123.234")),
				m.subtract(s3));
		assertTrue(m == m.subtract(s3));
		m = RoundedMoney.of("CHF", 12.402345534);
		s1 = RoundedMoney.of("CHF", 2343.45);
		s2 = RoundedMoney.of("CHF", 12.402345534);
		s3 = RoundedMoney.of("CHF", -2343.45);
		assertEquals(RoundedMoney.of("CHF", new BigDecimal("12.402345534")
				.subtract(new BigDecimal("2343.45"))), m.subtract(s1));
		assertEquals(RoundedMoney.of("CHF", new BigDecimal("12.402345534")
				.subtract(new BigDecimal("12.402345534"))),
				m.subtract(s2));
		assertEquals(RoundedMoney.of("CHF", 0), m.subtract(s2));
		assertEquals(RoundedMoney.of("CHF", new BigDecimal("2355.852345534")),
				m.subtract(s3));
		assertTrue(m == m.subtract(RoundedMoney.of("CHF", 0)));
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#pow(int)}.
	 */
	@Test
	public void testPow() {
		RoundedMoney m = RoundedMoney.of("CHF", 23.234);
		for (int p = 0; p < 100; p++) {
			assertEquals(RoundedMoney.of("CHF", BigDecimal.valueOf(23.234).pow(p)),
					m.pow(p));
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#ulp()}.
	 */
	@Test
	public void testUlp() {
		RoundedMoney[] moneys = new RoundedMoney[] { RoundedMoney.of("CHF", 100),
				RoundedMoney.of("CHF", 34242344), RoundedMoney.of("CHF", 23123213.435),
				RoundedMoney.of("CHF", 0), RoundedMoney.of("CHF", -100),
				RoundedMoney.of("CHF", -723527.36532) };
		for (RoundedMoney m : moneys) {
			assertEquals("Invalid ulp.",
					m.with(m.asType(BigDecimal.class).ulp()), m.ulp());
		}
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#remainder(java.lang.Number)}.
	 */
	@Test
	public void testRemainderNumber() {
		RoundedMoney[] moneys = new RoundedMoney[] { RoundedMoney.of("CHF", 100),
				RoundedMoney.of("CHF", 34242344), RoundedMoney.of("CHF", 23123213.435),
				RoundedMoney.of("CHF", 0), RoundedMoney.of("CHF", -100),
				RoundedMoney.of("CHF", -723527.36532) };
		for (RoundedMoney m : moneys) {
			assertEquals(
					"Invalid remainder of " + 10.50,
					m.with(m.asType(BigDecimal.class).remainder(
							BigDecimal.valueOf(10.50))),
					m.remainder(10.50));
			assertEquals(
					"Invalid remainder of " + -30.20,
					m.with(m.asType(BigDecimal.class).remainder(
							BigDecimal.valueOf(-30.20))),
					m.remainder(-30.20));
			assertEquals(
					"Invalid remainder of " + -3,
					m.with(m.asType(BigDecimal.class).remainder(
							BigDecimal.valueOf(-3))),
					m.remainder(-3));
			assertEquals(
					"Invalid remainder of " + 3,
					m.with(m.asType(BigDecimal.class).remainder(
							BigDecimal.valueOf(3))),
					m.remainder(3));
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#scaleByPowerOfTen(int)}
	 * .
	 */
	@Test
	public void testScaleByPowerOfTen() {
		RoundedMoney[] moneys = new RoundedMoney[] { RoundedMoney.of("CHF", 100),
				RoundedMoney.of("CHF", 34242344), RoundedMoney.of("CHF", 23123213.435),
				RoundedMoney.of("CHF", 0), RoundedMoney.of("CHF", -100),
				RoundedMoney.of("CHF", -723527.36532) };
		for (RoundedMoney m : moneys) {
			for (int p = -10; p < 10; p++) {
				assertEquals(
						"Invalid scaleByPowerOfTen.",
						m.with(m.asType(BigDecimal.class).scaleByPowerOfTen(p)),
						m.scaleByPowerOfTen(p));
			}
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#isZero()}.
	 */
	@Test
	public void testIsZero() {
		RoundedMoney[] moneys = new RoundedMoney[] { RoundedMoney.of("CHF", 100),
				RoundedMoney.of("CHF", 34242344), RoundedMoney.of("CHF", 23123213.435),
				RoundedMoney.of("CHF", -100),
				RoundedMoney.of("CHF", -723527.36532) };
		for (RoundedMoney m : moneys) {
			assertFalse(m.isZero());
		}
		moneys = new RoundedMoney[] { RoundedMoney.of("CHF", 0),
				RoundedMoney.of("CHF", 0.0), RoundedMoney.of("CHF", BigDecimal.ZERO),
				RoundedMoney.of("CHF", new BigDecimal("0.00000000000000000")) };
		for (RoundedMoney m : moneys) {
			assertTrue(m.isZero());
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#isPositive()}.
	 */
	@Test
	public void testIsPositive() {
		RoundedMoney[] moneys = new RoundedMoney[] { RoundedMoney.of("CHF", 100),
				RoundedMoney.of("CHF", 34242344), RoundedMoney.of("CHF", 23123213.435) };
		for (RoundedMoney m : moneys) {
			assertTrue(m.isPositive());
		}
		moneys = new RoundedMoney[] { RoundedMoney.of("CHF", 0),
				RoundedMoney.of("CHF", 0.0), RoundedMoney.of("CHF", BigDecimal.ZERO),
				RoundedMoney.of("CHF", new BigDecimal("0.00000000000000000")),
				RoundedMoney.of("CHF", -100),
				RoundedMoney.of("CHF", -34242344), RoundedMoney.of("CHF", -23123213.435) };
		for (RoundedMoney m : moneys) {
			assertFalse(m.isPositive());
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#isPositiveOrZero()}.
	 */
	@Test
	public void testIsPositiveOrZero() {
		RoundedMoney[] moneys = new RoundedMoney[] { RoundedMoney.of("CHF", 0),
				RoundedMoney.of("CHF", 0.0), RoundedMoney.of("CHF", BigDecimal.ZERO),
				RoundedMoney.of("CHF", new BigDecimal("0.00000000000000000")),
				RoundedMoney.of("CHF", 100),
				RoundedMoney.of("CHF", 34242344), RoundedMoney.of("CHF", 23123213.435) };
		for (RoundedMoney m : moneys) {
			assertTrue("Invalid positiveOrZero (expected true): " + m,
					m.isPositiveOrZero());
		}
		moneys = new RoundedMoney[] {
				RoundedMoney.of("CHF", -100),
				RoundedMoney.of("CHF", -34242344), RoundedMoney.of("CHF", -23123213.435) };
		for (RoundedMoney m : moneys) {
			assertFalse("Invalid positiveOrZero (expected false): " + m,
					m.isPositiveOrZero());
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#isNegative()}.
	 */
	@Test
	public void testIsNegative() {
		RoundedMoney[] moneys = new RoundedMoney[] { RoundedMoney.of("CHF", 0),
				RoundedMoney.of("CHF", 0.0), RoundedMoney.of("CHF", BigDecimal.ZERO),
				RoundedMoney.of("CHF", new BigDecimal("0.00000000000000000")),
				RoundedMoney.of("CHF", 100),
				RoundedMoney.of("CHF", 34242344), RoundedMoney.of("CHF", 23123213.435) };
		for (RoundedMoney m : moneys) {
			assertFalse("Invalid isNegative (expected false): " + m,
					m.isNegative());
		}
		moneys = new RoundedMoney[] {
				RoundedMoney.of("CHF", -100),
				RoundedMoney.of("CHF", -34242344), RoundedMoney.of("CHF", -23123213.435) };
		for (RoundedMoney m : moneys) {
			assertTrue("Invalid isNegative (expected true): " + m,
					m.isNegative());
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#isNegativeOrZero()}.
	 */
	@Test
	public void testIsNegativeOrZero() {
		RoundedMoney[] moneys = new RoundedMoney[] {
				RoundedMoney.of("CHF", 100),
				RoundedMoney.of("CHF", 34242344), RoundedMoney.of("CHF", 23123213.435) };
		for (RoundedMoney m : moneys) {
			assertFalse("Invalid negativeOrZero (expected false): " + m,
					m.isNegativeOrZero());
		}
		moneys = new RoundedMoney[] { RoundedMoney.of("CHF", 0),
				RoundedMoney.of("CHF", 0.0), RoundedMoney.of("CHF", BigDecimal.ZERO),
				RoundedMoney.of("CHF", new BigDecimal("0.00000000000000000")),
				RoundedMoney.of("CHF", -100),
				RoundedMoney.of("CHF", -34242344), RoundedMoney.of("CHF", -23123213.435) };
		for (RoundedMoney m : moneys) {
			assertTrue("Invalid negativeOrZero (expected true): " + m,
					m.isNegativeOrZero());
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#with(java.lang.Number)}
	 * .
	 */
	@Test
	public void testWithNumber() {
		RoundedMoney[] moneys = new RoundedMoney[] {
				RoundedMoney.of("CHF", 100),
				RoundedMoney.of("CHF", 34242344),
				RoundedMoney.of("CHF", new BigDecimal("23123213.435")),
				RoundedMoney.of("CHF", new BigDecimal("-23123213.435")),
				RoundedMoney.of("CHF", -23123213),
				RoundedMoney.of("CHF", 0) };
		RoundedMoney s = RoundedMoney.of("CHF", 10);
		RoundedMoney[] moneys2 = new RoundedMoney[] {
				s.with(100),
				s.with(34242344), s.with(new BigDecimal("23123213.435")),
				s.with(new BigDecimal("-23123213.435")), s.with(-23123213),
				s.with(0) };
		for (int i = 0; i < moneys.length; i++) {
			assertEquals("with(Number) failed.", moneys[i], moneys2[i]);
		}
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#with(javax.money.CurrencyUnit, java.lang.Number)}
	 * .
	 */
	@Test
	public void testWithCurrencyUnitNumber() {
		RoundedMoney[] moneys = new RoundedMoney[] {
				RoundedMoney.of("CHF", 100),
				RoundedMoney.of("USD", 34242344), RoundedMoney.of("EUR", 23123213.435),
				RoundedMoney.of("USS", -23123213.435), RoundedMoney.of("USN", -23123213),
				RoundedMoney.of("GBP", 0) };
		RoundedMoney s = RoundedMoney.of("XXX", 10);
		RoundedMoney[] moneys2 = new RoundedMoney[] {
				s.with(MoneyCurrency.of("CHF"), 100),
				s.with(MoneyCurrency.of("USD"), 34242344),
				s.with(MoneyCurrency.of("EUR"), new BigDecimal("23123213.435")),
				s.with(MoneyCurrency.of("USS"), new BigDecimal("-23123213.435")),
				s.with(MoneyCurrency.of("USN"), -23123213),
				s.with(MoneyCurrency.of("GBP"), 0) };
		for (int i = 0; i < moneys.length; i++) {
			assertEquals("with(Number) failed.", moneys[i], moneys2[i]);
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#getScale()}.
	 */
	@Test
	public void testGetScale() {
		RoundedMoney[] moneys = new RoundedMoney[] {
				RoundedMoney.of("CHF", 100),
				RoundedMoney.of("USD", 34242344), RoundedMoney.of("EUR", 23123213.435),
				RoundedMoney.of("USS", -23123213.435), RoundedMoney.of("USN", -23123213),
				RoundedMoney.of("GBP", 0) };
		for (RoundedMoney m : moneys) {
			assertEquals("Scale for " + m, m.asType(BigDecimal.class).scale(),
					m.getScale());
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#getPrecision()}.
	 */
	@Test
	public void testGetPrecision() {
		RoundedMoney[] moneys = new RoundedMoney[] {
				RoundedMoney.of("CHF", 100),
				RoundedMoney.of("USD", 34242344), RoundedMoney.of("EUR", 23123213.435),
				RoundedMoney.of("USS", -23123213.435), RoundedMoney.of("USN", -23123213),
				RoundedMoney.of("GBP", 0) };
		for (RoundedMoney m : moneys) {
			assertEquals("Precision for " + m, m.asType(BigDecimal.class)
					.precision(),
					m.getPrecision());
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#longValue()}.
	 */
	@Test
	public void testLongValue() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals("longValue of " + m, 100L, m.longValue());
		m = RoundedMoney.of("CHF", -100);
		assertEquals("longValue of " + m, -100L, m.longValue());
		m = RoundedMoney.of("CHF", -100.3434);
		assertEquals("longValue of " + m, -100L, m.longValue());
		m = RoundedMoney.of("CHF", 100.3434);
		assertEquals("longValue of " + m, 100L, m.longValue());
		m = RoundedMoney.of("CHF", 0);
		assertEquals("longValue of " + m, 0L, m.longValue());
		m = RoundedMoney.of("CHF", -0.0);
		assertEquals("longValue of " + m, 0L, m.longValue());
		m = RoundedMoney.of("CHF", Long.MAX_VALUE);
		assertEquals("longValue of " + m, Long.MAX_VALUE, m.longValue());
		m = RoundedMoney.of("CHF", Long.MIN_VALUE);
		assertEquals("longValue of " + m, Long.MIN_VALUE, m.longValue());
		// try {
		m = RoundedMoney
				.of("CHF",
						new BigDecimal(
								"12121762517652176251725178251872652765321876352187635217835378125"));
		m.longValue();
		// fail("longValue(12121762517652176251725178251872652765321876352187635217835378125) should fail!");
		// } catch (ArithmeticException e) {
		// // OK
		// }
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#longValueExact()}.
	 */
	@Test
	public void testLongValueExact() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals("longValue of " + m, 100L, m.longValueExact());
		m = RoundedMoney.of("CHF", -100);
		assertEquals("longValue of " + m, -100L, m.longValueExact());
		m = RoundedMoney.of("CHF", 0);
		assertEquals("longValue of " + m, 0L, m.longValueExact());
		m = RoundedMoney.of("CHF", -0.0);
		assertEquals("longValue of " + m, 0L, m.longValueExact());
		m = RoundedMoney.of("CHF", Long.MAX_VALUE);
		assertEquals("longValue of " + m, Long.MAX_VALUE, m.longValueExact());
		m = RoundedMoney.of("CHF", Long.MIN_VALUE);
		assertEquals("longValue of " + m, Long.MIN_VALUE, m.longValueExact());
		try {
			m = RoundedMoney
					.of("CHF",
							new BigDecimal(
									"12121762517652176251725178251872652765321876352187635217835378125"));
			m.longValueExact();
			fail("longValueExact(12121762517652176251725178251872652765321876352187635217835378125) should fail!");
		} catch (ArithmeticException e) {
			// OK
		}
		try {
			m = RoundedMoney.of("CHF", -100.3434);
			m.longValueExact();
			fail("longValueExact(-100.3434) should raise an ArithmeticException.");
		} catch (ArithmeticException e) {
			// OK
		}
		try {
			m = RoundedMoney.of("CHF", 100.3434);
			m.longValueExact();
			fail("longValueExact(100.3434) should raise an ArithmeticException.");
		} catch (ArithmeticException e) {
			// OK
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#doubleValue()}.
	 */
	@Test
	public void testDoubleValue() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals("doubleValue of " + m, 100d, m.doubleValue(), 0.0d);
		m = RoundedMoney.of("CHF", -100);
		assertEquals("doubleValue of " + m, -100d, m.doubleValue(), 0.0d);
		m = RoundedMoney.of("CHF", -100.3434);
		assertEquals("doubleValue of " + m, -100.3434, m.doubleValue(), 0.0d);
		m = RoundedMoney.of("CHF", 100.3434);
		assertEquals("doubleValue of " + m, 100.3434, m.doubleValue(), 0.0d);
		m = RoundedMoney.of("CHF", 0);
		assertEquals("doubleValue of " + m, 0d, m.doubleValue(), 0.0d);
		m = RoundedMoney.of("CHF", -0.0);
		assertEquals("doubleValue of " + m, 0d, m.doubleValue(), 0.0d);
		m = RoundedMoney.of("CHF", Double.MAX_VALUE);
		assertEquals("doubleValue of " + m, Double.MAX_VALUE, m.doubleValue(),
				0.0d);
		m = RoundedMoney.of("CHF", Double.MIN_VALUE);
		assertEquals("doubleValue of " + m, Double.MIN_VALUE, m.doubleValue(),
				0.0d);
		// try {
		m = RoundedMoney
				.of("CHF",
						new BigDecimal(
								"12121762517652176251725178251872652765321876352187635217835378125"));
		m.doubleValue();
		// fail("doubleValue(12121762517652176251725178251872652765321876352187635217835378125) should fail!");
		// } catch (ArithmeticException e) {
		// // OK
		// }
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#signum()}.
	 */
	@Test
	public void testSignum() {
		RoundedMoney m = RoundedMoney.of("CHF", 100);
		assertEquals("signum of " + m, 1, m.signum());
		m = RoundedMoney.of("CHF", -100);
		assertEquals("signum of " + m, -1, m.signum());
		m = RoundedMoney.of("CHF", 100.3435);
		assertEquals("signum of " + m, 1, m.signum());
		m = RoundedMoney.of("CHF", -100.3435);
		assertEquals("signum of " + m, -1, m.signum());
		m = RoundedMoney.of("CHF", 0);
		assertEquals("signum of " + m, 0, m.signum());
		m = RoundedMoney.of("CHF", -0);
		assertEquals("signum of " + m, 0, m.signum());
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#toEngineeringString()}.
	 */
	@Test
	public void testToEngineeringString() {
		RoundedMoney[] moneys = new RoundedMoney[] {
				RoundedMoney.of("CHF", 100),
				RoundedMoney.of("USD", 34242344), RoundedMoney.of("EUR", 23123213.435),
				RoundedMoney.of("USS", -23123213.435), RoundedMoney.of("USN", -23123213),
				RoundedMoney.of("GBP", 0) };
		for (RoundedMoney m : moneys) {
			assertEquals("toEngineeringString for " + m, m.getCurrency()
					.getCurrencyCode() + " " + m.asType(BigDecimal.class)
					.toEngineeringString(),
					m.toEngineeringString());
		}
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#toPlainString()}.
	 */
	@Test
	public void testToPlainString() {
		RoundedMoney[] moneys = new RoundedMoney[] {
				RoundedMoney.of("CHF", 100),
				RoundedMoney.of("USD", 34242344), RoundedMoney.of("EUR", 23123213.435),
				RoundedMoney.of("USS", -23123213.435), RoundedMoney.of("USN", -23123213),
				RoundedMoney.of("GBP", 0) };
		for (RoundedMoney m : moneys) {
			assertEquals("toEngineeringString for " + m, m.getCurrency()
					.getCurrencyCode() + " " + m.asType(BigDecimal.class)
					.toPlainString(),
					m.toPlainString());
		}
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#isLessThan(javax.money.MonetaryAmount)}
	 * .
	 */
	@Test
	public void testIsLessThan() {
		assertFalse(RoundedMoney.of("CHF", BigDecimal.valueOf(0d)).isLessThan(
				RoundedMoney.of("CHF", BigDecimal.valueOf(0))));
		assertFalse(RoundedMoney.of("CHF", BigDecimal.valueOf(0.00000000001d))
				.isLessThan(RoundedMoney.of("CHF", BigDecimal.valueOf(0d))));
		assertFalse(RoundedMoney.of("CHF", 15).isLessThan(
				RoundedMoney.of("CHF", 10)));
		assertFalse(RoundedMoney.of("CHF", 15.546).isLessThan(
				RoundedMoney.of("CHF", 10.34)));
		assertTrue(RoundedMoney.of("CHF", 5).isLessThan(
				RoundedMoney.of("CHF", 10)));
		assertTrue(RoundedMoney.of("CHF", 5.546).isLessThan(
				RoundedMoney.of("CHF", 10.34)));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#isLessThanOrEqualTo(javax.money.MonetaryAmount)}
	 * .
	 */
	@Test
	public void testIsLessThanOrEqualTo() {
		assertTrue(RoundedMoney.of("CHF", BigDecimal.valueOf(0d)).isLessThanOrEqualTo(
				RoundedMoney.of("CHF", BigDecimal.valueOf(0))));
		assertFalse(RoundedMoney.of("CHF", BigDecimal.valueOf(0.00000000001d))
				.isLessThanOrEqualTo(RoundedMoney.of("CHF", BigDecimal.valueOf(0d))));
		assertFalse(RoundedMoney.of("CHF", 15).isLessThanOrEqualTo(
				RoundedMoney.of("CHF", 10)));
		assertFalse(RoundedMoney.of("CHF", 15.546).isLessThan(
				RoundedMoney.of("CHF", 10.34)));
		assertTrue(RoundedMoney.of("CHF", 5).isLessThanOrEqualTo(
				RoundedMoney.of("CHF", 10)));
		assertTrue(RoundedMoney.of("CHF", 5.546).isLessThanOrEqualTo(
				RoundedMoney.of("CHF", 10.34)));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#isGreaterThan(javax.money.MonetaryAmount)}
	 * .
	 */
	@Test
	public void testIsGreaterThan() {
		assertFalse(RoundedMoney.of("CHF", BigDecimal.valueOf(0d)).isGreaterThan(
				RoundedMoney.of("CHF", BigDecimal.valueOf(0))));
		assertTrue(RoundedMoney.of("CHF", BigDecimal.valueOf(0.00000000001d))
				.isGreaterThan(RoundedMoney.of("CHF", BigDecimal.valueOf(0d))));
		assertTrue(RoundedMoney.of("CHF", 15).isGreaterThan(
				RoundedMoney.of("CHF", 10)));
		assertTrue(RoundedMoney.of("CHF", 15.546).isGreaterThan(
				RoundedMoney.of("CHF", 10.34)));
		assertFalse(RoundedMoney.of("CHF", 5).isGreaterThan(
				RoundedMoney.of("CHF", 10)));
		assertFalse(RoundedMoney.of("CHF", 5.546).isGreaterThan(
				RoundedMoney.of("CHF", 10.34)));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#isGreaterThanOrEqualTo(javax.money.MonetaryAmount)}
	 * .
	 */
	@Test
	public void testIsGreaterThanOrEqualTo() {
		assertTrue(RoundedMoney.of("CHF", BigDecimal.valueOf(0d))
				.isGreaterThanOrEqualTo(
						RoundedMoney.of("CHF", BigDecimal.valueOf(0))));
		assertTrue(RoundedMoney
				.of("CHF", BigDecimal.valueOf(0.00000000001d))
				.isGreaterThanOrEqualTo(RoundedMoney.of("CHF", BigDecimal.valueOf(0d))));
		assertTrue(RoundedMoney.of("CHF", 15).isGreaterThanOrEqualTo(
				RoundedMoney.of("CHF", 10)));
		assertTrue(RoundedMoney.of("CHF", 15.546).isGreaterThanOrEqualTo(
				RoundedMoney.of("CHF", 10.34)));
		assertFalse(RoundedMoney.of("CHF", 5).isGreaterThanOrEqualTo(
				RoundedMoney.of("CHF", 10)));
		assertFalse(RoundedMoney.of("CHF", 5.546).isGreaterThanOrEqualTo(
				RoundedMoney.of("CHF", 10.34)));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#isEqualTo(javax.money.MonetaryAmount)}.
	 */
	@Test
	public void testIsEqualTo() {
		assertTrue(RoundedMoney.of("CHF", BigDecimal.valueOf(0d)).isEqualTo(
				RoundedMoney.of("CHF", BigDecimal.valueOf(0))));
		assertFalse(RoundedMoney.of("CHF", BigDecimal.valueOf(0.00000000001d))
				.isEqualTo(RoundedMoney.of("CHF", BigDecimal.valueOf(0d))));
		assertTrue(RoundedMoney.of("CHF", BigDecimal.valueOf(5d)).isEqualTo(
				RoundedMoney.of("CHF", BigDecimal.valueOf(5))));
		assertTrue(RoundedMoney.of("CHF", BigDecimal.valueOf(1d)).isEqualTo(
				RoundedMoney.of("CHF", BigDecimal.valueOf(1.00))));
		assertTrue(RoundedMoney.of("CHF", BigDecimal.valueOf(1d)).isEqualTo(
				RoundedMoney.of("CHF", BigDecimal.ONE)));
		assertTrue(RoundedMoney.of("CHF", BigDecimal.valueOf(1)).isEqualTo(
				RoundedMoney.of("CHF", BigDecimal.ONE)));
		assertTrue(RoundedMoney.of("CHF", new BigDecimal("1.0000")).isEqualTo(
				RoundedMoney.of("CHF", new BigDecimal("1.00"))));
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#isNotEqualTo(javax.money.MonetaryAmount)}
	 * .
	 */
	@Test
	public void testIsNotEqualTo() {
		assertFalse(RoundedMoney.of("CHF", BigDecimal.valueOf(0d)).isNotEqualTo(
				RoundedMoney.of("CHF", BigDecimal.valueOf(0))));
		assertTrue(RoundedMoney.of("CHF", BigDecimal.valueOf(0.00000000001d))
				.isNotEqualTo(RoundedMoney.of("CHF", BigDecimal.valueOf(0d))));
		assertFalse(RoundedMoney.of("CHF", BigDecimal.valueOf(5d)).isNotEqualTo(
				RoundedMoney.of("CHF", BigDecimal.valueOf(5))));
		assertFalse(RoundedMoney.of("CHF", BigDecimal.valueOf(1d)).isNotEqualTo(
				RoundedMoney.of("CHF", BigDecimal.valueOf(1.00))));
		assertFalse(RoundedMoney.of("CHF", BigDecimal.valueOf(1d)).isNotEqualTo(
				RoundedMoney.of("CHF", BigDecimal.ONE)));
		assertFalse(RoundedMoney.of("CHF", BigDecimal.valueOf(1)).isNotEqualTo(
				RoundedMoney.of("CHF", BigDecimal.ONE)));
		assertFalse(RoundedMoney.of("CHF", new BigDecimal("1.0000")).isNotEqualTo(
				RoundedMoney.of("CHF", new BigDecimal("1.00"))));
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#getNumberType()}.
	 */
	@Test
	public void testGetNumberType() {
		assertEquals(RoundedMoney.of("CHF", 0).getNumberType(), BigDecimal.class);
		assertEquals(RoundedMoney.of("CHF", 0.34738746d).getNumberType(),
				BigDecimal.class);
		assertEquals(RoundedMoney.of("CHF", 100034L).getNumberType(), BigDecimal.class);
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#query(javax.money.MonetaryQuery)}.
	 */
	@Test
	public void testQuery() {
		MonetaryQuery<Integer> q = new MonetaryQuery<Integer>() {
			@Override
			public Integer queryFrom(MonetaryAmount amount) {
				return RoundedMoney.from(amount).getPrecision();
			}
		};
		RoundedMoney[] moneys = new RoundedMoney[] {
				RoundedMoney.of("CHF", 100),
				RoundedMoney.of("USD", 34242344), RoundedMoney.of("EUR", 23123213.435),
				RoundedMoney.of("USS", -23123213.435), RoundedMoney.of("USN", -23123213),
				RoundedMoney.of("GBP", 0) };
		for (int i = 0; i < moneys.length; i++) {
			assertEquals((Integer) moneys[i].query(q),
					(Integer) moneys[i].getPrecision());
		}
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#asType(java.lang.Class)}.
	 */
	@Test
	public void testAsTypeClassOfT() {
		RoundedMoney m = RoundedMoney.of("CHF", 13.656);
		assertEquals(m.asType(Byte.class), Byte.valueOf((byte) 13));
		assertEquals(m.asType(Short.class), Short.valueOf((short) 13));
		assertEquals(m.asType(Integer.class), Integer.valueOf(13));
		assertEquals(m.asType(Long.class), Long.valueOf(13L));
		assertEquals(m.asType(Float.class), Float.valueOf(13.656f));
		assertEquals(m.asType(Double.class), Double.valueOf(13.656));
		assertEquals(m.asType(BigDecimal.class), BigDecimal.valueOf(13.656));
		assertEquals(m.asType(BigDecimal.class), m.asNumber());
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#asNumber()}.
	 */
	@Test
	public void testAsNumber() {
		assertEquals(BigDecimal.ZERO, RoundedMoney.of("CHF", 0).asNumber());
		assertEquals(BigDecimal.valueOf(100034L), RoundedMoney.of("CHF", 100034L)
				.asNumber());
		assertEquals(new BigDecimal("0.34738746"), RoundedMoney
				.of("CHF", new BigDecimal("0.34738746")).asNumber());
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals("XXX 1.23455645",
				RoundedMoney.of("XXX", new BigDecimal("1.23455645"))
						.toString());
		assertEquals("CHF 1234", RoundedMoney.of("CHF", 1234).toString());
		assertEquals("CHF 1234.0", RoundedMoney.of("CHF", new BigDecimal("1234.0"))
				.toString());
		assertEquals("CHF 1234.1", RoundedMoney.of("CHF", new BigDecimal("1234.1"))
				.toString());
		assertEquals("CHF 0.0100", RoundedMoney.of("CHF", new BigDecimal("0.0100"))
				.toString());
	}

	/**
	 * Test method for {@link org.javamoney.moneta.RoundedMoney#getAmountWhole()}.
	 */
	@Test
	public void testGetAmountWhole() {
		assertEquals(1, RoundedMoney.of("XXX", 1.23455645d).getAmountWhole());
		assertEquals(1, RoundedMoney.of("CHF", 1).getAmountWhole());
		assertEquals(11, RoundedMoney.of("CHF", 11.0d).getAmountWhole());
		assertEquals(1234, RoundedMoney.of("CHF", 1234.1d).getAmountWhole());
		assertEquals(0, RoundedMoney.of("CHF", 0.0100d).getAmountWhole());
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#getAmountFractionNumerator()}.
	 */
	@Test
	public void testGetAmountFractionNumerator() {
		assertEquals(23455645L, RoundedMoney.of("XXX", new BigDecimal("1.23455645"))
				.getAmountFractionNumerator());
		assertEquals(0, RoundedMoney.of("CHF", 1).getAmountFractionNumerator());
		assertEquals(0, RoundedMoney.of("CHF", new BigDecimal("11.0"))
				.getAmountFractionNumerator());
		assertEquals(1L, RoundedMoney.of("CHF", new BigDecimal("1234.1"))
				.getAmountFractionNumerator());
		assertEquals(100L, RoundedMoney.of("CHF", new BigDecimal("0.0100"))
				.getAmountFractionNumerator());
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#getAmountFractionDenominator()}.
	 */
	@Test
	public void testGetAmountFractionDenominator() {
		assertEquals(100000000L, RoundedMoney.of("XXX", new BigDecimal("1.23455645"))
				.getAmountFractionDenominator());
		assertEquals(1, RoundedMoney.of("CHF", 1).getAmountFractionDenominator());
		assertEquals(10, RoundedMoney.of("CHF", new BigDecimal("11.0"))
				.getAmountFractionDenominator());
		assertEquals(10L, RoundedMoney.of("CHF", new BigDecimal("1234.1"))
				.getAmountFractionDenominator());
		assertEquals(10000L, RoundedMoney.of("CHF", new BigDecimal("0.0100"))
				.getAmountFractionDenominator());
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#with(javax.money.MonetaryAdjuster)}.
	 */
	@Test
	public void testWithMonetaryAdjuster() {
		MonetaryAdjuster adj = new MonetaryAdjuster() {
			@Override
			public MonetaryAmount adjustInto(MonetaryAmount amount) {
				return RoundedMoney.of(amount.getCurrency(), -100);
			}
		};
		RoundedMoney m = RoundedMoney.of("XXX", 1.23455645d);
		RoundedMoney a = m.with(adj);
		assertNotNull(a);
		assertNotSame(m, a);
		assertEquals(m.getCurrency(), a.getCurrency());
		assertEquals(RoundedMoney.of(m.getCurrency(), -100), a);
		adj = new MonetaryAdjuster() {
			@Override
			public MonetaryAmount adjustInto(MonetaryAmount amount) {
				return RoundedMoney.from(amount).multiply(2)
						.with(MoneyCurrency.of("CHF"));
			}
		};
		a = m.with(adj);
		assertNotNull(a);
		assertNotSame(m, a);
		assertEquals(MoneyCurrency.of("CHF"), a.getCurrency());
		assertEquals(RoundedMoney.of(a.getCurrency(), 1.23455645d * 2), a);
	}

	/**
	 * Test method for
	 * {@link org.javamoney.moneta.RoundedMoney#from(javax.money.MonetaryAmount)}.
	 */
	@Test
	public void testFrom() {
		RoundedMoney m = RoundedMoney.of("XXX", new BigDecimal("1.2345"));
		RoundedMoney m2 = RoundedMoney.from(m);
		assertTrue(m == m2);
		FastMoney fm = FastMoney.of("XXX", new BigDecimal("1.2345"));
		m2 = RoundedMoney.from(fm);
		assertFalse(m == m2);
		assertEquals(m, m2);
	}

}
