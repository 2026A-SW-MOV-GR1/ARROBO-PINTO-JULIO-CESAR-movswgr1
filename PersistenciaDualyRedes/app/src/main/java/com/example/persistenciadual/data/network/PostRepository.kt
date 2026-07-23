package com.example.persistenciadual.data.network

import com.example.persistenciadual.core.AppLogger

data class ApiCallResult<T>(
    val statusCode: Int,
    val data: T
)

class ApiException(
    val statusCode: Int,
    message: String
) : Exception(message)

class PostRepository(
    private val apiService: PostApiService,
    private val logger: AppLogger
) {

    suspend fun getPost(id: Int): ApiCallResult<PostDto> {
        logger.debug(
            TAG,
            "DEBUG iniciando GET /posts/$id"
        )

        val response = apiService.getPost(id)

        if (!response.isSuccessful) {
            logger.error(
                TAG,
                "ERROR GET /posts/$id. HTTP ${response.code()}"
            )

            throw ApiException(
                statusCode = response.code(),
                message = "La consulta devolvió HTTP ${response.code()}"
            )
        }

        val post = response.body()
            ?: throw ApiException(
                statusCode = response.code(),
                message = "El servidor respondió sin contenido"
            )

        logger.info(
            TAG,
            "INFO GET correcto. HTTP ${response.code()}"
        )

        return ApiCallResult(
            statusCode = response.code(),
            data = post
        )
    }

    suspend fun updatePost(
        post: PostDto
    ): ApiCallResult<PostDto> {

        logger.debug(
            TAG,
            "DEBUG iniciando PUT /posts/${post.id}"
        )

        val response = apiService.updatePost(
            id = post.id,
            post = post
        )

        if (!response.isSuccessful) {
            logger.error(
                TAG,
                "ERROR PUT /posts/${post.id}. HTTP ${response.code()}"
            )

            throw ApiException(
                statusCode = response.code(),
                message = "La actualización devolvió HTTP ${response.code()}"
            )
        }

        val updatedPost = response.body()
            ?: throw ApiException(
                statusCode = response.code(),
                message = "El servidor respondió sin contenido"
            )

        logger.info(
            TAG,
            "INFO PUT correcto. HTTP ${response.code()}"
        )

        return ApiCallResult(
            statusCode = response.code(),
            data = updatedPost
        )
    }

    companion object {
        private const val TAG = "PostRepository"
    }
}