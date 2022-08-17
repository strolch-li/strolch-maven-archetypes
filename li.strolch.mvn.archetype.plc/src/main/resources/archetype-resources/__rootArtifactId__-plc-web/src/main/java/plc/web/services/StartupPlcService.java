package ${package}.plc.web.services;

import static ${package}.plc.shared.SharedPlcConstants.*;

import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.plc.core.PlcHandler;
import li.strolch.plc.core.PlcService;

public class StartupPlcService extends PlcService {

	public StartupPlcService(PlcHandler plcHandler) {
		super(plcHandler);
	}

	@Override
	public void start(StrolchTransaction tx) {
		send(PLC, STARTED);
		super.start(tx);
	}

	@Override
	public void stop() {
		send(PLC, STOPPED);
		super.stop();
	}
}
