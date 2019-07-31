package util;

import constant.Constant;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class HttpsUtil {

    private static Logger logger = Logger.getLogger(HttpsUtil.class);

    public static JSONObject post(String url, JSONObject params, HashMap<String, String> headers) {
        return post(url, params, headers, "application/json");
    }

    /*
     * @param url
     * @param params
     * @param headers
     * @return JSONObject  res.status返回的状态码，res.body 返回的数据字符串
     */
    public static JSONObject post(String url, JSONObject params, HashMap<String, String> headers, String contentType) {
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpClient client = getHttpsClient();
        if (headers.size() > 0) {
            for (String key : headers.keySet()) {
                httpPost.addHeader(key, headers.get(key));
            }
        }
        StringEntity entity = new StringEntity(JSONUtils.valueToString(params), Constant.DEFAULT_CHARSET);
        entity.setContentEncoding(Constant.DEFAULT_CHARSET);
        entity.setContentType(contentType);
        httpPost.setEntity(entity);
        HttpResponse resp = null;
        JSONObject response = new JSONObject();
        String resBodyStr;
        try {
            resp = client.execute(httpPost);
            int status = resp.getStatusLine().getStatusCode();
            resBodyStr = EntityUtils.toString(resp.getEntity(), Constant.DEFAULT_CHARSET);
            response.put("status", status);
            response.put("response", JSONObject.fromObject(resBodyStr));
        } catch (IOException e) {
            response.put("status", -1);
            logger.error("Failed to execute https post " + url);
        }
        return response;
    }

    private static CloseableHttpClient getHttpsClient() {
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", trustAllHttpsCertificates())
                        .build();
        //创建ConnectionManager，添加Connection配置信息
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        CloseableHttpClient httpsClient = HttpClients.custom().setConnectionManager(connectionManager).build();
        return httpsClient;
    }

    /**
     * 信任所有证书
     */
    private static SSLConnectionSocketFactory trustAllHttpsCertificates() {
        SSLConnectionSocketFactory socketFactory = null;
        TrustManager[] trustAllCerts = buildTrustManagers();
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");//sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, null);
            socketFactory = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
            //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            logger.error("NoSuchAlgorithmException");
        } catch (KeyManagementException e) {
            logger.error("KeyManagementException");
        }
        return socketFactory;
    }

    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }
}
