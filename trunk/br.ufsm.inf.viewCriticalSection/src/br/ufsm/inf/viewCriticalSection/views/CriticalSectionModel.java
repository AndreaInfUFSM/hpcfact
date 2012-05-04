package br.ufsm.inf.viewCriticalSection.views;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

@SuppressWarnings("deprecation")
public class CriticalSectionModel implements IStructuredContentProvider{
	public ListenerList listenerList = new ListenerList();
	public void addListener(ICriticalSectionListener listener){
		listenerList.add(listener);
	};
	public void removeListener(ICriticalSectionListener listener){
		listenerList.remove(listener);
	}
	public List<CriticalSectionEvent> events = new ArrayList<CriticalSectionEvent>();
	public void addEvent(CriticalSectionEvent evento){
		events.add(evento);
		Object[] listeners = listenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((ICriticalSectionListener)listeners[i]).handleEvent(evento);
		}
		
	}
	public void clear(){
		for(CriticalSectionEvent evento : events)
		{
			Object[] listeners = listenerList.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				((ICriticalSectionListener)listeners[i]).removeEvent(evento);
			}
			
		}
		events.clear();
	}
	public Object[] getElements(Object inputElement) {
		return events.toArray();
	}

	public void dispose() {
		// Nada
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Nada
	}

}
