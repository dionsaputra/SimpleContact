package ds.hipecontact

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Contact::class], version = 1)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun getContactDao(): ContactDao
}