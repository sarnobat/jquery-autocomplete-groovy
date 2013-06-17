import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;



class MyHandler implements HttpHandler {
	private final List<String> lines;
	public MyHandler() {
		FileReader fr = new FileReader("/Users/sarnobat/mac-sync/mwk/new.mwk");
		lines = IOUtils.readLines(fr);		
	}
	public Map<String, String> getQueryMap(String query)  
	{  
		Pattern pattern = Pattern.compile("^..");
		Matcher matcher = pattern.matcher(query);
		query = matcher.replaceAll("");
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}

	public void handle(HttpExchange exchange) throws IOException {
		JSONArray response = new JSONArray();
		String query = exchange.getRequestURI();
		Map<String, String> queryString = getQueryMap(query);
		if(queryString.size() < 1) {
			throw new RuntimeException("No params");
		}
		String  value = queryString.get("param1");
		for (String dataSourceLine : lines) {
			if (!dataSourceLine.contains(value)) {
				continue;
			}
			JSONObject pair = new JSONObject();
			pair.put("name", dataSourceLine);
			println('Request headers: ' + exchange.getRequestHeaders());
			println('Request URI' + exchange.getRequestURI());
			println('value: ' + value);
			response.put(pair);
		}
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin","*");
		exchange.sendResponseHeaders(200, response.toString().length());
		OutputStream responseBody = exchange.getResponseBody();
		responseBody.write(response.toString().getBytes());
		responseBody.close();
	}
}

HttpServer server = HttpServer.create(new InetSocketAddress(4444), 0);
server.createContext("/", new MyHandler());
server.setExecutor(null); // creates a default executor
server.start();
