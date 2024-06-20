package kr.co.astkorea.proxy.http;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProxyService {

    @Autowired
    private CloseableHttpClient httpClient;

    public ResponseEntity<?> forwardGetRequest(String targetUrl, Map<String, String> params, Map<String, String> headers) throws IOException {
        String queryParams = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        String fullUrl = targetUrl + "?" + queryParams;
        HttpUriRequestBase request = new HttpGet(fullUrl);
        
        headers.forEach((key, value) -> {
            if (!"content-length".equalsIgnoreCase(key)) {
                request.addHeader(key, value);
            }
        });

        return executeGetRequest(request);
    }

    public ResponseEntity<?> forwardPostRequest(String targetUrl, String body, Map<String, String> headers) throws IOException {
        HttpPost request = new HttpPost(targetUrl);
        headers.forEach((key, value) -> {
            if (!"content-length".equalsIgnoreCase(key)) {
                request.addHeader(key, value);
            }
        });
        return executePostRequest(request);
    }

    private ResponseEntity<?> executeGetRequest(HttpUriRequestBase request) throws IOException {
        try (var response = httpClient.execute(request)) {
            int status = response.getCode();
            String body = EntityUtils.toString(response.getEntity());
            
            JsonObject json = new Gson().fromJson(body, JsonObject.class);
            JsonElement element = json.get("code");
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            String code = element.getAsString();
            if (primitive.isNumber()) {
                code = element.getAsInt() + "";
            }
            if ("500".equals(code)) {
                status = 204;
            }
            
            Header[] headers = response.getHeaders();
            HttpHeaders httpHeaders = new HttpHeaders();
            for (Header h : headers) {
                httpHeaders.addIfAbsent(h.getName(), h.getValue());
            }
            return ResponseEntity.status(status).body(body);
        } catch (ParseException | ClientProtocolException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    private ResponseEntity<?> executePostRequest(HttpUriRequestBase request) throws IOException {
        try (var response = httpClient.execute(request)) {
            int status = response.getCode();
            String body = EntityUtils.toString(response.getEntity());
            
            Header[] headers = response.getHeaders();
            HttpHeaders httpHeaders = new HttpHeaders();
            for (Header h : headers) {
                httpHeaders.add(h.getName(), h.getValue());
            }
            return ResponseEntity.status(status).headers(httpHeaders).body(body);
        } catch (ParseException | ClientProtocolException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
