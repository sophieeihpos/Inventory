package com.example.sophie.inventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.example.sophie.inventory.data.InventoryContract.CONTENT_URI;
import static com.example.sophie.inventory.data.InventoryContract.InventoryEntry.*;

/**
 * Created by Sophie on 3/19/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private TextView mIDTextView;
    private EditText mEmailEditText;
    private Button mSoldButton;
    private Button mReceivedButton;
    private Button mLoadImageButton;
    private ImageView mImageView;

    private Cursor cursor;
    private Uri currentUri;
    private Uri imageUri;
    private static final int INVENTORY_LOADER = 0;
    private static final int PICK_IMAGE_REQUEST = 1;

    private View.OnTouchListener touchListener;
    private boolean productChanged;
    private boolean permission=true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        touchListener= new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                productChanged=true;
                return false;
            }
        };

        mNameEditText = (EditText) findViewById(R.id.name_edit);
        mQuantityEditText = (EditText) findViewById(R.id.quantity_edit);
        mPriceEditText = (EditText) findViewById(R.id.price_edit);
        mEmailEditText = (EditText)findViewById(R.id.email_edit);
        mIDTextView= (TextView) findViewById(R.id.id_text);
        mSoldButton = (Button) findViewById(R.id.sold_button);
        mReceivedButton=(Button) findViewById(R.id.received_button);
        mLoadImageButton=(Button) findViewById(R.id.load_image_button);
        mImageView=(ImageView)findViewById(R.id.image_view);
        Button mOrderButton=(Button) findViewById(R.id.order_button);
        mEmailEditText.setOnTouchListener(touchListener);
        mNameEditText.setOnTouchListener(touchListener);
        mLoadImageButton.setOnTouchListener(touchListener);
        final EditText mSoldAmountText = (EditText) findViewById(R.id.no_of_sales);
        final EditText mReceivedAmountText = (EditText) findViewById(R.id.no_of_received);
        View mSoldLayout = findViewById(R.id.sold_layout);
        View mReceivedLayout=findViewById(R.id.received_layout);
        View mIDLayout=findViewById(R.id.id_layout);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permission=false;
            mLoadImageButton.setEnabled(false);
        }

        Intent intent = getIntent();
        currentUri = intent.getData();
        imageUri=null;

        if (currentUri==null){
            setTitle(R.string.title_add);
            mQuantityEditText.setOnTouchListener(touchListener);
            mPriceEditText.setOnTouchListener(touchListener);
            mSoldLayout.setVisibility(View.GONE);
            mReceivedLayout.setVisibility(View.GONE);
            mIDLayout.setVisibility(View.GONE);
            mOrderButton.setVisibility(View.GONE);
        }else{
            setTitle(R.string.title_edit);
            mPriceEditText.setEnabled(false);
            mQuantityEditText.setEnabled(false);
            LoaderManager loaderManager=getLoaderManager();
            loaderManager.initLoader(INVENTORY_LOADER,null,this);
        }

        mLoadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productName=cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                String subject=getResources().getString(R.string.order_button) ;
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT,productName);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        mSoldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                int soldAmount=Integer.parseInt(mSoldAmountText.getText().toString().trim());
                quantity=quantity-soldAmount;
                if(quantity<0){
                    Toast.makeText(getBaseContext(),R.string.review_quantity_sold,Toast.LENGTH_SHORT).show();
                }else {mQuantityEditText.setText(String.valueOf(quantity));}

            }
        });

        mReceivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                int soldAmount=Integer.parseInt(mReceivedAmountText.getText().toString().trim());
                quantity=quantity+soldAmount;
                mQuantityEditText.setText(String.valueOf(quantity));
            }
        });
    }
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == PICK_IMAGE_REQUEST) {
            if (resCode == RESULT_OK && data != null) {
                imageUri = data.getData();
                mImageView.setImageURI(imageUri);
            }
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        LoaderManager loaderManager=getLoaderManager();
        loaderManager.restartLoader(INVENTORY_LOADER,null,this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveData();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!productChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void saveData(){
        String name=mNameEditText.getText().toString().trim();
        String quantity_str = mQuantityEditText.getText().toString().trim();
        String price_str = mPriceEditText.getText().toString().trim();
        String email=mEmailEditText.getText().toString().trim();
        int quantity=-1;
        double price = -1;
        try{
            quantity= Integer.parseInt(quantity_str);
        }catch (NumberFormatException nfe){
            Toast.makeText(this,R.string.toast_quantity_invalid,Toast.LENGTH_SHORT).show();
            return;
        };
        try {
            price = Double.parseDouble(price_str);
        }catch (NumberFormatException nfe){
            Toast.makeText(this,R.string.toast_price_invalid,Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imageByte=null;
        if (imageUri!=null) {
            try {
                Bitmap imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                imageByte=getBytes(imageBitmap);
            } catch (FileNotFoundException e) {
                return;
            }
        }
        if (name.isEmpty()){
            Toast.makeText(this,R.string.toast_name_invalid,Toast.LENGTH_SHORT).show();
            return;
        }else if(quantity<0) {
            Toast.makeText(this,R.string.toast_quantity_invalid,Toast.LENGTH_SHORT).show();
            return;
        }
        else if(price<0) {
            Toast.makeText(this, R.string.toast_price_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        else if(email.isEmpty()){
            Toast.makeText(this, R.string.toast_email_invalid, Toast.LENGTH_SHORT).show();
            return;
        }else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME,name);
            values.put(COLUMN_QUANTITY, quantity);
            values.put(COLUMN_PRICE,price);
            values.put(COLUMN_EMAIL,email);
            if(imageByte!=null){
                values.put(COLUMN_IMAGE,imageByte);
            }
            if(currentUri==null){
                getContentResolver().insert(CONTENT_URI,values);
                imageUri=null;
            }else {
                getContentResolver().update(currentUri,values,null,null);
                imageUri=null;
            }
            finish();
        }
    }
    public void deleteData(){
        if (currentUri!=null){
            getContentResolver().delete(currentUri,null,null);
            finish();
        }
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(currentUri==null){
            return null;
        }else {
            return new CursorLoader(this, currentUri, null, null, null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursor=data;
        cursor.moveToPosition(0);
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        String name =cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        int quantity =cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
        double price =cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));
        String email=cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
        int id = cursor.getInt(cursor.getColumnIndex(_ID));
        byte[] imageByte = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE));
        if(imageByte!=null){
            Bitmap imageBitmap= getImage(imageByte);
            if(imageUri==null&& permission==true ){
                mImageView.setImageBitmap(imageBitmap);
            }
        }
        mNameEditText.setText(name);
        mQuantityEditText.setText(String.valueOf(quantity));
        mPriceEditText.setText(String.valueOf(price) );
        mEmailEditText.setText(email);
        mIDTextView.setText(String.valueOf(id));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursor=null;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteData();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!productChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
