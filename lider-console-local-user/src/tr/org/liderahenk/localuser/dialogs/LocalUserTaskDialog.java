package tr.org.liderahenk.localuser.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.ldap.enums.DNType;
import tr.org.liderahenk.liderconsole.core.rest.requests.TaskRequest;
import tr.org.liderahenk.liderconsole.core.rest.utils.TaskRestUtils;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;
import tr.org.liderahenk.localuser.constants.LocalUserConstants;
import tr.org.liderahenk.localuser.i18n.Messages;

/**
 * 
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Dogan</a>
 *
 */
public class LocalUserTaskDialog extends DefaultTaskDialog {

	private static final Logger logger = LoggerFactory.getLogger(LocalUserTaskDialog.class);

	private String dn;

	private TableViewer viewer;

	private Button btnAdd;
	private Button btnEdit;
	private Button btnDelete;

	private String user;
	private String groups;
	private String home;
	private boolean isActive;
	private boolean isDesktopWritePermissionExists;
	private boolean isKioskModeOn;

	private Map<String, String> homeMap = new HashMap<String, String>();
	private final String[] columnTitles = new String[] { "USER", "GROUP", "HOME", "IS_ACTIVE",
			"DESKTOP_WRITE_PERMISSION", "KIOSK_MODE" };

	public LocalUserTaskDialog(Shell parentShell, String dn) {
		super(parentShell, dn);
		this.dn = dn;
		subscribeEventHandler(eventHandler);
		getData();
	}

	@Override
	public String createTitle() {
		return Messages.getString("LOCAL_USERS");
	}

	private EventHandler eventHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("LOCAL_USER", 100);
					try {
						TaskStatusNotification taskStatus = (TaskStatusNotification) event
								.getProperty("org.eclipse.e4.data");
						byte[] data = taskStatus.getResult().getResponseData();

						final Map<String, Object> responseData = new ObjectMapper().readValue(data, 0, data.length,
								new TypeReference<HashMap<String, Object>>() {
								});
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								if (responseData != null && !responseData.isEmpty()
										&& responseData.containsKey("users")) {

									@SuppressWarnings({ "unchecked" })
									List<Map<String, Object>> usersList = (List<Map<String, Object>>) responseData
											.get("users");
									
									for (Map<String, Object> userMap : usersList) {
										user = (String) userMap.get("user");
										groups = (String) userMap.get("groups");
										home = (String) userMap.get("home");
										isActive = Boolean.parseBoolean((String) userMap.get("is_active"));
										isDesktopWritePermissionExists = Boolean.parseBoolean(
												(String) userMap.get("is_desktop_write_permission_exists"));
										isKioskModeOn = Boolean.parseBoolean((String) userMap.get("is_kiosk_mode_on"));

										TableItem item = new TableItem(viewer.getTable(), SWT.NONE);
										item.setText(0, user);
										item.setText(1, groups);
										item.setText(2, home);
										homeMap.put(user, home);

										if (isActive) {
											item.setImage(3, new Image(Display.getCurrent(),
													this.getClass().getResourceAsStream("/icons/16/ok.png")));
											item.setText(3, Messages.getString("TRUE"));
										} else {
											item.setImage(3, new Image(Display.getCurrent(),
													this.getClass().getResourceAsStream("/icons/16/cancel.png")));
											item.setText(3, Messages.getString("FALSE"));
										}

										if (isDesktopWritePermissionExists) {
											item.setImage(4, new Image(Display.getCurrent(),
													this.getClass().getResourceAsStream("/icons/16/ok.png")));
											item.setText(4, Messages.getString("TRUE"));
										} else {
											item.setImage(4, new Image(Display.getCurrent(),
													this.getClass().getResourceAsStream("/icons/16/cancel.png")));
											item.setText(4, Messages.getString("FALSE"));
										}

										if (isKioskModeOn) {
											item.setImage(5, new Image(Display.getCurrent(),
													this.getClass().getResourceAsStream("/icons/16/ok.png")));
											item.setText(5, Messages.getString("TRUE"));
										} else {
											item.setImage(5, new Image(Display.getCurrent(),
													this.getClass().getResourceAsStream("/icons/16/cancel.png")));
											item.setText(5, Messages.getString("FALSE"));
										}
									}
								}
							}
						});
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						Notifier.error("", Messages.getString("UNEXPECTED_ERROR_WHEN_GET_USERS"));
					}
					monitor.worked(100);
					monitor.done();

					return Status.OK_STATUS;
				}
			};

			job.setUser(true);
			job.schedule();
		}
	};

	private void getData() {
		try {
			TaskRequest task = new TaskRequest(new ArrayList<String>(getDnSet()), DNType.AHENK, getPluginName(),
					getPluginVersion(), getCommandId(), null, null, null, new Date());
			TaskRestUtils.execute(task);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("EXIT"), true);
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(5, false));

		GridData gData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gData.widthHint = 850;
		gData.heightHint = 500;
		composite.setLayoutData(gData);

		btnAdd = new Button(composite, SWT.PUSH);
		btnAdd.setText(Messages.getString("ADD"));
		btnAdd.setImage(new Image(Display.getCurrent(), this.getClass().getResourceAsStream("/icons/16/add.png")));
		btnAdd.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				AddEditUserDialog dialog = new AddEditUserDialog(Display.getDefault().getActiveShell(), dn, "ADD_USER",
						null, null, "false", "false", "false", null, "ADD_USER", homeMap);
				dialog.create();
				dialog.open();

				viewer.getTable().clearAll();
				viewer.getTable().setItemCount(0);
				getData();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnDelete = new Button(composite, SWT.PUSH);
		btnDelete.setText(Messages.getString("DELETE"));
		btnDelete
				.setImage(new Image(Display.getCurrent(), this.getClass().getResourceAsStream("/icons/16/delete.png")));
		btnDelete.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem item = viewer.getTable().getItem(viewer.getTable().getSelectionIndex());
				try {
					DeleteHomeQuestionDialog questionDialog = new DeleteHomeQuestionDialog(
							Display.getDefault().getActiveShell(), item.getText(0), item.getText(2), getDnSet());
					questionDialog.open();

				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
					Notifier.error(null, Messages.getString("ERROR_ON_EXECUTE"));
				}
				viewer.getTable().clearAll();
				viewer.getTable().setItemCount(0);
				getData();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnEdit = new Button(composite, SWT.PUSH);
		btnEdit.setText(Messages.getString("EDIT"));
		btnEdit.setImage(new Image(Display.getCurrent(), this.getClass().getResourceAsStream("/icons/16/edit.png")));
		btnEdit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = viewer.getTable().getSelectionIndex();
				if (index < 0) {
					Notifier.warning("", Messages.getString("SELECT_ONE_ITEM"));
				} else {
					TableItem tableItem = viewer.getTable().getItem(index);

					String isActive = "false";
					if (tableItem.getText(3).equals(Messages.getString("TRUE"))) {
						isActive = "true";
					} else if (tableItem.getText(3).equals(Messages.getString("FALSE"))) {
						isActive = "false";
					}

					String isDesktopWritePermissionExists = "false";
					if (tableItem.getText(4).equals(Messages.getString("TRUE"))) {
						isDesktopWritePermissionExists = "true";
					} else if (tableItem.getText(4).equals(Messages.getString("FALSE"))) {
						isDesktopWritePermissionExists = "false";
					}

					String isKioskModeOn = "false";
					if (tableItem.getText(5).equals(Messages.getString("TRUE"))) {
						isKioskModeOn = "true";
					} else if (tableItem.getText(5).equals(Messages.getString("FALSE"))) {
						isKioskModeOn = "false";
					}

					AddEditUserDialog dialog = new AddEditUserDialog(Display.getDefault().getActiveShell(), dn,
							"EDIT_USER", tableItem.getText(0), tableItem.getText(2), isActive,
							isDesktopWritePermissionExists, isKioskModeOn, tableItem.getText(1), "EDIT_USER", homeMap);
					dialog.create();
					dialog.open();

					viewer.getTable().clearAll();
					viewer.getTable().setItemCount(0);
					getData();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		viewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {

				int index = viewer.getTable().getSelectionIndex();
				TableItem tableItem = viewer.getTable().getItem(index);

				String isActive = "false";
				if (tableItem.getText(3).equals(Messages.getString("TRUE"))) {
					isActive = "true";
				} else if (tableItem.getText(3).equals(Messages.getString("FALSE"))) {
					isActive = "false";
				}

				String isDesktopWritePermissionExists = "false";
				if (tableItem.getText(4).equals(Messages.getString("TRUE"))) {
					isDesktopWritePermissionExists = "true";
				} else if (tableItem.getText(4).equals(Messages.getString("FALSE"))) {
					isDesktopWritePermissionExists = "false";
				}

				String isKioskModeOn = "false";
				if (tableItem.getText(5).equals(Messages.getString("TRUE"))) {
					isKioskModeOn = "true";
				} else if (tableItem.getText(5).equals(Messages.getString("FALSE"))) {
					isKioskModeOn = "false";
				}

				AddEditUserDialog dialog = new AddEditUserDialog(Display.getDefault().getActiveShell(), dn, "EDIT_USER",
						tableItem.getText(0), tableItem.getText(2), isActive, isDesktopWritePermissionExists,
						isKioskModeOn, tableItem.getText(1), "EDIT_USER", homeMap);
				dialog.create();
				dialog.open();

				viewer.getTable().clearAll();
				viewer.getTable().setItemCount(0);
				getData();
			}
		});

		createColumns(composite, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// define layout for the viewer
		GridData gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.heightHint = 300;
		viewer.getControl().setLayoutData(gridData);

		return null;
	}

	// create the columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer) {
		int[] bounds = { 120, 120, 120, 120, 150, 120 };

		for (int i = 0; i < columnTitles.length; i++) {
			createTableViewerColumn(Messages.getString(columnTitles[i]), bounds[i], i);
		}
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
	}

	@Override
	public Map<String, Object> getParameterMap() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getCommandId() {
		return "GET_USERS";
	}

	@Override
	public String getPluginName() {
		return LocalUserConstants.PLUGIN_NAME;
	}

	@Override
	public String getPluginVersion() {
		return LocalUserConstants.PLUGIN_VERSION;
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void setViewer(TableViewer viewer) {
		this.viewer = viewer;
	}
}
