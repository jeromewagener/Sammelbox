package collector.desktop.interfaces;

public interface UIObservable {
	public void registerObserver(UIObserver observer);
	public void unregisterObserver(UIObserver observer);
	public void unregisterAllObservers();
	public void notifyObservers();
}
