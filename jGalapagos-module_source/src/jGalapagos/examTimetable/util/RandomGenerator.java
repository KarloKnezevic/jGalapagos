package jGalapagos.examTimetable.util;

public interface RandomGenerator {
	
	// TODO: dodati metodu za dohvat default randoma

	public int nextInt();

	public int nextInt(int value);

	public double nextDouble();

	public boolean nextBoolean();

	public boolean nextBoolean(double value);

}