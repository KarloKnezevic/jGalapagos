package jGalapagos.examTimetable.util;


import java.util.Arrays;

import ec.util.MersenneTwisterFast;

public final class ArrayUtilities {
	
	private ArrayUtilities() { }
	
	public static <T> void insertInSortedArray(T[] array, T object) {
		int searchResult = Arrays.binarySearch(array, object);
		if (searchResult < 0) {
			searchResult = - searchResult - 1;
		}
		try {
			System.arraycopy(array, searchResult, array, searchResult + 1, array.length - searchResult - 1);
			array[searchResult] = object;
		} catch (IndexOutOfBoundsException ignorable) { }
	}
	
	public static <T> void insertInSortedArray(T[] array, int toIndex, T value) {
		int searchResult = Arrays.binarySearch(array, 0, toIndex, value);
		if (searchResult < 0) {
			searchResult = - searchResult - 1;
		}
		try {
			System.arraycopy(array, searchResult, array, searchResult + 1, toIndex - searchResult);
			array[searchResult] = value;
		} catch (IndexOutOfBoundsException ignorable) { }
	}
	
	public static void insertInSortedArray(int[] array, int toIndex, int value) {
		int searchResult = Arrays.binarySearch(array, 0, toIndex, value);
		if (searchResult < 0) {
			searchResult = - searchResult - 1;
		}
		try {
			System.arraycopy(array, searchResult, array, searchResult + 1, toIndex - searchResult);
			array[searchResult] = value;
		} catch (IndexOutOfBoundsException ignorable) { }
	}
	
	public static <T> void insertInSortedArray(T[] array, T object, int replacePosition) {
		int searchResult = Arrays.binarySearch(array, object);
		if (searchResult < 0) {
			searchResult = - searchResult - 1;
		}
		
		if (searchResult > replacePosition) {
			System.arraycopy(array, replacePosition + 1, array, replacePosition, searchResult - replacePosition - 1);
			array[searchResult - 1] = object;
		} else {
			System.arraycopy(array, searchResult, array, searchResult + 1, replacePosition - searchResult);
			array[searchResult] = object;
		}
	}
	
	public static void swap(int[] array, int a, int b) {
		int temp = array[a];
		array[a] = array[b];
		array[b] = temp;
	}
	
	public static void shuffle(int[] array, MersenneTwisterFast random) {
        for (int i = array.length; --i >= 0;) {
            swap(array, i, random.nextInt(i + 1));
        }
	}
	
	public static void shuffle(int[] array, int toIndex, RandomGenerator random) {
        for (int i = toIndex; --i >= 0;) {
            swap(array, i, random.nextInt(i + 1));
        }
	}
	
	public static <T>String toString(T[] array) {
		final StringBuffer buffer = new StringBuffer();
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			buffer.append(array[i] + "\t");
		}
		return buffer.toString();
	}
	
	public static String toString(float[] array) {
		final StringBuffer buffer = new StringBuffer();
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			buffer.append(array[i] + "\t");
		}
		return buffer.toString();
	}
	
	public static String toString(int[] array) {
		final StringBuffer buffer = new StringBuffer();
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			buffer.append(array[i] + "\t");
		}
		return buffer.toString();
	}
	
	public static String toString(boolean[] array) {
		final StringBuffer buffer = new StringBuffer();
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			buffer.append(array[i] ? "#" : ".");
		}
		return buffer.toString();
	}
	
	
	public static final double[] toArray(double... numbers) {
		return numbers;
	}

}
