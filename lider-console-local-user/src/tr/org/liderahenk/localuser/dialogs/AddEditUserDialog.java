package tr.org.liderahenk.localuser.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.localuser.constants.LocalUserConstants;
import tr.org.liderahenk.localuser.i18n.Messages;

/**
 * 
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Dogan</a>
 *
 */
public class AddEditUserDialog extends DefaultTaskDialog {
	
	private String title;
	private String username;
	private String home;
	private String isActive;
	private String groups;
	private String commandId;
	
	private ScrolledComposite sc;
	private Composite userGroupsComposite;
	
	private Text txtUsername;
	private Text txtNewUsername;
	private Text txtPassword;
	private Text txtHome;
	private Text txtGroup;
	private Button[] btnActive;
	
	public AddEditUserDialog(Shell parentShell, String dn, String title, String username, 
			String home, String isActive, String groups, String commandId) {
		super(parentShell, dn);
		this.title = title;
		this.username = username;
		this.home = home;
		this.isActive = isActive;
		this.groups = groups;
		this.commandId = commandId;
	}

	@Override
	public String createTitle() {
		return Messages.getString(title);
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {
		
		sc = new ScrolledComposite(parent, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 250;
		sc.setLayoutData(gridData);
		sc.setLayout(new GridLayout(1, false));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);
		
		Composite composite = new Composite(sc, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		
		sc.setContent(composite);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		
		Label username = new Label(composite, SWT.NONE);
		username.setText(Messages.getString("USERNAME"));
		
		txtUsername = new Text(composite, SWT.BORDER);
		GridData data =  new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		txtUsername.setLayoutData(data);
		
		if (this.username != null) {
			txtUsername.setText(this.username);
		}
		
		if (commandId.equals("EDIT_USER")) {
			txtUsername.setEnabled(false);
			Label newUsername = new Label(composite, SWT.NONE);
			newUsername.setText(Messages.getString("NEW_USERNAME"));
			
			txtNewUsername = new Text(composite, SWT.BORDER);
			txtNewUsername.setLayoutData(data);
			
			if (this.username != null) {
				txtNewUsername.setText(this.username);
			}
		}
		
		Label password = new Label(composite, SWT.NONE);
		password.setText(Messages.getString("PASSWORD"));
		
		txtPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		GridData passwordGridData =  new GridData();
		passwordGridData.horizontalAlignment = SWT.FILL;
		passwordGridData.grabExcessHorizontalSpace = true;
		txtPassword.setLayoutData(passwordGridData);
		
		if (commandId.equals("EDIT_USER")) {
			txtPassword.setToolTipText(Messages.getString("FILL_TO_CHANGE"));
		}
		
		Button btnShow = new Button(composite, SWT.CHECK);
		btnShow.setText(Messages.getString("SHOW"));
		btnShow.addSelectionListener(new SelectionAdapter() {
			
			@Override
		    public void widgetSelected(SelectionEvent e)
		    {
		        Button button = (Button) e.widget;
		        if (button.getSelection()) {
		        	txtPassword.setEchoChar('\0');
		        }
		        else {
		        	char ch = 0x25cf;
		        	txtPassword.setEchoChar(ch);
		        }
		    }
		});

		Label home = new Label(composite, SWT.NONE);
		home.setText(Messages.getString("USER_HOME"));

		txtHome = new Text(composite, SWT.BORDER);
		txtHome.setLayoutData(data);
		
		if (this.home != null) {
			txtHome.setText(this.home);
		}
		else {
			txtHome.setToolTipText(Messages.getString("SAMPLE_HOME"));
		}
		
		btnActive = new Button[2];
		
		btnActive[0] = new Button(composite, SWT.RADIO);
		btnActive[0].setText(Messages.getString("ACTIVE"));
		
		btnActive[1] = new Button(composite, SWT.RADIO);
		btnActive[1].setText(Messages.getString("PASSIVE"));
		
		if(Boolean.parseBoolean(isActive)) {
			btnActive[0].setSelection(true);
		}
		else {
			btnActive[1].setSelection(true);
		}
		
		Composite compGroups = new Composite(composite, SWT.NONE);
		compGroups.setLayout(new GridLayout(1, false));
		compGroups.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		
		Label groups = new Label(compGroups, SWT.NONE);
		groups.setText(Messages.getString("GROUP"));
		groups.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		userGroupsComposite = new Composite(compGroups, SWT.NONE);
		userGroupsComposite.setLayout(new GridLayout(2, false));
		userGroupsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createGroupEntry(userGroupsComposite);
		
		Button btnAdd = new Button(userGroupsComposite, SWT.PUSH);
		btnAdd.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/add.png"));
		btnAdd.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAddGroupButton(e);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		if (this.groups != null) {
			String[] groupList = this.groups.split(",");
			for (int i = 0; i < groupList.length; i++) {
				if(i > 1) {
					createGroupEntry(userGroupsComposite);

					Button btnRemoveGroup = new Button(userGroupsComposite, SWT.NONE);
					btnRemoveGroup
							.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/remove.png"));
					btnRemoveGroup.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							handleRemoveGroupButton(e);
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});

					redraw();
				}
				
				if(i > 0) {
					txtGroup.setText(groupList[i]);
				}
			}
		}

		return null;
	}
	
	protected void handleAddGroupButton(SelectionEvent e) {

		Composite parent = (Composite) ((Button) e.getSource()).getParent();
		
		createGroupEntry(parent);

		Button btnRemoveGroup = new Button(parent, SWT.NONE);
		btnRemoveGroup
				.setImage(SWTResourceManager.getImage(LiderConstants.PLUGIN_IDS.LIDER_CONSOLE_CORE, "icons/16/remove.png"));
		btnRemoveGroup.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemoveGroupButton(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		redraw();
	}
	
	private void createGroupEntry(Composite parent) {
		
		Group grpGroupEntry = new Group(parent, SWT.NONE);
		grpGroupEntry.setLayout(new GridLayout(1, false));
		grpGroupEntry.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		txtGroup = new Text(grpGroupEntry, SWT.BORDER);
		GridData groupsData =  new GridData();
		groupsData.horizontalAlignment = SWT.FILL;
		groupsData.grabExcessHorizontalSpace = true;
		txtGroup.setLayoutData(groupsData);
	}

	
	protected void handleRemoveGroupButton(SelectionEvent e) {
		Button thisBtn = (Button) e.getSource();
		Composite parent = thisBtn.getParent();
		Control[] children = parent.getChildren();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				if (children[i].equals(thisBtn) && i - 1 > 0) {
					children[i - 1].dispose();
					children[i].dispose();
					redraw();
					break;
				}
			}
		}
	}
	
	private void redraw() {
		sc.layout(true, true);
		sc.setMinSize(sc.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
	}
	
	private String[] list() {

		List<String> groups = new ArrayList<String>();

		Control[] children = userGroupsComposite.getChildren();
		if (children != null) {
			for (Control child : children) {
				if (child instanceof Group) {
					Control[] gChildren = ((Group) child).getChildren();
					if (gChildren != null && gChildren.length == 1) {
						if (((Text) gChildren[0]).getText() != null && !((Text) gChildren[0]).getText().isEmpty()) {
							groups.add(((Text) gChildren[0]).getText());
						}
					}
				}
			}
		}

		return groups.toArray(new String[] {});
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
		
		if(txtUsername.getText().isEmpty() || txtHome.getText().isEmpty()) {
			throw new ValidationException(Messages.getString("FILL_SOME_FIELDS"));
		}
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(LocalUserConstants.PARAMETERS.USERNAME, txtUsername.getText());
		
		if (txtNewUsername != null && txtNewUsername.getText() != null) {
			parameterMap.put(LocalUserConstants.PARAMETERS.NEW_USERNAME, txtNewUsername.getText());
		}
		
		if (txtPassword.getText() != null) {
			parameterMap.put(LocalUserConstants.PARAMETERS.PASSWORD, txtPassword.getText());
		}
		parameterMap.put(LocalUserConstants.PARAMETERS.HOME, txtHome.getText());
		parameterMap.put(LocalUserConstants.PARAMETERS.ACTIVE, String.valueOf(btnActive[0].getSelection()));
		
		String strList = Arrays.toString(list());               
		strList = strList.substring(1, strList.length()-1).replaceAll(" ", "");
		
		parameterMap.put(LocalUserConstants.PARAMETERS.GROUPS, strList);
		
		return parameterMap;
	}

	@Override
	public String getCommandId() {
		return commandId;
	}

	@Override
	public String getPluginName() {
		return LocalUserConstants.PLUGIN_NAME;
	}

	@Override
	public String getPluginVersion() {
		return LocalUserConstants.PLUGIN_VERSION;
	}

}
