package tr.org.liderahenk.localuser.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.localuser.constants.LocalUserConstants;
import tr.org.liderahenk.localuser.i18n.Messages;

/**
 * 
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Dogan</a>
 *
 */
public class DeleteHomeQuestionDialog extends Dialog {

	private Set<String> dnSet;
	private String username;
	private String home;

	private Label message;
	private Button btnYes;
	private Button btnNo;
	private boolean delete;

	public DeleteHomeQuestionDialog(Shell parentShell, String username, String home, Set<String> dnSet) {
		super(parentShell);
		this.username = username;
		this.home = home;
		this.dnSet = dnSet;
	}

	@Override
	protected Control createContents(Composite parent) {

		// Disable ESC key in this dialog
		getShell().addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					e.doit = false;
				}
			}
		});

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label image = new Label(composite, SWT.NONE);
		image.setImage(new Image(Display.getCurrent(), this.getClass().getResourceAsStream("/icons/64/warning.png")));

		message = new Label(composite, SWT.WRAP);
		message.setText(Messages.getString("DELETE_HOME"));
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		message.setLayoutData(gridData);

		delete = false;

		Composite cmpButtons = new Composite(composite, SWT.NONE);
		cmpButtons.setLayout(new GridLayout(2, true));
		cmpButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true, 2, 1));

		btnYes = new Button(cmpButtons, SWT.PUSH);
		btnYes.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true));
		btnYes.setText(Messages.getString("TRUE"));
		btnYes.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				delete = true;
				try {
					sendTask();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				Display.getDefault().getActiveShell().close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnNo = new Button(cmpButtons, SWT.PUSH);
		btnNo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true));
		btnNo.setText(Messages.getString("FALSE"));
		btnNo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				delete = false;
				try {
					sendTask();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				Display.getDefault().getActiveShell().close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		return composite;
	}

	public Map<String, Object> getParameterMap() {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(LocalUserConstants.PARAMETERS.USERNAME, username);
		parameterMap.put(LocalUserConstants.PARAMETERS.HOME, home);
		parameterMap.put(LocalUserConstants.PARAMETERS.DELETE_HOME, delete);
		return parameterMap;
	}

	private void sendTask() throws Exception {
		TaskRequest task = new TaskRequest(new ArrayList<String>(dnSet), DNType.AHENK, LocalUserConstants.PLUGIN_NAME,
				LocalUserConstants.PLUGIN_VERSION, "DELETE_USER", getParameterMap(), null, null, new Date());
		TaskRestUtils.execute(task);
	}

}
