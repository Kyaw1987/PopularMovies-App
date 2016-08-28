package com.freelance.android.brooklyn.popularmoviesapp.sync;

import android.os.AsyncTask;
import android.util.Log;

import com.freelance.android.brooklyn.popularmoviesapp.BuildConfig;
import com.freelance.android.brooklyn.popularmoviesapp.network.MovieDatabaseService;
import com.freelance.android.brooklyn.popularmoviesapp.model.Trailer;
import com.freelance.android.brooklyn.popularmoviesapp.model.Trailers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Administrator on 08/11/2016.
 */
public class FetchMovieTrailersTask extends AsyncTask<Long, Void, List<Trailer>> {

    @SuppressWarnings("unused")
    public static String LOG_TAG = FetchMovieTrailersTask.class.getSimpleName();
    private final Listener mListener;

    public interface Listener{
        void onFetchFinished(List<Trailer> trailers);
    }

    public FetchMovieTrailersTask(Listener listener) {
        mListener = listener;
    }

    @Override
    protected void onPostExecute(List<Trailer> trailers) {
        if(trailers != null){
            mListener.onFetchFinished(trailers);
        }else{
            mListener.onFetchFinished(new ArrayList<Trailer>());
        }
    }

    @Override
    protected List<Trailer> doInBackground(Long... params) {
        if (params.length == 0) {
            return null;
        }

        long mTrailersId = params[0];

        Retrofit r = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MovieDatabaseService movieDBService = r.create(MovieDatabaseService.class);
        Call<Trailers> call = movieDBService.findTrailersById(mTrailersId, BuildConfig.MOVIE_DATABASE_API_KEY);

        try {
            Response<Trailers> response = call.execute();
            Trailers t = response.body();
            return t.getTrailers();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Be sure connection problem occurred and talking to the movie db org.", e);
        }

        return null;
    }
}
