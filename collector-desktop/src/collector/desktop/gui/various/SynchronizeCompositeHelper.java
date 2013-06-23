package collector.desktop.gui.various;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.widgets.Label;


public class SynchronizeCompositeHelper implements Observer {
	private HashMap<SynchronizeStep, Label> synchronizeStepsToLabelsMap = null;
	
	public void storeSynchronizeCompositeLabels(HashMap<SynchronizeStep, Label> syncronizeStepsToLabelsMap) {
		this.synchronizeStepsToLabelsMap = syncronizeStepsToLabelsMap;
	}
	
	private void enabledSynchronizeStep(SynchronizeStep synchronizeStep) {
		((Label) synchronizeStepsToLabelsMap.get(synchronizeStep).getAccessible().getControl()).setEnabled(true);
		//syncronizeStepsToLabelsMap.get(synchronizeStep).setEnabled(true);
	}
	
	private void disableSynchronizeStep(SynchronizeStep synchronizeStep) {
		((Label) synchronizeStepsToLabelsMap.get(synchronizeStep).getAccessible().getControl()).setEnabled(false);
		//.setEnabled(false);
	}

	@Override
	public void update(Observable observable, Object object) {
		disableSynchronizeStep(SynchronizeStep.ESTABLISH_CONNECTION);
		enabledSynchronizeStep(SynchronizeStep.UPLOAD_DATA);
	}
}
