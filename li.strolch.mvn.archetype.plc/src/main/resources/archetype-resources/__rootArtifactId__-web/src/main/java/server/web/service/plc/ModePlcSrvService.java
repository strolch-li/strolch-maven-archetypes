package ${package}.server.web.service.plc;

import static ${package}.plc.shared.SharedPlcConstants.*;
import static li.strolch.utils.helper.ExceptionHelper.getCallerMethod;

import li.strolch.model.Resource;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.plc.gw.server.PlcGwServerHandler;
import li.strolch.plc.gw.server.PlcGwService;
import li.strolch.plc.model.PlcAddressKey;

public class ModePlcSrvService extends PlcGwService {

	public ModePlcSrvService(String plcId, PlcGwServerHandler plcHandler) {
		super(plcId, plcHandler);
	}

	@Override
	public void handleNotification(PlcAddressKey addressKey, Object value) {
		run(ctx -> {
			try (StrolchTransaction tx = openTx(ctx, getCallerMethod(), false)) {

				Resource configuration = tx.getConfiguration();

				switch (addressKey.action) {
				case A_ON -> configuration.setBoolean(PARAM_AUTOMATIC_MODE, (boolean) value);
				case STARTED -> configuration.setBoolean(PARAM_PLC_STARTED, true);
				case STOPPED -> configuration.setBoolean(PARAM_PLC_STARTED, false);
				}

				logger.info(addressKey + " = " + value);
				tx.update(configuration);
				tx.commitOnClose();
			}
		});
	}

	@Override
	public void register() {
		register(R_AUTOMATIC_MODE, A_ON);
		register(PLC, STARTED);
		register(PLC, STOPPED);
		super.register();
	}

	@Override
	public void unregister() {
		unregister(R_AUTOMATIC_MODE, A_ON);
		unregister(PLC, STARTED);
		unregister(PLC, STOPPED);
		super.unregister();
	}
}
