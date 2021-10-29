package com.decidone.messenger;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MyAPI{

    @FormUrlEncoded
    @POST("post/create/")
    Call<PostItem> createPost(
            @Field("access_token") String access_token,
            @Field("band_key") String band_key,
            @Field("content") String content,
            @Field("do_push") boolean do_push
    );

    @GET("get/")
    Call<List<PostItem>> get_posts();
}