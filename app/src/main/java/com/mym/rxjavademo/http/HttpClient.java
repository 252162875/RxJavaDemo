package com.mym.rxjavademo.http;

import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.mym.rxjavademo.app.BaseApplication;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import com.mym.rxjavademo.utils.CheckNetwork;

/**
 * 网络请求工具类
 * <p>
 * 豆瓣api:
 * 问题：API限制为每分钟40次，一不小心就超了，马上KEY就被封,用不带KEY的API，每分钟只有可怜的10次。
 * 返回：code:112（rate_limit_exceeded2 IP 访问速度限制）
 * 解决：1.使用每分钟访问次数限制（客户端）2.更换ip (更换wifi)
 * 豆瓣开发者服务使用条款: https://developers.douban.com/wiki/?title=terms
 */

public class HttpClient {
    private static HttpClient instance;
    private Gson gson;
    private Object https;
    private final static String API = "https://api.douban.com/v2/movie/";

    public static HttpClient getInstance() {
        if (!CheckNetwork.isNetworkConnected(BaseApplication.getContext()) && !CheckNetwork.isWifiConnected(BaseApplication.getContext())) {
            Toast.makeText(BaseApplication.getContext(), "请检查网络...", Toast.LENGTH_SHORT).show();
        }
        if (instance == null) {
            synchronized (HttpClient.class) {
                if (instance == null) {
                    instance = new HttpClient();
                }
            }
        }
        return instance;
    }

    public <T> T getServer(Class<T> a) {
        if (https == null) {
            synchronized (HttpClient.class) {
                if (https == null) {
                    https = getBuilder(API).build().create(a);
                }
            }
        }
        return (T) https;
    }

    private Retrofit.Builder getBuilder(String apiUrl) {
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(apiUrl);
        builder.client(getOkHttpClient());
        builder.addConverterFactory(GsonConverterFactory.create(getGson()));
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        return builder;
    }

    private Gson getGson() {
        if (gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.setLenient();
            builder.serializeNulls();
            gson = builder.create();
        }
        return gson;
    }

    private final static long DEFAULT_TIMEOUT = 10;
    private final static long DEFAULT_READ_TIMEOUT = 20;
    private final static long DEFAULT_WRITE_TIMEOUT = 20;

    private OkHttpClient getOkHttpClient() {
        try {
            //定制OkHttp
            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            }};
            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            //设置超时时间
            httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            httpClientBuilder.writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS);
            httpClientBuilder.readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS);
            httpClientBuilder.sslSocketFactory(sslSocketFactory);
            ParamsInterceptor paramsInterceptor = new ParamsInterceptor();
            httpClientBuilder.addInterceptor(paramsInterceptor);
            httpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
//                    Log.d("HttpClient", "==come");
                    return true;
                }
            });
            return httpClientBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
