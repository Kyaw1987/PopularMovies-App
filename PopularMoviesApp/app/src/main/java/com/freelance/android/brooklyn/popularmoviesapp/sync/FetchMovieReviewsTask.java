package com.freelance.android.brooklyn.popularmoviesapp.sync;

import android.os.AsyncTask;
import android.util.Log;

import com.freelance.android.brooklyn.popularmoviesapp.BuildConfig;
import com.freelance.android.brooklyn.popularmoviesapp.network.MovieDatabaseService;
import com.freelance.android.brooklyn.popularmoviesapp.model.Review;
import com.freelance.android.brooklyn.popularmoviesapp.model.Reviews;

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
public class FetchMovieReviewsTask extends AsyncTask<Long, Void, List<Review>>{

    @SuppressWarnings("unused")
    public static String LOG_TAG = FetchMovieReviewsTask.class.getSimpleName();
    private final Listener mListener;

    public interface Listener{
        void onReviewsFetchFinished(List<Review> reviews);
    }

    public FetchMovieReviewsTask(Listener mListener) {
        this.mListener = mListener;
    }

    @Override
    protected List<Review> doInBackground(Long... params) {
        if(params.length == 0){
            return null;
        }

        long mReviewsId = params[0];

        Retrofit r = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MovieDatabaseService movieDBService = r.create(MovieDatabaseService.class);
        Call<Reviews> call = movieDBService.findReviewsById(mReviewsId, BuildConfig.MOVIE_DATABASE_API_KEY);

        try {
            Response<Reviews> response = call.execute();
            Reviews reviews = response.body();
            return reviews.getReviews();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Be sure connection problem occurred and talking to the movie db org.", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Review> reviews) {
        if(reviews != null){
            mListener.onReviewsFetchFinished(reviews);
        }else{
            mListener.onReviewsFetchFinished(new ArrayList<Review>());
        }
    }
}
