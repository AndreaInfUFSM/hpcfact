package br.ufsm.inf.viewCriticalSection.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import br.ufsm.inf.viewCriticalSection.ViewCriticalSectionPlugin;

/**
 * @author Eduardo
 */

public class CriticalSectionView extends CriticalSectionTableViewPart {
	// private TableViewer viewer;
	private Action doubleClickAction;
	private CriticalSectionOpenMarkedAction openAction;

	private CriticalSectionListener modelListener;
	private CriticalSectionModel model;

	public static final int COL_IMAGE = 0;
	public static final int COL_VAR_NAME = 1;
	public static final int COL_FILE_NAME = 2;
	public static final int COL_TYPE = 3;
	public static final int COL_DETAILS = 4;
//	public static final int COL_MARKER = 5;

	// private String columnHeaders[] =
	// { "", "Project", "File Name", "Type", "Suggested Refactoring", };
	private String columnHeaders[] = { "", "Variable", "File Name", "Type",
			"Occurrence of the Variable"};

	private ColumnLayoutData columnLayouts[] = {
			new ColumnPixelData(19, false), new ColumnWeightData(100),
			new ColumnWeightData(200), new ColumnWeightData(100),
			new ColumnWeightData(75)};

	public CriticalSectionView() {
		super();
		model = ViewCriticalSectionPlugin.getDefault().getModel();
		modelListener = new CriticalSectionListener();
		setColumnHeaders(columnHeaders);
		setColumnLayouts(columnLayouts);
	}

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return new String[] { /* "One", "Two", "Three" */};
		}
	}

	/*
	 * class ViewLabelProvider extends LabelProvider implements
	 * ITableLabelProvider { public String getColumnText(Object obj, int index)
	 * { return getText(obj); } public Image getColumnImage(Object obj, int
	 * index) { return getImage(obj); } public Image getImage(Object obj) {
	 * return PlatformUI.getWorkbench().
	 * getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT); } }
	 */class NameSorter extends ViewerSorter {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		/*
		 * viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL |
		 * SWT.V_SCROLL); viewer.setContentProvider(new ViewContentProvider());
		 * viewer.setLabelProvider(new ViewLabelProvider());
		 * viewer.setSorter(new NameSorter()); viewer.setInput(getViewSite());
		 * makeActions(); // OK hookContextMenu(); hookDoubleClickAction();
		 * contributeToActionBars(); model.addListener(modelListener);
		 */
		super.createPartControl(parent);
		TableViewer viewer = getViewer();
		viewer.setContentProvider(model);
		viewer.setLabelProvider(new CriticalSectionViewLabelProvider());
		viewer.setInput(getViewSite());
		// setDoubleClickAction(action1);
		setDoubleClickAction(openAction);
		model.addListener(modelListener);

	}

	protected void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
    	fillLocalToolBar(bars.getToolBarManager());
	}

	protected void createActions() {
		openAction = new CriticalSectionOpenMarkedAction(getSite()) {
			public void run() {

				ISelection selection = getViewer().getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				
				CriticalSectionEvent ev = (CriticalSectionEvent) obj;
				IMarker marker = ev.getMarker();
                run(marker);
				
				if (marker.getResource() instanceof IFile) {
					try {
						IDE.openEditor(this.getSite().getPage(), marker,
								OpenStrategy.activateOnOpen());
					} catch (PartInitException e) {
						MessageDialog
								.openError(
										this.getSite().getShell(),
										"Unable to open file in editor for given marker",
										e.getMessage());
					}
				}

			}
		};
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = getViewer().getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
			}
		};
	}

	private void hookDoubleClickAction() {
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openAction.run();
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		getViewer().getControl().setFocus();
	}

	private class CriticalSectionListener implements ICriticalSectionListener {
		public void handleEvent(final CriticalSectionEvent evento) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					getViewer().add(evento);
				}
			}
			
			);

		}
		
		//dionatan
		public void removeEvent(final CriticalSectionEvent evento) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					getViewer().remove(evento);
				}
			}
			
			);

		}
	}
}
