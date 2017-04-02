package com.example.sophie.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.sophie.inventory.data.InventoryContract.CONTENT_URI;
import static com.example.sophie.inventory.data.InventoryContract.InventoryEntry.*;

/**
 * Created by Sophie on 3/20/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    private Uri currentUri;
    private int quantity;

    public InventoryCursorAdapter(Context context,Cursor cursor){
        super(context,cursor,0);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name_edit);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_edit);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_edit);
        String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        quantity=cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
        double price = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));

        nameTextView.setText(name);
        quantityTextView.setText(String.valueOf(quantity));
        priceTextView.setText(String.valueOf(price));

        final Button saleButton = (Button) view.findViewById(R.id.sale_button);
        int cursorPosition=cursor.getPosition();
        saleButton.setTag(cursorPosition);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cursorPosition= (int) saleButton.getTag();
                cursor.moveToPosition(cursorPosition);
                int id = cursor.getInt(cursor.getColumnIndex(_ID));
                currentUri= ContentUris.withAppendedId(CONTENT_URI,id);
                quantity=cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
                if (quantity>0){
                    quantity=quantity-1;
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_QUANTITY,quantity);
                    context.getContentResolver().update(currentUri,values,null,null);
                }else {
                    Toast.makeText(context,R.string.toast_no_sale,Toast.LENGTH_SHORT).show();
                }
            }
        });
        final View productView = view.findViewById(R.id.product_layout);
        productView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cursorPosition= (int) saleButton.getTag();
                cursor.moveToPosition(cursorPosition);
                int id = cursor.getInt(cursor.getColumnIndex(_ID));
                Uri uri= ContentUris.withAppendedId(CONTENT_URI,id);
                Intent intent =new Intent(context,EditorActivity.class);
                intent.setData(uri);
                context.startActivity(intent);
            }
        });
    }

}
