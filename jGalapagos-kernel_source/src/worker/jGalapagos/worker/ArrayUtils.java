package jGalapagos.worker;

import java.text.NumberFormat;

public class ArrayUtils {
	
	public static void writeIntToByteArray(int value, byte[] byteArray, int offset, int byteCount) {
		for (int i = 0; i < byteCount; i++) {
			byteArray[offset + i] = (byte) (value & 0xff);
			value = value >>> 8;
		}
	}

	public static int readIntFromByteArray(byte[] byteArray, int offset, int byteCount) {
		int number = 0;
		for (int i = 0; i < byteCount; i++) {
			number += toInt(byteArray[offset + i]) << 8 * i;
		}
		return number;
	}

	public static final int toInt(byte b) {
		int i = b;
		return (i < 0) ? 256 + i : i;
	}
	
	public static final String toString(double[] a, NumberFormat numberFormat) {
		if (a == null) {
			return "null";
		}
		int iMax = a.length - 1;
		if (iMax == -1) {
			return "[]";
		}

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			b.append(numberFormat.format(a[i]));
			if (i == iMax) {
				return b.append(']').toString();			
			}
			b.append(", ");
		}
	}

}
