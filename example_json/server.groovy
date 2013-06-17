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
		println("Constructor");
//		File file = new File("/Users/sarnobat/mac-sync/mwk/new.mwk");
//		InputStream in;
		FileReader fr = new FileReader("/Users/sarnobat/mac-sync/mwk/new.mwk");
//		in = new FileInputStream(file);
		lines = IOUtils.readLines(fr);		
	}
	public Map<String, String> getQueryMap(String query)  
	{  
		Pattern pattern = Pattern.compile("^..");
		Matcher matcher = pattern.matcher(query);
		query = matcher.replaceAll("");
		String[] params = query.split("&");  
		Map<String, String> map = new HashMap<String, String>();  
		for (String param : params)  
		{  
			String name = param.split("=")[0];  
			String value = param.split("=")[1];  
			map.put(name, value);  
		}  
		return map;  
	}  

	public void handle(HttpExchange t) throws IOException {
		JSONArray jsonArray = new JSONArray();
		String query = t.getRequestURI();
		Map<String, String> map = getQueryMap(query);
		if(map.size() < 1) {
			throw new RuntimeException("No params");
		}
		String  value = map.get("param1");
		for (String line : lines) {
			if (!line.contains(value)) {
				continue;
			}
			JSONObject json = new JSONObject();
			json.put("name", line);
			println('Request headers: ' + t.getRequestHeaders());
			println('Request URI' + t.getRequestURI());
			println('value: ' + value);
			jsonArray.put(json);
		}
		t.getResponseHeaders().add("Access-Control-Allow-Origin","*");
		t.sendResponseHeaders(200, jsonArray.toString().length());
		OutputStream os = t.getResponseBody();
		os.write(jsonArray.toString().getBytes());
		os.close();
	}
}
    
HttpServer server = HttpServer.create(new InetSocketAddress(4444), 0);
server.createContext("/", new MyHandler());
server.setExecutor(null); // creates a default executor
server.start();
