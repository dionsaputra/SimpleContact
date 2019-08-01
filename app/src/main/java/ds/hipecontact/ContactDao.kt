package ds.hipecontact

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact_table")
    fun contactList(): Single<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contact: Contact): Completable

    @Delete
    fun delete(contact: Contact): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(contact: Contact): Completable
}