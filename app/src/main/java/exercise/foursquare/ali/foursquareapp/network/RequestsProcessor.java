package exercise.foursquare.ali.foursquareapp.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import exercise.foursquare.ali.foursquareapp.R;
import exercise.foursquare.ali.foursquareapp.models.SearchResponse;
import exercise.foursquare.ali.foursquareapp.utils.AppConstants;
import exercise.foursquare.ali.foursquareapp.utils.NetConstants;

/**
 * Created by kazi_ on 7/21/2016.
 */
public class RequestsProcessor {
    //https://api.foursquare.com/v2/venues/search?client_id=CLIENT_ID&client_secret=CLIENT_SECRET&v=20130815&ll=40.7,-74&query=sushi

    private static final String LOG_TAG = AppConstants.LOG_TAG_QUERY;

    private Context mContext;
    private Uri.Builder mUriBuilder;
    private ConnectionHelper mConnectionHelper;
    private BufferedInputStream mBufferedInputStream;
    private InputStreamReader mInputStreamReader;
    private RequestResponseListener mRequestResponseListener;

    public RequestsProcessor(Context context, RequestResponseListener requestResponseListener) {
        mContext = context;
        mRequestResponseListener = requestResponseListener;
        mConnectionHelper = new ConnectionHelper(mContext);
    }

    public void searchQuery(String query, double lat, double lang) {
        String latLang = String.format(mContext.getString(R.string.param_lat_lang), lat, lang);
        mUriBuilder = new Uri.Builder();
        mUriBuilder.scheme(NetConstants.SCHEME_HTTPS)
                .authority(NetConstants.FS_AUTHORITY)
                .appendPath(NetConstants.FS_API_V2)
                .appendPath(NetConstants.FS_PATH_VENUES)
                .appendPath(NetConstants.FS_PATH_SEARCH)
                .appendQueryParameter(NetConstants.FS_CLIENT_ID, AppConstants.CLIENT_ID)
                .appendQueryParameter(NetConstants.FS_CLIENT_SECRET, AppConstants.CLIENT_SECRET)
                .appendQueryParameter(NetConstants.FS_VERSION_PARAMETER, AppConstants.VERSION_PARAMTER)
                .appendQueryParameter(NetConstants.FS_LATITUDE_LONGITUDE, latLang)
                .appendQueryParameter(NetConstants.FS_PARAMETER_QUERY, query);

        try {
            URL url = new URL(mUriBuilder.build().toString());
            Gson gson =  new Gson();
            if (mConnectionHelper != null) {
                HttpsURLConnection connection = mConnectionHelper.get(url);
                if(connection != null && connection.getResponseCode() == NetConstants.RESPONSE_CODE_OK) {
                    mBufferedInputStream = new BufferedInputStream(connection.getInputStream());
                    mInputStreamReader = new InputStreamReader(mBufferedInputStream);
                    SearchResponse searchResponse = gson.fromJson(mInputStreamReader, SearchResponse.class);
                    mBufferedInputStream.close();
                    mInputStreamReader.close();
                    mRequestResponseListener.responseOk(searchResponse);
                } else {
                    mRequestResponseListener.responseError();
                }
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception with get. e: " + e.getLocalizedMessage());
            mRequestResponseListener.responseError();
        }
    }

    public interface RequestResponseListener {
        void responseOk(SearchResponse searchResponse);

        void responseError();
    }
}
