package com.example.sophie.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.sophie.inventory.R;

import static com.example.sophie.inventory.data.InventoryContract.*;
import static com.example.sophie.inventory.data.InventoryContract.InventoryEntry.*;

/**
 * Created by Sophie on 3/20/2017.
 */

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    private InventoryDbHelper inventoryDbHelper;
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY,PATH_INVENTORY,PRODUCTS);
        sUriMatcher.addURI(CONTENT_AUTHORITY,PATH_INVENTORY+"/#",PRODUCT_ID);
    }
    @Override
    public boolean onCreate() {
        inventoryDbHelper=new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = inventoryDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor=database.query(TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int delete=0;
        switch (match) {
            case PRODUCTS:
                delete= database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                delete= database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        database.close();
        if (delete==0){
            Toast toast = Toast.makeText(getContext(), R.string.toast_delete_failed, Toast.LENGTH_SHORT);
            toast.show();
        }else {
            Toast toast = Toast.makeText(getContext(), R.string.toast_delete_successful, Toast.LENGTH_SHORT);
            toast.show();
        }
        return delete;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        Long id=database.insert(TABLE_NAME,null,values);

        if (id==-1){
            Toast toast = Toast.makeText(getContext(), R.string.toast_insert_failed, Toast.LENGTH_SHORT);
            toast.show();
        }else{
            Toast toast = Toast.makeText(getContext(), R.string.toast_data_saved, Toast.LENGTH_SHORT);
            toast.show();
        }

        database.close();
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, id);
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        int update=database.update(TABLE_NAME,values,selection,selectionArgs);
        if (update==1){
            Toast toast = Toast.makeText(getContext(), R.string.update_successful, Toast.LENGTH_SHORT);
            toast.show();
        }else{
            Toast toast = Toast.makeText(getContext(), R.string.update_fail, Toast.LENGTH_SHORT);
            toast.show();
        }
        database.close();
        getContext().getContentResolver().notifyChange(uri,null);
        return update;
    }
}
