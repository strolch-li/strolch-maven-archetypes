package ${package}.plc.web.components;

import java.util.ArrayList;
import java.util.List;

import ${package}.plc.web.services.StartupPlcService;
import li.strolch.agent.api.ComponentContainer;
import li.strolch.plc.core.PlcHandler;
import li.strolch.plc.core.PlcService;
import li.strolch.plc.core.PlcServiceInitializer;

public class CustomPlcServiceInitializer extends PlcServiceInitializer {

	public CustomPlcServiceInitializer(ComponentContainer container, String componentName) {
		super(container, componentName);
	}

	@Override
	protected List<PlcService> getPlcServices(PlcHandler plcHandler) {
		ArrayList<PlcService> plcServices = new ArrayList<>();

		plcServices.add(new StartupPlcService(plcHandler));

		return plcServices;
	}
}
