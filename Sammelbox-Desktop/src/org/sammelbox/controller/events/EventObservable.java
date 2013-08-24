/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package org.sammelbox.controller.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EventObservable {
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
