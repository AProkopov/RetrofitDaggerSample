package com.bugsnguns.retrofitdaggersample;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bugsnguns.retrofitdaggersample.adapter.RandomUserAdapter;
import com.bugsnguns.retrofitdaggersample.interfaces.RandomUsersApi;
import com.bugsnguns.retrofitdaggersample.model.RandomUsers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    Retrofit retrofit;
    RecyclerView recyclerView;
    RandomUserAdapter mAdapter;

    Picasso picasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        Timber.plant(new Timber.DebugTree());

        File cacheFile = new File(this.getCacheDir(), "HttpCache");
        File cacheFileForOffline = new File(this.getCacheDir(), "HttpCacheOffline");
        cacheFile.mkdirs();
        cacheFileForOffline.mkdirs();

        Cache cache = new Cache(cacheFile, 10 * 1000 * 1000);//10 MB
        Cache cacheOffline = new Cache(cacheFileForOffline, 10 * 1000 * 1000);//10 MB

        Cache cacheTarget;

        //Если директория HttpCache существует
        if (cacheFile != null) {
            if (noConnection) {
                cacheTarget = cacheOffline;
            } else {
                cacheTarget = cache;
            }
        } else cacheTarget = cache;

        HttpLoggingInterceptor httpLoggingInterceptor = new
                HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Timber.i(message);
            }
        });

        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .cache(cache)
                .addInterceptor(httpLoggingInterceptor)
                .build();

        OkHttp3Downloader okHttpDownloader = new OkHttp3Downloader(okHttpClient);

        picasso = new Picasso.Builder(this).downloader(okHttpDownloader).build();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://randomuser.me/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        populateUsers();

    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void populateUsers() {
        Call<RandomUsers> randomUsersCall = getRandomUserService().getRandomUsers(10);
        randomUsersCall.enqueue(new Callback<RandomUsers>() {
            @Override
            public void onResponse(Call<RandomUsers> call, @NonNull Response<RandomUsers> response) {
                if(response.isSuccessful()) {
                    mAdapter = new RandomUserAdapter();
                    mAdapter.setItems(response.body().getResults());
                    recyclerView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onFailure(Call<RandomUsers> call, Throwable t) {
                Timber.i(t.getMessage());
            }
        });
    }

    public RandomUsersApi getRandomUserService(){
        return retrofit.create(RandomUsersApi.class);
    }

}
