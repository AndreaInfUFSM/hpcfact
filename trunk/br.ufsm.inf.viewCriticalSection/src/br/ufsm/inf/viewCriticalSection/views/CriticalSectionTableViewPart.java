package br.ufsm.inf.viewCriticalSection.views;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Dionatan Kitzmann Tietzmann
 */

public class CriticalSectionTableViewPart extends ViewPart {
	protected static final String TAG_COLUMN = "column";

	protected static final String TAG_NUMBER = "number";

	protected static final String TAG_WIDTH = "width";

	private String columnHeaders[];

	private ColumnLayoutData columnLayouts[];

	private IAction doubleClickAction;

	private IMemento memento;

	private Table table;

	private TableViewer viewer;

	
	public CriticalSectionTableViewPart(){
		super();
	}
	
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL);

		table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createColumns();
		createActions();
		hookEvents();
	}

	protected void createColumns() {
		if (memento != null) {
			restoreColumnWidths(memento);
		}

		TableLayout layout = new TableLayout();
		table.setLayout(layout);

		for (int i = 0; i < columnHeaders.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.NONE, i);

			tc.setText(columnHeaders[i]);
			tc.setResizable(columnLayouts[i].resizable);
			layout.addColumnData(columnLayouts[i]);
		}
	}

	protected void restoreColumnWidths(IMemento memento) {
		IMemento children[] = memento.getChildren(TAG_COLUMN);
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				Integer val = children[i].getInteger(TAG_NUMBER);
				if (val != null) {
					int index = val.intValue();
					val = children[i].getInteger(TAG_WIDTH);
					if (val != null) {
						columnLayouts[index] = new ColumnPixelData(val
								.intValue(), true);
					}
				}
			}
		}
	}

	protected void saveColumnWidths(IMemento memento) {
		Table table = viewer.getTable();
		TableColumn columns[] = table.getColumns();

		for (int i = 0; i < columns.length; i++) {
			if (columnLayouts[i].resizable) {
				IMemento child = memento.createChild(TAG_COLUMN);
				child.putInteger(TAG_NUMBER, i);
				child.putInteger(TAG_WIDTH, columns[i].getWidth());
			}
		}
	}

	protected void hookEvents() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() != null)
					CriticalSectionTableViewPart.this.selectionChanged(event);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

//	protected void contributeToActionBars() {
//		IActionBars bars = getViewSite().getActionBars();
//		fillLocalPullDown(bars.getMenuManager());
//		fillLocalToolBar(bars.getToolBarManager());
//	}

	public void saveState(IMemento memento) {
		if (viewer == null) {
			if (this.memento != null) //Keep the old state;
				memento.putMemento(this.memento);
			return;
		}

		saveColumnWidths(memento);
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * @return   Returns the table.
	 * @uml.property   name="table"
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * @return   Returns the viewer.
	 * @uml.property   name="viewer"
	 */
	public TableViewer getViewer() {
		return viewer;
	}

	/**
	 * @param columnHeaders   The columnHeaders to set.
	 * @uml.property   name="columnHeaders"
	 */
	public void setColumnHeaders(String[] strings) {
		columnHeaders = strings;
	}

	/**
	 * @param columnLayouts   The columnLayouts to set.
	 * @uml.property   name="columnLayouts"
	 */
	public void setColumnLayouts(ColumnLayoutData[] data) {
		columnLayouts = data;
	}

	/**
	 * @param doubleClickAction   The doubleClickAction to set.
	 * @uml.property   name="doubleClickAction"
	 */
	public void setDoubleClickAction(IAction action) {
		doubleClickAction = action;
	}

	protected void fillContextMenu(IMenuManager manager) {
	}

	protected void fillLocalPullDown(IMenuManager manager) {
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
	}

	protected void selectionChanged(SelectionChangedEvent event) {
	}

	protected void createActions() {
	}


}