package collector.desktop.controller.interfaces;

import collector.desktop.controller.interfaces.UIObserver;

public interface UIObservable {
	public void registerObserver(UIObserver observer);
	public void unregisterObserver(UIObserver observer);
	public void unregisterAllObservers();
	public void notifyObservers();
}
