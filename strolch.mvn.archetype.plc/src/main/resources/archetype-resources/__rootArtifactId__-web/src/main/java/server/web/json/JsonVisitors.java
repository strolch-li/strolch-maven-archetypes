package ${package}.server.web.json;

import static ${package}.plc.shared.SharedPlcConstants.*;
import static li.strolch.plc.model.PlcConstants.PARAM_PLC_ID;

import com.google.gson.JsonObject;
import li.strolch.model.Resource;
import li.strolch.model.json.StrolchRootElementToJsonVisitor;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.plc.gw.server.PlcGwServerHandler;

public class JsonVisitors {

	public static StrolchRootElementToJsonVisitor toJson() {
		return new StrolchRootElementToJsonVisitor().withoutPolicies();
	}

	public static StrolchRootElementToJsonVisitor flatToJson() {
		return toJson().withoutVersion().flat();
	}

	public static JsonObject shopFloorToJson(StrolchTransaction tx) {

		Resource configuration = tx.getConfiguration();
		PlcGwServerHandler plcHandler = tx.getContainer().getComponent(PlcGwServerHandler.class);

		JsonObject data = new JsonObject();
		data.addProperty(PARAM_AUTOMATIC_MODE, configuration.getBoolean(PARAM_AUTOMATIC_MODE));
		data.addProperty(PARAM_PLC_STARTED, configuration.getBoolean(PARAM_PLC_STARTED));
		data.addProperty(PARAM_PLC_CONNECTED, plcHandler.isPlcConnected(tx.getConfiguration().getString(PARAM_PLC_ID)));

		return data;
	}
}
