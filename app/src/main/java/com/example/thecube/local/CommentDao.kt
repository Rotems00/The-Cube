package com.example.thecube.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.thecube.model.Comment

@Dao
interface CommentDao {

    @Query("DELETE FROM comments")
    suspend fun deleteAllComments()

    @Query("SELECT * FROM comments WHERE dishId = :dishId")
    suspend fun getCommentsForDishSync(dishId: String): List<Comment>

    @Delete
    suspend fun deleteComment(comment: Comment)


    @Query("SELECT * FROM comments WHERE dishId = :dishId ORDER BY timeStamp ASC")
    fun getCommentsForDish(dishId: String): LiveData<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<Comment>)

    @Query("DELETE FROM comments WHERE dishId = :dishId")
    suspend fun deleteCommentsForDish(dishId: String)
}
