package org.secuso.privacyfriendlynotes.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
        @PrimaryKey(autoGenerate = false)
        var _noteId: Int,
        var time: Int) {


}