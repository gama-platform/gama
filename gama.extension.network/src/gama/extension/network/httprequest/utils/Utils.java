/*******************************************************************************************************
 *
 * Utils.java, in gama.extension.network, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.network.httprequest.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;

import gama.api.GAMA;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.map.GamaMapFactory;
import gama.core.util.json.ParseException;

/**
 * The Class Utils.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 29 oct. 2023
 */
public class Utils {

	/**
	 * Builds the URI.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @param url
	 *            the url
	 * @return the uri
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @date 29 oct. 2023
	 */
	public static URI buildURI(final String host, final String port, final String url) throws URISyntaxException {
		String uri = "";
		String local_port = port != null ? ":" + port : "";

		if (host.startsWith("http://") || host.startsWith("https://")) {
			uri = host + local_port + url;
		} else {
			uri = "http://" + host + local_port + url;
		}

		return new URI(uri);// URLEncoder.encode(uri, StandardCharsets.UTF_8));
	}

	/**
	 * Parses the BODY.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param body
	 *            the body
	 * @return the i list
	 * @date 29 oct. 2023
	 */
	public static IList parseBODY(final IScope scope, final String body) {
		// TODO Transform lee boy en map/list si response en JSON

		return null;
	}

	/**
	 * Format response.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param response
	 *            the response
	 * @return the i map
	 * @date 29 oct. 2023
	 */
	public static IMap<String, Object> formatResponse(final HttpResponse<String> response) {
		IMap<String, Object> responseMap = null;

		try {
			responseMap = GamaMapFactory.create();
			responseMap.put("CODE", response.statusCode());

			IMap<String, List<String>> mapHeaders =
					GamaMapFactory.wrap(Types.STRING, Types.STRING, false, response.headers().map());
			responseMap.put("HEADERS", mapHeaders);

			Object jsonBody = "";
			if (!"".equals(response.body())) {
				List<String> contentType = mapHeaders.get("content-type");
				if (contentType != null) {
					if (contentType.stream().anyMatch(e -> e.contains("json"))) {
						jsonBody = GAMA.getJsonEncoder().parse(response.body());
					} else {
						jsonBody = response.body();
					}
				}
			}
			responseMap.put("BODY", jsonBody);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return responseMap;
	}

}
