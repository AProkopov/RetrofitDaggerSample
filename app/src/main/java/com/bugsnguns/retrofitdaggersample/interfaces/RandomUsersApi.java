package com.bugsnguns.retrofitdaggersample.interfaces;

/**
 * Created by antonio on 07.01.18.
 */

import com.bugsnguns.retrofitdaggersample.model.RandomUsers;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Hari on 20/11/17.
 */

public interface RandomUsersApi {

    @GET("api")
    Call<RandomUsers> getRandomUsers(@Query("results") int size);
}
