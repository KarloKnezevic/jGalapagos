package jGalapagos.examTimetable;

import jGalapagos.core.ProblemViewer;
import jGalapagos.examTimetable.model.ConstantData;

import java.awt.BorderLayout;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class TimetableProblemViewer implements ProblemViewer {

	private static final long serialVersionUID = 1L;
	
	private final JPanel panel;
	private final CourseTableModel courseTableModel;
	private final TermTableModel termTableModel;
	private final StudentTableModel studentTableModel;
	
	public TimetableProblemViewer() {
		panel = new JPanel(new BorderLayout());
		
		courseTableModel = new CourseTableModel();
		termTableModel = new TermTableModel();
		studentTableModel = new StudentTableModel();
		
		JTable courseTable = new JTable(courseTableModel);
		JTable termTable = new JTable(termTableModel);
		JTable studentTable = new JTable(studentTableModel);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Courses", new JScrollPane(courseTable));
		tabbedPane.addTab("Terms", new JScrollPane(termTable));
		tabbedPane.addTab("Students", new JScrollPane(studentTable));
		
		panel.add(tabbedPane, BorderLayout.CENTER);
	}
	
	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void updateProblem(Serializable problemDescription) {
		ConstantData constantData = (ConstantData) problemDescription;
		
		courseTableModel.setConstantData(constantData);
		termTableModel.setConstantData(constantData);
		studentTableModel.setConstantData(constantData);
	}
	
	
	private class StudentTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		private ConstantData constantData;
		private final StringBuilder builder = new StringBuilder(); 
		
		public void setConstantData(ConstantData constantData) {
			this.constantData = constantData;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return (constantData == null) ? 0 : constantData.getStudentCount();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "JMBAG";
			case 1:
				return "Group index";
			case 2:
				return "Courses";
			default:
				return null;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return constantData.getJmbag(rowIndex);
			case 1:
				return constantData.getGroupIndexForStudent(rowIndex);
			case 2:
				builder.setLength(0);
				int studentGroup = constantData.getGroupIndexForStudent(rowIndex);
				int[] courseIndexes = constantData.getCourseGroupIndexes(studentGroup);
				if (courseIndexes != null && courseIndexes.length > 0) {
					builder.append(constantData.getCourse(courseIndexes[0]).getName().replace("#", ", "));
					for (int i = 1; i < courseIndexes.length; i++) {
						builder.append(", ").append(constantData.getCourse(courseIndexes[i]).getName().replace("#", ", "));
					}
				}
				return builder.toString();
			default:
				return null;
			}
		}
	}
	
	private class TermTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		private ConstantData constantData;
		
		public void setConstantData(ConstantData constantData) {
			this.constantData = constantData;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return (constantData == null) ? 0 : constantData.getTermCount();
		}

		@Override
		public int getColumnCount() {
			return 6;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Label";
			case 1:
				return "Date";
			case 2:
				return "Soft capacity";
			case 3:
				return "Hard capacity";
			case 4:
				return "Day index";
			case 5:
				return "Within day";
			default:
				return null;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return constantData.getTerm(rowIndex).getLabel();
			case 1:
				return constantData.getTerm(rowIndex).getDate();
			case 2:
				return constantData.getTerm(rowIndex).getSoftCapacity();
			case 3:
				return constantData.getTerm(rowIndex).getHardCapacity();
			case 4:
				return constantData.getTerm(rowIndex).getDayIndex();
			case 5:
				return constantData.getTerm(rowIndex).getWithinDayIndex();
			default:
				return null;
			}
		}
		
	}
	
	private class CourseTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		private ConstantData constantData;
		
		public void setConstantData(ConstantData constantData) {
			this.constantData = constantData;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return (constantData == null) ? 0 : constantData.getCourseCount();
		}

		@Override
		public int getColumnCount() {
			return 5;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Name";
			case 1:
				return "Code";
			case 2:
				return "Year";
			case 3:
				return "Penalty";
			case 4:
				return "Students";
			default:
				return null;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return constantData.getCourse(rowIndex).getName();
			case 1:
				return constantData.getCourse(rowIndex).getCode();
			case 2:
				return constantData.getCourse(rowIndex).getYear();
			case 3:
				return constantData.getCourse(rowIndex).getPenaltyFactor();
//				return "-hidden-";
			case 4:
				return constantData.getCourse(rowIndex).getStudentCount();
			default:
				return null;
			}
		}
		
	}

}
