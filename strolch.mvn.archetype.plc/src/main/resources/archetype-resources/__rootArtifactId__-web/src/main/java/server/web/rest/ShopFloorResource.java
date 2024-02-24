package ${package}.server.web.rest;

import static ${package}.plc.shared.SharedPlcConstants.ROLE_SHOP_FLOOR;
import static ${package}.server.web.json.JsonVisitors.shopFloorToJson;
import static li.strolch.model.Tags.Json.DATA;
import static li.strolch.plc.model.PlcConstants.PARAM_PLC_ID;
import static li.strolch.rest.StrolchRestfulConstants.STROLCH_CERTIFICATE;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ${package}.server.web.service.ShopFloorActionService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.privilege.model.Certificate;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.helper.ResponseUtil;
import li.strolch.service.JsonServiceArgument;
import li.strolch.service.api.ServiceHandler;

@Path("shopFloor")
public class ShopFloorResource {

	private static String getContext() {
		StackTraceElement element = new Throwable().getStackTrace()[2];
		return element.getClassName() + "." + element.getMethodName();
	}

	private StrolchTransaction openTx(Certificate certificate) {
		return RestfulStrolchComponent.getInstance().openTx(certificate, getContext());
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getState(@Context HttpServletRequest request) {
		Certificate cert = (Certificate) request.getAttribute(STROLCH_CERTIFICATE);

		try (StrolchTransaction tx = openTx(cert)) {
			tx.assertHasRole(ROLE_SHOP_FLOOR);
			return ResponseUtil.toResponse(DATA, shopFloorToJson(tx));
		}
	}

	@PUT
	@Path("action")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doAction(@Context HttpServletRequest request, String data) {
		Certificate cert = (Certificate) request.getAttribute(STROLCH_CERTIFICATE);

		JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
		try (StrolchTransaction tx = openTx(cert)) {
			jsonObject.addProperty(PARAM_PLC_ID, tx.getConfiguration().getString(PARAM_PLC_ID));
		}

		ShopFloorActionService svc = new ShopFloorActionService();
		JsonServiceArgument arg = svc.getArgumentInstance();
		arg.jsonElement = jsonObject;

		ServiceHandler svcHandler = RestfulStrolchComponent.getInstance().getServiceHandler();
		return ResponseUtil.toResponse(svcHandler.doService(cert, svc, arg));
	}
}
