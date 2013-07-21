package collector.desktop.controller.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventObservable {
	private final static Logger LOGGER = LoggerFactory.getLogger(EventObservable.class);
	
	private static List<Observer> observers = new ArrayList<Observer>();
	private static Queue<SammelboxEvent> sammelboxEvents = new LinkedList<SammelboxEvent>();
	
	private EventObservable() {
	}
	
	public static SammelboxEvent peekHeadEventInQueue() {
		return sammelboxEvents.peek();
	}
	
	public static void addEventToQueue(SammelboxEvent sammmelboxEvent) {		
		if (sammelboxEvents.size() > 1) {
			LOGGER.warn(sammelboxEvents.toString() + "\n There is more than one unprocessed event in the sammelbox event queue. " +
					"(" + sammelboxEvents.size() + " events) This is probably due to an error " +
							"since this is currently not supported!");
		}
		
		sammelboxEvents.add(sammmelboxEvent);
		notifyObservers();
	}
	
	public static void registerObserver(Observer observer) {
		observers.add(observer);
	}

	public static void unregisterObserver(Observer observer) {
		observers.remove(observer);
	}

	public static void unregisterAllObservers() {
		observers.clear();
	}

	public static void notifyObservers() {		
		SammelboxEvent event = sammelboxEvents.poll();
		
		for (Observer observer : observers) {
			observer.update(event);
		}
	}
}
