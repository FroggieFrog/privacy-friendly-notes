/*
 This file is part of the application Privacy Friendly Notes.
 Privacy Friendly Notes is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 Privacy Friendly Notes is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Notes. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlynotes.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;

import androidx.annotation.NonNull;

import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil;
import org.secuso.privacyfriendlybackup.api.backup.FileUtil;
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.secuso.privacyfriendlynotes.room.NoteDatabase.DATABASE_NAME;

public class BackupRestorer implements IBackupRestorer {

    private void readFiles(@NonNull JsonReader reader, @NonNull Context context) throws IOException {
        reader.beginObject();

        while(reader.hasNext()) {
            String name = reader.nextName();

            switch(name) {
                case "sketches":
                case "audio_notes":
                    File f = new File(context.getFilesDir(), name);
                    FileUtil.readPath(reader, f);
                    break;
                default:
                    throw new RuntimeException("Unknown folder "+name);
            }
        }

        reader.endObject();
    }

    private void readDatabase(@NonNull JsonReader reader, @NonNull Context context) throws IOException {
        reader.beginObject();

        String n1 = reader.nextName();
        if(!n1.equals("version")) {
            throw new RuntimeException("Unknown value " + n1);
        }
        int version = reader.nextInt();

        String n2 = reader.nextName();
        if(!n2.equals("content")) {
            throw new RuntimeException("Unknown value " + n2);
        }

        // delete if file already exists
        File restoreDatabase = context.getDatabasePath("restoreDatabase");
        if(restoreDatabase.exists()) {
            restoreDatabase.delete();
        }

        // create new restore database
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(restoreDatabase, null);
        db.beginTransaction();
        db.setVersion(version);

        DatabaseUtil.readDatabaseContent(reader, db);

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        reader.endObject();

        // copy file to correct location
        File restoreDatabaseFile = context.getDatabasePath("restoreDatabase");
        File actualDatabaseFile = context.getDatabasePath(DATABASE_NAME);

        DatabaseUtil.deleteRoomDatabase(context, DATABASE_NAME);

        FileUtil.copyFile(restoreDatabaseFile, actualDatabaseFile);
        Log.d("NoteRestore", "Backup Restored");

        // delete restore database
        restoreDatabaseFile.delete();

    }

    private void readPreferences(@NonNull JsonReader reader, @NonNull Context context) throws IOException {
        reader.beginObject();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        while(reader.hasNext()) {
            String name = reader.nextName();

            switch(name) {
                case "settings_use_custom_font_size":
                case "settings_del_notes":
                    pref.edit().putBoolean(name, reader.nextBoolean()).apply();
                    break;
                case "settings_font_size":
                    pref.edit().putString(name, reader.nextString()).apply();
                    break;
                default:
                    throw new RuntimeException("Unknown preference "+name);
            }
        }

        reader.endObject();
    }

    @Override
    public boolean restoreBackup(@NonNull Context context, @NonNull InputStream restoreData) {
        try {
            InputStreamReader isReader = new InputStreamReader(restoreData);
            JsonReader reader = new JsonReader(isReader);

            // START
            reader.beginObject();

            while(reader.hasNext()) {
                String type = reader.nextName();

                switch(type) {
                    case "database":
                        readDatabase(reader, context);
                        break;
                    case "preferences":
                        readPreferences(reader, context);
                        break;
                    case "files":
                        readFiles(reader, context);
                        break;
                    default:
                        throw new RuntimeException("Can not parse type "+type);
                }

            }

            reader.endObject();
            // END

            // stop app to trigger migration on wakeup
            Log.d("NoteRestore", "Restore completed successfully.");
            System.exit(0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
