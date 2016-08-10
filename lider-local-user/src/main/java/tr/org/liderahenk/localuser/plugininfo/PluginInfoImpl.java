package tr.org.liderahenk.localuser.plugininfo;

import tr.org.liderahenk.lider.core.api.plugin.BasePluginInfo;

public class PluginInfoImpl extends BasePluginInfo {
	
	private String pluginName;

	private String pluginVersion;

	private String description;

	private Boolean machineOriented;

	private Boolean userOriented;

	private Boolean policyPlugin;
	
	private Boolean taskPlugin;

	private Boolean usesFileTransfer;

	private Boolean xbased;
	
	@Override
	public String toString() {
		return "PluginInfoImpl [pluginName=" + pluginName + ", pluginVersion=" + pluginVersion + ", description="
				+ description + ", machineOriented=" + machineOriented + ", userOriented=" + userOriented
				+ ", policyPlugin=" + policyPlugin + ", taskPlugin=" + taskPlugin + ", usesFileTransfer="
				+ usesFileTransfer + ", xbased=" + xbased + "]";
	}
	
	@Override
	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	@Override
	public String getPluginVersion() {
		return pluginVersion;
	}

	public void setPluginVersion(String pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Boolean getMachineOriented() {
		return machineOriented;
	}

	public void setMachineOriented(Boolean machineOriented) {
		this.machineOriented = machineOriented;
	}

	@Override
	public Boolean getUserOriented() {
		return userOriented;
	}

	public void setUserOriented(Boolean userOriented) {
		this.userOriented = userOriented;
	}

	@Override
	public Boolean getPolicyPlugin() {
		return policyPlugin;
	}

	public void setPolicyPlugin(Boolean policyPlugin) {
		this.policyPlugin = policyPlugin;
	}

	@Override
	public Boolean getXbased() {
		return xbased;
	}

	public void setXbased(Boolean xbased) {
		this.xbased = xbased;
	}
	
	@Override
	public Boolean getTaskPlugin() {
		return taskPlugin;
	}

	public void setTaskPlugin(Boolean taskPlugin) {
		this.taskPlugin = taskPlugin;
	}

	@Override
	public Boolean getUsesFileTransfer() {
		return usesFileTransfer;
	}

	public void setUsesFileTransfer(Boolean usesFileTransfer) {
		this.usesFileTransfer = usesFileTransfer;
	}

}