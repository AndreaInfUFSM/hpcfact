package br.ufsm.inf.viewCriticalSection.views;

public interface ICriticalSectionListener {

	public void handleEvent(final CriticalSectionEvent event);
	
	public void removeEvent(CriticalSectionEvent evento);

}
