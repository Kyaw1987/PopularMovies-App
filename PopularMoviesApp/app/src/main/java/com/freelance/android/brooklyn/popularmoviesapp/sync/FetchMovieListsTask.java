package com.freelance.android.brooklyn.popularmoviesapp.sync;

import android.os.AsyncTask;
import android.support.annotation.StringDef;
import android.util.Log;

import com.freelance.android.brooklyn.popularmoviesapp.BuildConfig;
import com.freelance.android.brooklyn.popularmoviesapp.model.Movie;
import com.freelance.android.brooklyn.popularmoviesapp.network.MovieDatabaseService;
import com.freelance.android.brooklyn.popularmoviesapp.model.Movies;

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
public class FetchMovieListsTask extends AsyncTask<Void, Void, List<Movie>> {

    public static String LOG_TAG = FetchMovieListsTask.class.getSimpleName();

    public final static String MOST_POPULAR = "popular";
    public final static String TOP_RATED = "top_rated";
    public final static String FAVORITES = "favorites";

    @StringDef({MOST_POPULAR, TOP_RATED, FAVORITES})
    public @interface SORT_BY {
    }

    private final NotifyAboutTaskCompletionCommand mCommand;
    private
    @SORT_BY
    String mSortBy = MOST_POPULAR;

    public interface Listener {
        void onFetchFinished(Command command);
    }

    public static class NotifyAboutTaskCompletionCommand implements Command {
        private FetchMovieListsTask.Listener mListener;

        private List<Movie> mMovies;

        public NotifyAboutTaskCompletionCommand(FetchMovieListsTask.Listener listener) {
            mListener = listener;
        }

        public void execute() {
            mListener.onFetchFinished(this);
        }

        public List<Movie> getMovies() {
            return mMovies;
        }
    }


    public FetchMovieListsTask(@SORT_BY String sortBy, NotifyAboutTaskCompletionCommand command) {
        mCommand = command;
        mSortBy = sortBy;
    }


    @Override
    protected void onPostExecute(List<Movie> movies) {
        if (null != movies) {
            mCommand.mMovies = movies;
        } else {
            mCommand.mMovies = new ArrayList<>();
        }
        mCommand.execute();
    }

    @Override
    protected List<Movie> doInBackground(Void... params) {

        Log.d(LOG_TAG, "Starting sync");

        Retrofit r = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MovieDatabaseService movieDBService = r.create(MovieDatabaseService.class);
        Call<Movies> call = movieDBService.discoverMovies(mSortBy, BuildConfig.MOVIE_DATABASE_API_KEY);

        try {
            Response<Movies> response = call.execute();
            Movies m = response.body();
            return m.getMovies();

        } catch (IOException e) {
            Log.e(LOG_TAG, "You have found connection problem occurred and talking to the movie db org.", e);
        }
        return null;
    }
}
