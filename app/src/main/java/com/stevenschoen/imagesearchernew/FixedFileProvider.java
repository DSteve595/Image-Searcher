package com.stevenschoen.imagesearchernew;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.Arrays;

/* Found at http://stackoverflow.com/a/25020642/919716 */

public class FixedFileProvider extends android.support.v4.content.FileProvider {

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor source = super.query(uri, projection, selection, selectionArgs, sortOrder);

        String[] columnNames = source.getColumnNames();
        String[] newColumnNames = columnNamesWithData(columnNames);
        MatrixCursor cursor = new MatrixCursor(newColumnNames, source.getCount());

        source.moveToPosition(-1);
        while (source.moveToNext()) {
            MatrixCursor.RowBuilder row = cursor.newRow();
            for (int i = 0; i < columnNames.length; i++) {
                row.add(source.getString(i));
            }
        }

        return cursor;
    }

    private String[] columnNamesWithData(String[] columnNames) {
        for (String columnName : columnNames)
            if (MediaStore.MediaColumns.DATA.equals(columnName))
                return columnNames;

        String[] newColumnNames = Arrays.copyOf(columnNames, columnNames.length + 1);
        newColumnNames[columnNames.length] = MediaStore.MediaColumns.DATA;
        return newColumnNames;
    }
}