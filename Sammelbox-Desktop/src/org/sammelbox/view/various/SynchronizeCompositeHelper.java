/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
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

package org.sammelbox.view.various;

import java.util.HashMap;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public final class SynchronizeCompositeHelper {	
	private static HashMap<SynchronizeStep, Label> synchronizeStepsToLabelsMap = null;
	
	private SynchronizeCompositeHelper() {}
	
	public static void storeSynchronizeCompositeLabels(HashMap<SynchronizeStep, Label> syncronizeStepsToLabelsMap) {
		SynchronizeCompositeHelper.synchronizeStepsToLabelsMap = syncronizeStepsToLabelsMap;
	}
	
	public static void enabledSynchronizeStep(final SynchronizeStep synchronizeStep) {
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		    	((Label) synchronizeStepsToLabelsMap.get(synchronizeStep).getAccessible().getControl()).setEnabled(true);
		    }
		});
	}
	
	public static void disableSynchronizeStep(final SynchronizeStep synchronizeStep) {
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		    	((Label) synchronizeStepsToLabelsMap.get(synchronizeStep).getAccessible().getControl()).setEnabled(false);
		    }
		});
	}
}
