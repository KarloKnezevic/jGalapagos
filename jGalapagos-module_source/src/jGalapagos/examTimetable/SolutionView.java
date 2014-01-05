package jGalapagos.examTimetable;

import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.model.Course;
import jGalapagos.examTimetable.model.JoinedCourse;
import jGalapagos.examTimetable.model.Term;
import jGalapagos.examTimetable.util.AlgorithmUtilities;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

public class SolutionView extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy.");
	private final DateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private final NumberFormat studentCountFormat = new DecimalFormat("0000");
	private final NumberFormat penaltyFormat = new DecimalFormat("000.0000");
	private final JFileChooser saveToChooser = new JFileChooser();
	
	private final ConstantTimetable constantTimetable;
	private final ConstantData constantData;
	private final JTable[] tables;
	private CourseSet selectedCourses = null;
	private CourseSet selectedCourseGroup = null;

	
	public SolutionView(final ConstantTimetable constantTimetable, final ConstantData constantData) {
		super(new BorderLayout());
		this.constantTimetable = constantTimetable;
		this.constantData = constantData;
		tables = new JTable[constantData.getTermCount()];

		JPanel coursePanel = new JPanel();
		coursePanel.setLayout(new TimetableLayoutManager(constantTimetable, constantData, coursePanel));
		for (int termIndex = 0; termIndex < constantData.getTermCount(); termIndex++) {
			JTable termTable = createTableForTerm(termIndex);
			coursePanel.add(new JScrollPane(termTable));
			termTable.getParent().setBackground(Color.WHITE);
			tables[termIndex] = termTable;
		}
		int firstDayIndex = constantData.getTerm(0).getDayIndex() - 1;
		for (int dayIndex = firstDayIndex; dayIndex < constantData.getDayCount(); dayIndex++) {
			int[] termsForDay = constantData.getTermsForDay(dayIndex);
			if (termsForDay == null) continue;
			Date date = constantData.getTerm(termsForDay[0]).getDate();
			coursePanel.add(new JLabel(dateFormat.format(date), JLabel.CENTER));
		}
		
		// calculate courses in day and term
		int[] coursesInTermForStudent = new int[constantData.getTermCount()];
		int[] coursesInDayForStudent = new int[constantData.getDayCount() + 1];
		int globalMaxCoursesInDay = 0;
		Set<String> studentsWithMoreThenOneCourseInTerm = new HashSet<String>();
		Map<String, int[]> maxCoursesInDayByStudents = new HashMap<String, int[]>();
		for (int studentIndex = 0; studentIndex < constantData.getStudentCount(); studentIndex++) {
			String jmbag = constantData.getJmbag(studentIndex);
			Arrays.fill(coursesInDayForStudent, firstDayIndex, constantData.getDayCount() + 1, 0);
			Arrays.fill(coursesInTermForStudent, 0);
			int[] courseIndexes = constantData.getCourseIndexes(studentIndex);
			boolean moreThenOneCourseInTerm = false;
			int maxCoursesInDay = 0;
			for (int i = 0; i < courseIndexes.length; i++) {
				int courseIndex1 = courseIndexes[i];
				int termIndex1 = constantTimetable.getTermIndex(courseIndex1);
				if (++coursesInTermForStudent[termIndex1] >= 2) moreThenOneCourseInTerm = true;
				int coursesInTermOfCourse = ++coursesInDayForStudent[constantData.getTerm(termIndex1).getDayIndex()];
				maxCoursesInDay = Math.max(coursesInTermOfCourse, maxCoursesInDay);
			}
			if (moreThenOneCourseInTerm) {
				studentsWithMoreThenOneCourseInTerm.add(jmbag);
			}
			globalMaxCoursesInDay = Math.max(maxCoursesInDay, globalMaxCoursesInDay);
			
			if (maxCoursesInDay > 1) {
				int[] coursesInDayArray = new int[maxCoursesInDay - 1];
				for (int i = firstDayIndex; i < constantData.getDayCount() + 1; i++) {
					int coursesInDay = coursesInDayForStudent[i];
					if (coursesInDay > 1) {
						coursesInDayArray[coursesInDay - 2]++;
					}
				}
				maxCoursesInDayByStudents.put(jmbag, coursesInDayArray);
			}
		}
		
		// calculate fitness by student
		boolean hasCollisions = !studentsWithMoreThenOneCourseInTerm.isEmpty();
		int collisionCount = 0;
		int[] timesInDay = new int[globalMaxCoursesInDay - 1];
		int[] studentCountTimesInDay = new int[globalMaxCoursesInDay - 1];
		String[][] studentData = new String[constantData.getStudentCount()][2 + (globalMaxCoursesInDay - 1) + (hasCollisions ? 1 : 0)];
		for (int studentIndex = 0; studentIndex < constantData.getStudentCount(); studentIndex++) {
			String jmbag = constantData.getJmbag(studentIndex);
			studentData[studentIndex][0] = jmbag;
			studentData[studentIndex][1] = penaltyFormat.format(constantData.getFitnessForStudent(studentIndex, constantTimetable));
			int[] courseCountInDayForStudent = maxCoursesInDayByStudents.get(jmbag);
			if (courseCountInDayForStudent != null) {
				for (int i = 0; i < courseCountInDayForStudent.length; i++) {
					int value = courseCountInDayForStudent[i];
					if (value != 0) {
						timesInDay[i] += value;
						studentCountTimesInDay[i]++;
						studentData[studentIndex][i + 2] = String.valueOf(value);
					}
				}
			}
			if (hasCollisions && studentsWithMoreThenOneCourseInTerm.contains(jmbag)) {
				collisionCount++;
				studentData[studentIndex][globalMaxCoursesInDay + 2] = "true";
			}
		}
		
		// calculate courseSet for students
		final Map<String, CourseSet[]> studentsInCourses = new HashMap<String, CourseSet[]>();
		for (int studentIndex = 0; studentIndex < constantData.getStudentCount(); studentIndex++) {
			int[] courseIndexes = constantData.getCourseIndexes(studentIndex);
			CourseSet courseSet = new CourseSet();
			for (int i = 0; i < courseIndexes.length; i++) {
				int courseIndex = courseIndexes[i];
				Course course = constantData.getCourse(courseIndex);
				if (course instanceof JoinedCourse) {
					JoinedCourse joinedCourse = (JoinedCourse) course;
					courseSet.add(joinedCourse.getCourse1());
					courseSet.add(joinedCourse.getCourse2());
				} else {
					courseSet.add(course);
				}
			}
			String jmbag = constantData.getJmbag(studentIndex);
			
			int[] courseGroupIndexes = constantData.getCourseGroupIndexes(constantData.getGroupIndexForStudent(studentIndex));
			CourseSet courseGroup = new CourseSet();
			for (int i = 0; i < courseGroupIndexes.length; i++) {
				int courseIndex = courseGroupIndexes[i];
				Course course = constantData.getCourse(courseIndex);
				if (course instanceof JoinedCourse) {
					JoinedCourse joinedCourse = (JoinedCourse) course;
					courseGroup.add(joinedCourse.getCourse1());
					courseGroup.add(joinedCourse.getCourse2());
				} else {
					courseGroup.add(course);
				}
			}
			
			studentsInCourses.put(jmbag, new CourseSet[] { courseSet, courseGroup });
		}
		
		
		// calculate student count for courseSets
		Set<CourseSet> courseSets = new HashSet<CourseSet>();
		for (CourseSet[] values : studentsInCourses.values()) {
			courseSets.add(values[0]);
		}
		Object[][] data = new Object[courseSets.size()][2];
		int current = 0;
		for (CourseSet courseSet : courseSets) {
			data[current][0] = courseSet;
			int count = 0;
			for (CourseSet[] courseSetArray : studentsInCourses.values()) {
				if (courseSet.equals(courseSetArray[0])) {
					count++;
				}
			}
			data[current][1] = studentCountFormat.format(count);
			current++;
		}
		
		// calculate fitness by theoretical courses
		final String[][] fitnessForTheoreticalCourses = new String[constantData.getCountOfCoursesPairs()][4];
		for (int i = 0; i < constantData.getCountOfCoursesPairs(); i++) {
			int[] coursePair = constantData.getCoursesPair(i);
			int termIndex1 = constantTimetable.getTermIndex(coursePair[0]);
			int termIndex2 = constantTimetable.getTermIndex(coursePair[1]);
			double penalty = constantData.getPenaltyBetweenDays(termIndex1, termIndex2);
			Course course1 = constantData.getCourse(coursePair[0]);
			Course course2 = constantData.getCourse(coursePair[1]);
			String[] students1 = course1.getJmbags();
			String[] students2 = course2.getJmbags();
			int studentCount = 0;
			for (int j = 0; j < students1.length; j++) {
				String jmbag = students1[j];
				if (Arrays.binarySearch(students2, jmbag) >= 0) {
					studentCount++;
				}
			}
			fitnessForTheoreticalCourses[i][0] = course1.getCode();
			fitnessForTheoreticalCourses[i][1] = course2.getCode();
			fitnessForTheoreticalCourses[i][2] = Integer.toString(studentCount);
			fitnessForTheoreticalCourses[i][3] = penaltyFormat.format(penalty);
		}
		
		Object[][] profilesData = new Object[constantData.getCourseGroupCount()][2];
		for (int i = 0; i < constantData.getCourseGroupCount(); i++) {
			int[] courseIndexes = constantData.getCourseGroupIndexes(i);
			CourseSet courseSet = new CourseSet();
			for (int courseIndex : courseIndexes) {
				courseSet.add(constantData.getCourse(courseIndex));
			}
			profilesData[i][0] = constantData.getCourseGroupLabel(i);
			profilesData[i][1] = courseSet;
		}
		
		final String[] columnNames = new String[2 + (globalMaxCoursesInDay - 1) + (hasCollisions ? 1 : 0)];
		columnNames[0] = "JMBAG";
		columnNames[1] = "Penalty";
		for (int i = 0; i < globalMaxCoursesInDay - 1; i++) {
			columnNames[i + 2] = (i + 2) + " in day";
		}
		if (hasCollisions) {
			columnNames[globalMaxCoursesInDay + 2] = "Collision";
		}
		final JTable studentsTable = new JTable(studentData, columnNames);
		studentsTable.setAutoCreateRowSorter(true);
		studentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int row = studentsTable.getSelectedRow();
					String jmbag = (String) studentsTable.getValueAt(row, 0);
					CourseSet[] courseSets = studentsInCourses.get(jmbag);
					selectedCourses = courseSets[0];
					selectedCourseGroup = courseSets[1];
					for (int i = 0; i < tables.length; i++) {
						tables[i].repaint();
					}
				}
					
			}
		});
		
		final JTable pairsTable = new JTable(fitnessForTheoreticalCourses, new String[] { "Code 1", "Code 2", "Students", "Penalty" });
		pairsTable.setAutoCreateRowSorter(true);
		pairsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int row = pairsTable.getSelectedRow();
					String courseCode1 = (String) pairsTable.getValueAt(row, 0);
					String courseCode2 = (String) pairsTable.getValueAt(row, 1);
					Course course1 = null;
					Course course2 = null;
					for (int i = 0; i < constantData.getCourseCount(); i++) {
						Course course = constantData.getCourse(i);
						if (course.getCode().equals(courseCode1)) {
							course1 = course;
						} else if (course.getCode().equals(courseCode2)) {
							course2 = course;
						}
						if (course1 != null && course2 != null) {
							break;
						}
					}
					CourseSet courseSet = new CourseSet();
					courseSet.add(course1);
					courseSet.add(course2);
					selectedCourses = courseSet;
					selectedCourseGroup = null;
					for (int i = 0; i < tables.length; i++) {
						tables[i].repaint();
					}
				}
					
			}
		});
		
		final JTable groupsTable = new JTable(data, new String[] { "Codes", "Count" });
		groupsTable.setAutoCreateRowSorter(true);
		groupsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int row = groupsTable.getSelectedRow();
					CourseSet courseSet = (CourseSet) groupsTable.getValueAt(row, 0);
					selectedCourses = courseSet;
					selectedCourseGroup = null;
					for (int i = 0; i < tables.length; i++) {
						tables[i].repaint();
					}
				}
					
			}
		});
		
		final JTable profilesTable = new JTable(profilesData, new String[] { "Label", "Codes" });
		profilesTable.setAutoCreateRowSorter(true);
		profilesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int row = profilesTable.getSelectedRow();
					CourseSet courseSet = (CourseSet) profilesTable.getValueAt(row, 1);
					selectedCourses = courseSet;
					selectedCourseGroup = null;
					for (int i = 0; i < tables.length; i++) {
						tables[i].repaint();
					}
				}
			}
		});
		
		final CardLayout cardLayout = new CardLayout();
		final JPanel showByPanel = new JPanel(cardLayout);
		
		final JComboBox comboBox = new JComboBox(new String[] { "students", "pairs", "groups", "profiles" });
		comboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedItem = comboBox.getSelectedItem().toString();
				cardLayout.show(showByPanel, selectedItem);
			}
		});
		showByPanel.add(new JScrollPane(studentsTable), "students");
		showByPanel.add(new JScrollPane(pairsTable), "pairs");
		showByPanel.add(new JScrollPane(groupsTable), "groups");
		showByPanel.add(new JScrollPane(profilesTable), "profiles");
		
		studentsTable.getParent().setBackground(Color.WHITE);
		pairsTable.getParent().setBackground(Color.WHITE);
		groupsTable.getParent().setBackground(Color.WHITE);
		profilesTable.getParent().setBackground(Color.WHITE);
		
		JPanel comboBoxPanel = new JPanel(new BorderLayout());
		comboBox.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		comboBoxPanel.add(new JLabel("Show by:"), BorderLayout.WEST);
		comboBoxPanel.add(comboBox);
		
		JPanel statisticsPanel = new JPanel();
		statisticsPanel.setLayout(new BoxLayout(statisticsPanel, BoxLayout.Y_AXIS));
		statisticsPanel.setBorder(BorderFactory.createTitledBorder("Statistic data"));
		statisticsPanel.add(new JLabel("Penalty: " + penaltyFormat.format(constantTimetable.getFitness()[0])));
		if (hasCollisions) {
			statisticsPanel.add(new JLabel("Collision count: " + collisionCount));
		}
		for (int i = 0; i < timesInDay.length; i++) {
			statisticsPanel.add(new JLabel("Same day " + (i + 2) + ": " + timesInDay[i] + " (" + studentCountTimesInDay[i] + ")"));
		}

		JButton buttonSave = new JButton("Save");
		buttonSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (saveToChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = saveToChooser.getSelectedFile();
					AlgorithmUtilities.writeTimetable(constantTimetable, constantData, file);
				}
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonSave);
		
		JPanel bottomLeftPanel = new JPanel(new BorderLayout());
		bottomLeftPanel.add(statisticsPanel, BorderLayout.CENTER);
		bottomLeftPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		JPanel leftPane = new JPanel(new BorderLayout());
		leftPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		leftPane.add(comboBoxPanel, BorderLayout.NORTH);
		leftPane.add(showByPanel, BorderLayout.CENTER);
		leftPane.add(bottomLeftPanel, BorderLayout.SOUTH);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(leftPane);
		splitPane.setRightComponent(coursePanel);
		splitPane.setDividerLocation(300);
		
		add(splitPane, BorderLayout.CENTER);
	}
	
	private JTable createTableForTerm(int termIndex) {
		int courseCountInTerm = constantTimetable.getCourseCount(termIndex);
		int joinedCourseCount = 0;
		for (int k = 0; k < courseCountInTerm; k++) {
			int courseIndex = constantTimetable.getCourseInTermIndex(termIndex, k);
			Course course = constantData.getCourse(courseIndex);
			if (course instanceof JoinedCourse) {
				joinedCourseCount++;
			}
		}
		Course[][] data = new Course[courseCountInTerm + joinedCourseCount][1];
		int studentCount = 0;
		int position = 0;
		for (int k = 0; k < courseCountInTerm; k++) {
			int courseIndex = constantTimetable.getCourseInTermIndex(termIndex, k);
			Course course = constantData.getCourse(courseIndex);
			if (course instanceof JoinedCourse) {
				JoinedCourse joinedCourse = (JoinedCourse) course;
				Course course1 = joinedCourse.getCourse1();
				data[position++][0] = course1;
				studentCount += course1.getStudentCount();
				Course course2 = joinedCourse.getCourse2();
				data[position++][0] = course2;
				studentCount += course2.getStudentCount();
			} else {
				data[position++][0] = course;
				studentCount += course.getStudentCount();
			}
		}
		
		Term term = constantData.getTerm(termIndex);
		String heading =  timeFormat.format(term.getDate()) + " (" + studentCount + ")";
		JTable table = new JTable(data, new String[] { heading }) {
			private static final long serialVersionUID = 1L;
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
				if (c instanceof JComponent) {
					JComponent jc = (JComponent) c;
					Course course = (Course) getValueAt(rowIndex, vColIndex);
					jc.setToolTipText("[" + course.getCode() + "] " + course.getName() + " (" + course.getStudentCount() + ")");
				}
				return c;
			}
		};
		table.setDefaultRenderer(Object.class, new TimetableCellRenderer(table.getDefaultRenderer(String.class)));
		return table;
	}

	private final class TimetableCellRenderer implements TableCellRenderer {
		
		private final TableCellRenderer r;

		public TimetableCellRenderer(TableCellRenderer r) {
			this.r = r;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Color bg = null;
			boolean bold = false;
			Course course = (Course) value;
			Set<Course> selectedCoursesSet = selectedCourses;
			Set<Course> selectedCoursesGroup = selectedCourseGroup;
			if (selectedCoursesSet != null &&  selectedCoursesSet.contains(course)) {
				switch (course.getYear()) {
				case 1:
					bg = Color.GREEN;
					break;
				case 2:
					bg = Color.YELLOW;
					break;
				case 3:
					bg = Color.ORANGE;
					break;
				case 4:
					bg = Color.MAGENTA;
					break;
				case 5:
					bg = Color.CYAN;
					break;
				default:
					bg = Color.LIGHT_GRAY;
					break;
				}
				if (selectedCoursesGroup != null && selectedCoursesGroup.contains(course)) {
					bold = true;
				}
			} else {
				bg = Color.WHITE;
			}
			
			Component c = r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			c.setBackground(bg);
			c.setFont(c.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN));
			return c;
		};
	}
	
	private final class TimetableLayoutManager implements LayoutManager {
		
		private final int EMPTY_DAY_WIDTH = 20;
		private final int columnCount;
		private final int rowCount;
		private final int[] termPosition;
		private final boolean[] emptyDays;
		private final int emptyDayCount;
		private final int numberOfCells;
		
		public TimetableLayoutManager(ConstantTimetable constantTimetable, ConstantData constantData, Container parent) {
			Calendar calendar = Calendar.getInstance();
			int earliestExam = Integer.MAX_VALUE;
			int latestExam = 0;
			int minInterval = Integer.MAX_VALUE;
			int firstDayIndex = constantData.getTerm(0).getDayIndex() - 1;
			emptyDays = new boolean[constantData.getDayCount() - firstDayIndex];
			termPosition = new int[constantData.getTermCount()];
			
			int emptyDayCount = 0;
			for (int dayIndex = firstDayIndex; dayIndex < constantData.getDayCount(); dayIndex++) {
				int[] termIndexes = constantData.getTermsForDay(dayIndex);
				if (termIndexes == null) {
					emptyDayCount++;
					emptyDays[dayIndex - firstDayIndex] = true;
					continue;
				}
				for (int i = 0; i < termIndexes.length; i++) {
					int termIndex = termIndexes[i];
					Term term = constantData.getTerm(termIndex);
					calendar.setTime(term.getDate());
					int quarterHours = (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) / 15;
					earliestExam = Math.min(earliestExam, quarterHours);
					latestExam = Math.max(latestExam, quarterHours);
					if (i != 0) {
						minInterval = Math.min(minInterval, (int) ((term.getDate().getTime() - constantData.getTerm(termIndexes[i - 1]).getDate().getTime()) / 900000));
					}
					termPosition[termIndex] = quarterHours;
				}
			}
			this.emptyDayCount = emptyDayCount;
			
			rowCount = latestExam + minInterval - earliestExam;
			columnCount = constantData.getDayCount() - firstDayIndex;
			numberOfCells = minInterval;
			
			for (int i = 0; i < constantData.getTermCount(); i++) {
				termPosition[i] -= earliestExam;
			}
		}

		@Override
		public void addLayoutComponent(String name, Component comp) { }

		@Override
		public void removeLayoutComponent(Component comp) { }

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			Insets insets = parent.getInsets();
			int preferredCellWidth = 0;
			int preferredCellHeight = 0;
			int prederredHeightOfHeader = 0;
			
			for (int i = 0; i < termPosition.length; i++) {
				Dimension preferredComponentSize = parent.getComponent(i).getPreferredSize();
				preferredCellWidth = Math.max(preferredCellWidth, preferredComponentSize.width);
				preferredCellHeight = Math.max(preferredCellHeight, preferredComponentSize.height);
			}
			for (int i = termPosition.length; i < termPosition.length + columnCount - emptyDayCount; i++) {
				Dimension preferredComponentSize = parent.getComponent(i).getPreferredSize();
				preferredCellWidth = Math.max(preferredCellWidth, preferredComponentSize.width);
				prederredHeightOfHeader = Math.max(prederredHeightOfHeader, preferredComponentSize.height);
			}
			
			int preferredWidth = preferredCellWidth * (columnCount - emptyDayCount) + emptyDayCount * EMPTY_DAY_WIDTH + insets.left + insets.right;
			int preferredHeight = (int) (((double) preferredCellHeight) * rowCount / numberOfCells) + prederredHeightOfHeader + insets.top + insets.bottom;
			return new Dimension(preferredWidth, preferredHeight);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(0, 0);
		}

		@Override
		public void layoutContainer(Container parent) {
			Insets insets = parent.getInsets();
			int width = parent.getWidth() - (insets.left + insets.right);
			int height = parent.getHeight() - (insets.top + insets.bottom);
			
			double cellWidth = (double) (width - emptyDayCount * EMPTY_DAY_WIDTH) / (columnCount - emptyDayCount);
			double cellHeight = (double) height / rowCount;
			
			int firstDayIndex = constantData.getTerm(0).getDayIndex() - 1;
			double currentPosition = insets.left;
			int currentHeader = termPosition.length;
			for (int dayIndex = firstDayIndex; dayIndex < constantData.getDayCount(); dayIndex++) {
				int[] termIndexes = constantData.getTermsForDay(dayIndex);
				if (termIndexes == null) {
					currentPosition += EMPTY_DAY_WIDTH;
					continue;
				}
				Component headerComponent = parent.getComponent(currentHeader++);
				int preferredHeaderHeight = headerComponent.getPreferredSize().height;
				headerComponent.setBounds((int) currentPosition, insets.top, (int) cellWidth, preferredHeaderHeight);
				
				for (int i = 0; i < termIndexes.length; i++) {
					int termIndex = termIndexes[i];
					int x = (int) currentPosition;
					int y = (int) (cellHeight * termPosition[termIndex] + preferredHeaderHeight + insets.top);
					int w = (int) cellWidth;
					int h = (int) (numberOfCells * cellHeight);
					parent.getComponent(termIndex).setBounds(x, y, w, h);
				}
				currentPosition += cellWidth;
			}
		}
		
	}
	
	private class CourseSet extends HashSet<Course> {
		
		private static final long serialVersionUID = 1L;
		private StringBuffer buffer = new StringBuffer();
		private int hash = 0;

		@Override
		public int hashCode() {
			return hash;
		}
		
		@Override
		public boolean add(Course e) {
			hash += e.hashCode();
			buffer.append(e.getCode()).append(" ");
			return super.add(e);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (!(o instanceof CourseSet)) return false;
			if (hash != o.hashCode()) return false;
			CourseSet set = (CourseSet) o;
			if (size() != set.size()) return false;
			for (Course course : this) {
				if (!set.contains(course)) {
					return false;
				}
			}
			return true;
		}
		
		@Override
		public String toString() {
			return buffer.toString();
		}
		
	}
	
//	public static void main(String[] args) {		
//		String projectFile = "data/2010-2011-ljetni-mi/projekt.txt";
//		String byYearsFile = "data/2010-2011-ljetni-mi/poGodinama.txt";
//		String courseGroupsFile = "data/2010-2011-ljetni-mi/grupePredmeta.txt";
//		String studentGroupFile = "data/2010-2011-ljetni-mi/grupaStudenata.txt";
//		String coursePairsFile = "data/2010-2011-ljetni-mi/dijeljeniStudentiT.txt";
//		
//		ConstantData constantData = ConstantData.loadTextData(projectFile, byYearsFile, courseGroupsFile, studentGroupFile, coursePairsFile);
//		ConstantTimetable timetable = AlgorithmUtilities.readTimetable("raspored.txt", constantData).getConstantTimetable();
//		
//		SolutionView solutionView = new SolutionView(timetable, constantData);
//		
//		JFrame frame = new JFrame();
//		frame.setSize(800, 800);
//		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		frame.add(solutionView);
//		frame.setVisible(true);
//	}

}
