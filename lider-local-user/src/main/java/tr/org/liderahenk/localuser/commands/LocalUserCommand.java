package tr.org.liderahenk.localuser.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.lider.core.api.log.IOperationLogService;
import tr.org.liderahenk.lider.core.api.persistence.IPluginDbService;
import tr.org.liderahenk.lider.core.api.rest.requests.ITaskRequest;
import tr.org.liderahenk.lider.core.api.plugin.IPluginInfo;
import tr.org.liderahenk.lider.core.api.plugin.ICommand;
import tr.org.liderahenk.lider.core.api.service.ICommandContext;
import tr.org.liderahenk.lider.core.api.service.ICommandResult;
import tr.org.liderahenk.lider.core.api.service.ICommandResultFactory;
import tr.org.liderahenk.lider.core.api.service.enums.CommandResultStatus;

public class LocalUserCommand implements ICommand {

	private Logger logger = LoggerFactory.getLogger(LocalUserCommand.class);
	
	private ICommandResultFactory resultFactory;
	private IPluginInfo pluginInfo;
	private IOperationLogService logService;
	private IPluginDbService pluginDbService;

	@Override
	public ICommandResult execute(ICommandContext context) {
		
		// TODO Modify parameter map before sending it to agent(s).
		ITaskRequest req = context.getRequest();
		Map<String, Object> parameterMap = req.getParameterMap();
		parameterMap.put("dummy-param", "dummy-param-value");
		
		logger.debug("Parameter map updated.");
		
		// TODO Modify entity objects related to plugin command via DB service
		//Object entity = new Object();
		//pluginDbService.save(entity);
		logger.debug("Entity saved successfully.");
		
		// TODO Modify result map to provide additional parameters or info before sending it back to console.
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("dummy-param", "dummy-param-value");
		
		logger.debug("Executed command, returning result.");
		ICommandResult commandResult = resultFactory.create(CommandResultStatus.OK, new ArrayList<String>(), this, resultMap);

		return commandResult;
	}

	@Override
	public ICommandResult validate(ICommandContext context) {
		// TODO Validate before command execution
		return resultFactory.create(CommandResultStatus.OK, null, this, null);
	}

	@Override
	public String getCommandId() {
		// TODO Unique command ID used to match incoming REST requests to this Command class.
		return "SAMPLE_COMMAND1";
	}

	@Override
	public Boolean executeOnAgent() {
		// TODO True if we need to send a task to agent(s), false otherwise.
		return true;
	}
	
	@Override
	public String getPluginName() {
		return pluginInfo.getPluginName();
	}

	@Override
	public String getPluginVersion() {
		return pluginInfo.getPluginVersion();
	}

	public void setResultFactory(ICommandResultFactory resultFactory) {
		this.resultFactory = resultFactory;
	}
	
	public void setPluginInfo(IPluginInfo pluginInfo) {
		this.pluginInfo = pluginInfo;
	}

	public void setLogService(IOperationLogService logService) {
		this.logService = logService;
	}

	public void setPluginDbService(IPluginDbService pluginDbService) {
		this.pluginDbService = pluginDbService;
	}
	
}
