package com.freelance.android.brooklyn.popularmoviesapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.freelance.android.brooklyn.popularmoviesapp.R;
import com.freelance.android.brooklyn.popularmoviesapp.adapter.MovieListsAdapter;
import com.freelance.android.brooklyn.popularmoviesapp.data.MovieContract.MovieEntry;
import com.freelance.android.brooklyn.popularmoviesapp.fragments.MovieDetailFragment;
import com.freelance.android.brooklyn.popularmoviesapp.model.Movie;
import com.freelance.android.brooklyn.popularmoviesapp.sync.Command;
import com.freelance.android.brooklyn.popularmoviesapp.sync.FetchMovieListsTask;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;



public class MovieListsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        FetchMovieListsTask.Listener, MovieListsAdapter.Callbacks {

    private static final String EXTRA_MOVIES = "EXTRA_MOVIES";
    private static final String EXTRA_SORT_BY = "EXTRA_SORT_BY";
    private static final int FAVORITE_MOVIES_LOADER = 0;

    private boolean mTwoPane;
    private RetainedFragment mRetainedFragment;
    private MovieListsAdapter mAdapter;
    private String mSortBy = FetchMovieListsTask.MOST_POPULAR;

    @Bind(R.id.movie_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_lists);
        ButterKnife.bind(this);

        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        String name = RetainedFragment.class.getName();
        this.mRetainedFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag(name);

        if (this.mRetainedFragment == null) {
            this.mRetainedFragment = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(this.mRetainedFragment, name).commit();
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.grid_number_cols)));
        mAdapter = new MovieListsAdapter(new ArrayList<Movie>(), this);
        mRecyclerView.setAdapter(mAdapter);


        mTwoPane = findViewById(R.id.movie_detail_container) != null;


        if (savedInstanceState != null) {
            mSortBy = savedInstanceState.getString(EXTRA_SORT_BY);
            if (savedInstanceState.containsKey(EXTRA_MOVIES)) {
                List<Movie> movies = savedInstanceState.getParcelableArrayList(EXTRA_MOVIES);
                mAdapter.add(movies);
                findViewById(R.id.progress).setVisibility(View.GONE);

                if (mSortBy.equals(FetchMovieListsTask.FAVORITES)) {
                    getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER, null, this);
                }
            }
            updateEmptyState();
        } else {
            fetchMovies(mSortBy);
        }
    }

    private void updateEmptyState() {
        if (mAdapter.getItemCount() == 0) {
            if (mSortBy.equals(FetchMovieListsTask.FAVORITES)) {
                findViewById(R.id.empty_state_container).setVisibility(View.GONE);
                findViewById(R.id.empty_state_favorites_container).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.empty_state_container).setVisibility(View.VISIBLE);
                findViewById(R.id.empty_state_favorites_container).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.empty_state_container).setVisibility(View.GONE);
            findViewById(R.id.empty_state_favorites_container).setVisibility(View.GONE);
        }
    }

    private void fetchMovies(String mSortBy) {
        if (!mSortBy.equals(FetchMovieListsTask.FAVORITES)) {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            FetchMovieListsTask.NotifyAboutTaskCompletionCommand command =
                    new FetchMovieListsTask.NotifyAboutTaskCompletionCommand(this.mRetainedFragment);
            new FetchMovieListsTask(mSortBy, command).execute();
        } else {
            getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu_movie_lists, menu);

        switch (mSortBy) {
            case FetchMovieListsTask.MOST_POPULAR:
                menu.findItem(R.id.mostPopular).setChecked(true);
                break;

            case FetchMovieListsTask.TOP_RATED:
                menu.findItem(R.id.highestRated).setChecked(true);
                break;

            case FetchMovieListsTask.FAVORITES:
                menu.findItem(R.id.favourites).setChecked(true);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.action_scroll_to_top:
                scrollToTop(true);
                break;
            case R.id.action_scroll_to_top:
                scrollToTop(true);
                break;*/
            case R.id.highestRated:
                if (mSortBy.equals(FetchMovieListsTask.FAVORITES)) {
                    getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
                }
                mSortBy = FetchMovieListsTask.TOP_RATED;
                fetchMovies(mSortBy);
                item.setChecked(true);
                break;
            case R.id.mostPopular:
                if (mSortBy.equals(FetchMovieListsTask.FAVORITES)) {
                    getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
                }
                mSortBy = FetchMovieListsTask.MOST_POPULAR;
                fetchMovies(mSortBy);
                item.setChecked(true);
                break;
            case R.id.favourites:
                mSortBy = FetchMovieListsTask.FAVORITES;
                item.setChecked(true);
                fetchMovies(mSortBy);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFetchFinished(Command command) {
        if (command instanceof FetchMovieListsTask.NotifyAboutTaskCompletionCommand) {
            mAdapter.add(((FetchMovieListsTask.NotifyAboutTaskCompletionCommand) command).getMovies());
            updateEmptyState();
            findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        return new CursorLoader(this,
                MovieEntry.CONTENT_URI,
                MovieEntry.MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.add(cursor);
        updateEmptyState();
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void open(Movie movie, int position) {
        if (mTwoPane) {
            Bundle b = new Bundle();
            b.putParcelable(MovieDetailFragment.ARG_MOVIE, movie);
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(b);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDetailFragment.ARG_MOVIE, movie);
            startActivity(intent);
        }
    }


    public static class RetainedFragment extends Fragment implements FetchMovieListsTask.Listener {

        private boolean mPaused = false;

        private Command mWaitingCommand = null;

        public RetainedFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public void onResume() {
            super.onResume();
            mPaused = false;
            if (mWaitingCommand != null) {
                onFetchFinished(mWaitingCommand);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            mPaused = true;
        }

        @Override
        public void onFetchFinished(Command command) {
            if (getActivity() instanceof FetchMovieListsTask.Listener && !mPaused) {
                FetchMovieListsTask.Listener listener = (FetchMovieListsTask.Listener) getActivity();
                listener.onFetchFinished(command);
                mWaitingCommand = null;
            } else {
                mWaitingCommand = command;
            }
        }
    }
}
