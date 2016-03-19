package im.ene.lab.wordy.data.api;

import im.ene.lab.wordy.data.response.ClassifiersResponse;
import im.ene.lab.wordy.data.response.ClassifyResponse;
import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by eneim on 3/19/16.
 */
public class ApiService {

    static final String END_POINT = "https://gateway.watsonplatform.net/visual-recognition-beta/";

    public static final String user_name = "913f67b0-dfec-47c2-ace9-8b9701ce302e";

    public static final String password = "p55sHLqZFmB5";

    public static final String alchemyApikey = "464e790263f961081b84adcbbc5888a9d830657d";

    public interface Api {

        /**
         * Get classifiers. Need to call first.
         *
         * @return list of classifier
         */
        @GET("api/v2/classifiers")
        Observable<ClassifiersResponse> classifiers(@Query("version") String version);

        @Multipart
        @POST("api/v2/classify")
        Observable<ClassifyResponse> classify(
                @Part("images_file") RequestBody file,
                @Query("version") String version);    // format: 2015-12-02

    }
}
