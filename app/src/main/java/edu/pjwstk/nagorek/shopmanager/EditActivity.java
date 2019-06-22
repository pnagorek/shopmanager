package edu.pjwstk.nagorek.shopmanager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.pjwstk.nagorek.shopmanager.components.Shop;

public class EditActivity extends AppCompatActivity {

    private int TAKE_PHOTO_CODE = 1;
    private String currentPhotoPath;
    private Button btnPhoto, btnSubmit;
    private ImageView photoImage;
    private Shop shop = null;
    private TextView sname, stype, sradius, stitle;
    private int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        btnPhoto = findViewById(R.id.btn_photo);
        btnSubmit = findViewById(R.id.btn_submit);
        photoImage = findViewById(R.id.photoImage);

        sname = findViewById(R.id.sname);
        stype = findViewById(R.id.stype);
        sradius = findViewById(R.id.sradius);
        stitle = findViewById(R.id.stitle);

        this.shop = (Shop) getIntent().getSerializableExtra("shop");
        if(this.shop == null)
        {
            stitle.setText("Add new shop");
            this.shop = new Shop();
        }
        else
        {
            stitle.setText("Edit shop");
            btnSubmit.setText("Update");
            this.position = getIntent().getIntExtra("position", -1);
            sname.setText(shop.getName());
            stype.setText(shop.getType());
            sradius.setText(shop.getRadius());

            if(shop.getPhoto() != null){
                byte[] decodedString = Base64.decode(shop.getPhoto(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                photoImage.setImageBitmap((Bitmap.createScaledBitmap(decodedByte, 200,200, true)));
            } else {
                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher, null);
                photoImage.setImageDrawable(drawable);
            }
        }

        btnPhoto.setOnClickListener(v -> {

            File photoFile = null;
            Uri outputFileUri = null;
            try {
                photoFile = createImageFile();
                outputFileUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
        });

        btnSubmit.setOnClickListener(v -> {

            this.shop.setName(sname.getText().toString());
            this.shop.setType(stype.getText().toString());
            this.shop.setRadius(sradius.getText().toString());

            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.keepSynced(true);

            mDatabase.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<Shop>> genericTypeIndicator = new GenericTypeIndicator<List<Shop>>() {};
                    List<Shop> shopList = dataSnapshot.getValue(genericTypeIndicator);
                    if(shopList == null){
                        shopList = new ArrayList<>();
                    }
                    if(position != -1){
                        shopList.set(position, shop);
                    }
                    else{
                        shopList.add(shop);
                    }
                    mDatabase.child(id).setValue(shopList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println(databaseError.toString());
                }
            });

            NavUtils.navigateUpFromSameTask(this);
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            final Uri imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", new File(currentPhotoPath));
            final InputStream imageStream;
            try {
                imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                String encodedImage = encodeImage(selectedImage);
                this.shop.setPhoto(encodedImage);
                photoImage.setImageBitmap(Bitmap.createScaledBitmap(selectedImage, 200,200, true));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String encodeImage(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }
}
