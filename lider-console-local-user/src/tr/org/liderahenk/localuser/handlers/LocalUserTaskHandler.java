package tr.org.liderahenk.localuser.handlers;

import org.eclipse.swt.widgets.Display;

import tr.org.liderahenk.liderconsole.core.handlers.SingleSelectionHandler;
import tr.org.liderahenk.localuser.dialogs.LocalUserTaskDialog;

public class LocalUserTaskHandler extends SingleSelectionHandler {
	
	@Override
	public void executeWithDn(String dn) {
		LocalUserTaskDialog dialog = new LocalUserTaskDialog(Display.getDefault().getActiveShell(), dn);
		dialog.create();
		dialog.open();
	}}
