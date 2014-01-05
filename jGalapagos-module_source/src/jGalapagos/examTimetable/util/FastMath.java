package jGalapagos.examTimetable.util;
/**
 * Methods that are used to perform faster mathematical operations.
 */
public final class FastMath {
	
	private FastMath() { }

	/**
	 * Function that approximates  power function but is much faster.
	 * @param a Base.
	 * @param b	Exponent.
	 * @return	Power approximation.
	 */
	public static double pow(final double a, final double b) {
		final int tmp = (int) (Double.doubleToLongBits(a) >> 32);
		final int tmp2 = (int) (b * (tmp - 1072632447) + 1072632447);
		return Double.longBitsToDouble(((long) tmp2) << 32);
	}
	
	public static long nint(double x) {
		if (x < 0.0)
			return (long) Math.ceil(x - 0.5);
		return (long) Math.floor(x + 0.5);
	}

	public static double logFactorial(int n) {
		double ans = 0.0;
		for (int i = 1; i <= n; i++)
			ans += Math.log(i);
		return ans;
	}

	public static long binomial(int n, int k) {
		return nint(Math.exp(logFactorial(n) - logFactorial(k) - logFactorial(n - k)));
	}
	
}
