package br.ufpe.cin.android.podcast

import androidx.room.*

@Dao
interface ItemFeedDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertItems(vararg items:ItemFeed)

    @Update
    fun updateItems(vararg items:ItemFeed)

    @Delete
    fun destroyItems(vararg items:ItemFeed)

    @Query("SELECT * FROM feed_items ORDER BY title ASC")
    fun allItems() : List<ItemFeed>

    @Query("SELECT * FROM feed_items WHERE title LIKE :q")
    fun searchItemByTitle(q : String) : List<ItemFeed>

    @Query("SELECT * FROM feed_items WHERE pubDate LIKE :q")
    fun searchItemByPubdate(q : String) : List<ItemFeed>

    @Query("SELECT * FROM feed_items WHERE description LIKE :q")
    fun searchItemByDescription(q : String) : List<ItemFeed>

    @Query("SELECT * FROM feed_items WHERE downloadLink LIKE :q")
    fun searchItemByDownloadLink(q : String) : ItemFeed
}