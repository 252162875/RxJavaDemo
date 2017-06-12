package com.mym.rxjavademo.http;

import com.mym.rxjavademo.bean.DouBanBean;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {

    class Builder {
        public static Api getService() {
            return HttpClient.getInstance().getServer(Api.class);
        }
    }

    @GET("top250")
    Observable<DouBanBean> getTopMovie(@Query("start") int start, @Query("count") int count);
}