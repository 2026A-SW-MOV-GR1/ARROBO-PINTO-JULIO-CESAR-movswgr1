package com.example.persistenciadual.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface PostApiService {

    @GET("posts/{id}")
    suspend fun getPost(
        @Path("id") id: Int
    ): Response<PostDto>

    @PUT("posts/{id}")
    suspend fun updatePost(
        @Path("id") id: Int,
        @Body post: PostDto
    ): Response<PostDto>
}