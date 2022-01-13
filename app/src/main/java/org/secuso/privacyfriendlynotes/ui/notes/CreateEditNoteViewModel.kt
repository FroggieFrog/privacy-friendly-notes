package org.secuso.privacyfriendlynotes.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.secuso.privacyfriendlynotes.room.model.Category
import org.secuso.privacyfriendlynotes.room.model.Note
import org.secuso.privacyfriendlynotes.room.NoteDatabase
import org.secuso.privacyfriendlynotes.room.model.Notification

/**
 * The CreateEditNoteViewModel provides the data for all four note types.
 */

class CreateEditNoteViewModel(application: Application) : AndroidViewModel(application){

    private val repository: NoteDatabase = NoteDatabase.getInstance(application)
    val allNotifications: LiveData<List<Notification>> = repository.notificationDao().allNotifications
    val allCategoriesLive: LiveData<List<Category>> = repository.categoryDao().allCategoriesLive
    private val _categoryName: MediatorLiveData<String?> = MediatorLiveData<String?>()
    private var _categoryNameLast: LiveData<String?>? = null
    private val database: NoteDatabase = NoteDatabase.getInstance(application)


    fun insert(notification: Notification){
        viewModelScope.launch(Dispatchers.Default){
            repository.notificationDao().insert(notification)
        }
    }
    fun update(notification: Notification){
        viewModelScope.launch(Dispatchers.Default){
            repository.notificationDao().update(notification)
        }
    }
    fun delete(notification: Notification){
        viewModelScope.launch(Dispatchers.Default){
            repository.notificationDao().delete(notification)
        }
    }


    fun insert(category: Category) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().insert(category)
        }
    }

    fun update(category: Category) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().update(category)
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch(Dispatchers.Default) {
            repository.categoryDao().delete(category)
        }
    }

    fun getCategoryNameFromId(categoryId: Integer): LiveData<String?> {

        viewModelScope.launch(Dispatchers.Default){
            withContext(Dispatchers.Main){
                if(_categoryNameLast != null){
                    _categoryName.removeSource(_categoryNameLast!!)
                }
            }
            _categoryNameLast = repository.categoryDao().categoryNameFromId(categoryId)

            withContext(Dispatchers.Main){
                _categoryName.addSource(_categoryNameLast!!){
                    _categoryName.postValue(it)
                }
            }

        }
        return _categoryName
    }


    fun insert(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            database.noteDao().insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            database.noteDao().update(note)

        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.Default) {
            database.noteDao().delete(note)
        }
    }
}