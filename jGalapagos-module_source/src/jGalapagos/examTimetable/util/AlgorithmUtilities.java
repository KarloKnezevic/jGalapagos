package jGalapagos.examTimetable.util;


import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.model.Course;
import jGalapagos.examTimetable.model.JoinedCourse;
import jGalapagos.examTimetable.model.Term;
import jGalapagos.examTimetable.model.VariableTimetable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class AlgorithmUtilities {
	
	private static final Log log = LogFactory.getLog(AlgorithmUtilities.class);
	private static final String dateFormatString = "yyyy-MM-dd\tHH:mm";
	
	private AlgorithmUtilities() { }

//	/**
//	 * Provjerava jesu li prekoračena čvrsta ograničenja za cijeli raspored.
//	 * Koristiti samo u razvoju!
//	 * 
//	 * @param timetable
//	 *            Raspored koji se provjerava
//	 * @param constantData
//	 *            Opis problema.
//	 * @return
//	 */
//	public static boolean hasHardConstraints(final Timetable timetable, final ConstantData constantData) {
//		int courseCount = constantData.getCourseCount();
//		for (int i = 0; i < courseCount; i++) {
//			if (timetable.getTermIndex(i) < 0) {
//				return true;
//			}
//		}
//		for (int day = 0; day < constantData.getDayCount(); day++) {
//			int[] terms = constantData.getTermsForDay(day);
//			if (terms == null) {
//				continue;
//			}
//			for (int term = 0; term < terms.length; term++) {
//				int studentCount = 0;
//				boolean courseAloneInTerm = true;
//				for (int course = 0; course < courseCount; course++) {
//					if (term == timetable.getTermIndex(course)) {
//						if (studentCount > 0) {
//							courseAloneInTerm = false;
//						}
//						studentCount += constantData.getStudentCount(course);
//						for (int course2 = course + 1; course2 < courseCount; course2++) {
//							if (term == timetable.getTermIndex(course2)) {
//								if (constantData.getSameStudentsCount(course, course2) > 0) {
//									return true;
//								}
//							}
//						}
//					}
//				}
//				
//				if (studentCount > (courseAloneInTerm ? constantData.getTerm(term).getHardCapacity() : constantData.getTerm(term).getSoftCapacity())) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}

//	/**
//	 * Provjerava može li se predmet <code>course</code> premjestiti u termin
//	 * <code>term</code> kako i dalje čvrsta ograničenja ne bi bila prekoračena.
//	 * 
//	 * @param course
//	 *            Predmet koji se provjerava.
//	 * @param term
//	 *            Termin u koji bi se predmet trebao premjestiti.
//	 * @param timetable
//	 *            Raspored u kojem se provjerava. Čvrsta ograničenja moraju biti
//	 *            zadovoljena! Ukoliko čvrsta ograničenja nisu zadovoljena,
//	 *            poziv ove metode NEĆE javiti da čvrsta ograničenja nisu
//	 *            zadovoljena!
//	 * @param constantData
//	 *            Opis problema.
//	 * @return Vraća <code>true</code> ukoliko nije moguće predmet premjestiti u
//	 *         novi termin.
//	 */
//	public static boolean hasHardConstraints(final int course, final int term, final Timetable timetable, final ConstantData constantData) {
//		if (!constantData.isAcceptable(course, term)) {
//			return true;
//		}
//		int studentCount = constantData.getStudentCount(course);
//		boolean courseAloneInTerm = true;
//		for (int i = constantData.getCourseCount(); --i >= 0;) {
//			if (i != course && term == timetable.getTermIndex(i)) {
//				if (studentCount > 0) {
//					courseAloneInTerm = false;
//				}
//				studentCount += constantData.getStudentCount(i);
//				if (constantData.getSameStudentsCount(i, course) > 0) {
//					return true;
//				}
//			}
//		}
//
//		if (studentCount > (courseAloneInTerm ? constantData.getTerm(term).getHardCapacity() : constantData.getTerm(term).getSoftCapacity())) {
//			return true;
//		}
//		
//		return false;
//	}
	
	public static int getCloseTerms(final int course,
			final VariableTimetable timetable, final ConstantData constantData,
			final int[] buffer, final boolean excludeCurrent, final int distance) {
		
		int currentTerm = timetable.getTermIndex(course);
		int currentTermDayIndex = 0;
		boolean dayIsFound = false;
		
//		// finding current term's day index
//		if (constantData.termsInDays == null) {
//			return 0;
//		}
		
		for (int i = constantData.getDayCount(); --i >= 0;) {
			if (constantData.getTermsForDay(i) == null) {
				continue;
			}
			
			for (int y = constantData.getTermsForDay(i).length; --y >= 0;) {
				if (constantData.getTermsForDay(i)[y] == currentTerm) {
					currentTermDayIndex = i;
					dayIsFound = true;
					break;
				}
			}
			
			if (dayIsFound)
				break;
		}
		// defining day borders
		int fromDay = currentTermDayIndex - distance < 0 ? 0 : currentTermDayIndex - distance;
		int toDay = currentTermDayIndex + distance >= constantData.getDayCount() ? constantData.getDayCount() - 1
				: currentTermDayIndex + distance;
		
		// putting close terms in buffer
		int testTerm = 0;
		int positionInBuffer = 0;
		for (int day = fromDay; day <= toDay; day++) {
			if (constantData.getTermsForDay(day) == null) {
				continue;
			}
			
			for (int pos = 0; pos < constantData.getTermsForDay(day).length; pos++) {
				testTerm = constantData.getTermsForDay(day)[pos];
				
				if (excludeCurrent && testTerm == currentTerm) {
					continue;
				}
				
				if (timetable.isTransferPossible(course, testTerm)) {
					buffer[positionInBuffer] = testTerm;
					positionInBuffer++;
				}
			}
		}
		
		return positionInBuffer;
	}
	
	public static boolean saveConstData(final String filename, final ConstantData constantData) {
		boolean result = true;
		ObjectOutputStream oos = null;
		try {
			final FileOutputStream fout = new FileOutputStream(filename);
			final ZipOutputStream zos = new ZipOutputStream(fout);
			zos.putNextEntry(new ZipEntry("constantData.dat"));
			oos = new ObjectOutputStream(zos);
			oos.writeObject(constantData);
			zos.closeEntry();
		} catch (Exception e) { 
			e.printStackTrace(); 
			result = false;
		} finally {
			try {
				oos.close();
			} catch (Exception ignorable) { }
		}
		return result;
	}
	
	// TODO: bacati IOException
	// TODO: iskoristiti Apache Commons IO
	public static ConstantData loadConstData(final String filename) {
		ConstantData constantData = null;
		ObjectInputStream ois = null;
		try {
			final FileInputStream fin = new FileInputStream(filename);
			final ZipInputStream zis = new ZipInputStream(fin);
			zis.getNextEntry();
			ois = new ObjectInputStream(zis);
			
			constantData = (ConstantData) ois.readObject();
			ois.close();
		} catch (Exception e) { 
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (Exception ignorable) { }
		}
		return constantData;
	}

	/**
	 * Metoda ispisuje raspored u datoteku.
	 */
	public static boolean writeTimetable(ConstantTimetable timetable, ConstantData constantData, File file) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
		BufferedWriter out = null;
		boolean success;
		
		try {
			out = new BufferedWriter(new FileWriter(file));
			
			final int termsLength = constantData.getTermCount();
			for (int i = 0; i < termsLength; i++) {
				int courseCount = timetable.getCourseCount(i);
				if (courseCount > 0) {
					for (int j = 0; j < courseCount; j++) {
						Course course = constantData.getCourse(timetable.getCourseInTermIndex(i, j));
						if (course instanceof JoinedCourse) {
							JoinedCourse joinedCourse = (JoinedCourse) course;
							out.append(dateFormat.format(constantData.getTerm(i).getDate()));
							out.append("\t2\t");
							out.append(joinedCourse.getCourse1().getName());
							out.append("\t");
							out.append(joinedCourse.getCourse1().getCode());
							out.newLine();
							out.append(dateFormat.format(constantData.getTerm(i).getDate()));
							out.append("\t2\t");
							out.append(joinedCourse.getCourse2().getName());
							out.append("\t");
							out.append(joinedCourse.getCourse2().getCode());
							out.newLine();
						} else {
							out.append(dateFormat.format(constantData.getTerm(i).getDate()));
							out.append("\t2\t");
							out.append(course.getName());
							out.append("\t");
							out.append(course.getCode());
							out.newLine();
						}
					}
				}
			}
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		} finally {
			try { out.close(); } catch (Exception e) {}
		}
		
		return success;
	}
	
	public static VariableTimetable readTimetable(String file, ConstantData constantData) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
		
		BufferedReader reader = null;
		VariableTimetable timetable = new VariableTimetable(constantData);
		
		try {
			reader = new BufferedReader(new FileReader(file));
			
			while(true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				String[] data = line.split("\t");
				Date date = dateFormat.parse(data[0] + "\t" + data[1]);
				String code = data[4];
				
				int termIndex = -1;
				for (int i = 0; i < constantData.getTermCount(); i++) {
					Term currentTerm = constantData.getTerm(i);
					if (currentTerm.getDate().equals(date)) {
						termIndex = i;
						break;
					}
				}
				
				int courseIndex = -1;
				for (int i = 0; i < constantData.getCourseCount(); i++) {
					Course currentCourse = constantData.getCourse(i);
					
					if (currentCourse instanceof JoinedCourse) {
						JoinedCourse joinedCourse = (JoinedCourse) currentCourse;
						if (joinedCourse.getCourse1().getCode().equals(code) || joinedCourse.getCourse2().getCode().equals(code)) {
							courseIndex = i;
							break;
						}
					} else if (currentCourse.getCode().equals(code)) {
						courseIndex = i;
						break;
					}
				}
				
				if (courseIndex == -1) {
					log.warn("Unknown course in line " + line);
					return null;
				}
				
				if (termIndex == -1) {
					log.warn("Unknown term in line " + line);
					return null;
				}
				
				timetable.setTermIndex(courseIndex, termIndex);
			}
			
			
		} catch (Exception e) {
			log.error("Error reading timetable", e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
		
		return timetable;
	}
	
}