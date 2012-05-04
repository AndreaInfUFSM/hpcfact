package br.ufsm.inf.viewCriticalSection.views;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

class CriticalSectionViewLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	public String getColumnText(Object obj, int index) {
		CriticalSectionEvent event = (CriticalSectionEvent) obj;
		switch (index) {
		case CriticalSectionView.COL_VAR_NAME:
			return event.getProjectName();
		case CriticalSectionView.COL_FILE_NAME:
			return event.getFileName();
		case CriticalSectionView.COL_TYPE:
			return event.getType();
		case CriticalSectionView.COL_DETAILS:
			return event.getDetails();

		}
		return "";
	}

	public Image getColumnImage(Object obj, int index) {
		CriticalSectionEvent event = (CriticalSectionEvent) obj;
		if (index == CriticalSectionView.COL_IMAGE) {
			if (event.getImage() == "E")
				return PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
			else
				return PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_DEC_FIELD_WARNING);

		} else
			return null;
	}

}