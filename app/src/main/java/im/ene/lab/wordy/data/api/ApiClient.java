package im.ene.lab.wordy.data.api;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;

import im.ene.lab.wordy.data.response.ClassifiersResponse;
import im.ene.lab.wordy.data.response.ClassifyResponse;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by eneim on 3/19/16.
 */
public class ApiClient {

    static final Retrofit retrofit;

    private static final OkHttpClient client;

    private static final ApiService.Api api;

    static {
        client = new OkHttpClient.Builder()
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic(ApiService.user_name, ApiService.password);
                        return response.request().newBuilder().header("Authorization", credential).build();
                    }
                })
//                .addNetworkInterceptor(new CurlInterceptor(new Loggable() {
//                    @Override
//                    public void log(String message) {
//                        Log.v("Ok2Curl", message);
//                    }
//                }))
                .build();

        retrofit = new Retrofit.Builder().baseUrl(ApiService.END_POINT)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build();

        api = retrofit.create(ApiService.Api.class);
    }

    public static Observable<ClassifiersResponse> classifiers(String version) {
        return api.classifiers(version);
    }

    public static Observable<ClassifyResponse> classify(File file) {
        RequestBody body =
                MultipartBody.create(MediaType.parse("image/jpeg"), file);
        return api.classify(body, "2016-03-20");
    }
}
