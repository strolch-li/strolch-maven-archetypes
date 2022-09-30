package ${package}.server.web.service;

import static ${package}.plc.shared.SharedPlcConstants.*;
import static li.strolch.plc.model.PlcConstants.PARAM_PLC_ID;

import java.util.Optional;

import com.google.gson.JsonObject;
import li.strolch.model.Resource;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.plc.gw.server.PlcGwServerHandler;
import li.strolch.plc.gw.server.PlcGwSrvI18n;
import li.strolch.plc.gw.server.service.SendPlcTelegramCommand;
import li.strolch.plc.model.PlcAddressResponse;
import li.strolch.plc.model.PlcConstants;
import li.strolch.privilege.model.SimpleRestrictable;
import li.strolch.service.JsonServiceArgument;
import li.strolch.service.api.AbstractService;
import li.strolch.service.api.ServiceResult;
import li.strolch.utils.dbc.DBC;

public class ShopFloorActionService extends AbstractService<JsonServiceArgument, ServiceResult> {

	@Override
	protected ServiceResult getResultInstance() {
		return new ServiceResult();
	}

	@Override
	public JsonServiceArgument getArgumentInstance() {
		return new JsonServiceArgument();
	}

	@Override
	protected ServiceResult internalDoService(JsonServiceArgument arg) {

		JsonObject data = arg.jsonElement.getAsJsonObject();
		String action = data.get(PlcConstants.PARAM_ACTION).getAsString();
		DBC.PRE.assertNotEmpty("action must be set!", action);

		Optional<ServiceResult> result;
		try (StrolchTransaction tx = openArgOrUserTx(arg, false)) {

			// validate that the user can do this action
			tx.validateAction(new SimpleRestrictable(getPrivilegeValue(), action));

			switch (action) {
			case "EnableAutomaticMode" -> result = setAutomaticMode(tx, A_ON);
			case "DisableAutomaticMode" -> result = setAutomaticMode(tx, A_OFF);
			case "StopAll" -> result = stopAll(tx);
			default -> {
				tx.rollbackOnClose();
				throw new IllegalArgumentException("Unexpected action " + action);
			}
			}

			if (result.isPresent()) {
				tx.rollbackOnClose();
			} else if (tx.needsCommit()) {
				tx.commitOnClose();
			}
		}

		return result.orElseGet(ServiceResult::success);
	}

	private String getPlcId(StrolchTransaction tx) {
		return tx.getConfiguration().getString(PARAM_PLC_ID);
	}

	protected Optional<ServiceResult> stopAll(StrolchTransaction tx) {
		return assertPlcConnected(tx).or(() -> sendPlcTelegram(tx, R_STOP_ALL_ACTORS, A_NOW));
	}

	protected Optional<ServiceResult> setAutomaticMode(StrolchTransaction tx, String action) {
		return assertPlcConnected(tx) //
				.or(() -> sendPlcTelegram(tx, R_AUTOMATIC_MODE, action)) //
				.or(() -> {
					Resource configuration = tx.getConfiguration();
					configuration.setBoolean(PARAM_AUTOMATIC_MODE, action.equals(A_ON));
					tx.update(configuration);
					return Optional.empty();
				});
	}

	protected Optional<ServiceResult> assertPlcConnected(StrolchTransaction tx) {
		PlcGwServerHandler plcHandler = getComponent(PlcGwServerHandler.class);
		if (!plcHandler.isPlcConnected(getPlcId(tx)))
			return Optional.of(ServiceResult.error("PLC " + getPlcId(tx) + " is not connected!")
					.i18n(PlcGwSrvI18n.bundle, "execution.plc.notConnected", "plc", getPlcId(tx)));

		return Optional.empty();
	}

	public static Optional<ServiceResult> sendPlcTelegram(StrolchTransaction tx, String resource, String action) {

		SendPlcTelegramCommand cmd = new SendPlcTelegramCommand(tx);
		cmd.setPlcId(tx.getConfiguration().getString(PARAM_PLC_ID));
		cmd.setResource(resource);
		cmd.setAction(action);

		cmd.validate();
		cmd.doCommand();

		PlcAddressResponse response = cmd.getResponse();
		if (response.isFailed())
			return Optional.of(ServiceResult.error(response.getState() + ": " + response.getStateMsg()));

		return Optional.empty();
	}
}
