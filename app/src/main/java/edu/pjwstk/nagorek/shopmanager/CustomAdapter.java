package edu.pjwstk.nagorek.shopmanager;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import edu.pjwstk.nagorek.shopmanager.components.Shop;

public class CustomAdapter extends BaseAdapter implements ListAdapter {

    private List<Shop> shopList;
    private Context context;

    public CustomAdapter(Context context, List<Shop> shopList) {
        this.shopList=shopList;
        this.context=context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) { }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) { }

    @Override
    public int getCount() {
        return shopList != null ? shopList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Shop shop = shopList.get(position);

        if(convertView == null) {

            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_row, null);

            convertView.setOnClickListener(v -> {

            });

            TextView description = convertView.findViewById(R.id.stype);
            ImageView image = convertView.findViewById(R.id.list_image);

            if(shop.getPhoto() != null){
                byte[] decodedString = Base64.decode(shop.getPhoto(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                image.setImageBitmap(decodedByte);
            } else {
                Drawable drawable = ResourcesCompat.getDrawable(convertView.getResources(), R.mipmap.ic_launcher, null);
                image.setImageDrawable(drawable);
            }

            description.setText("NAME: " + shop.getName() + "\nTYPE: " + shop.getType() + "\nRANGE: " + shop.getRadius());
        }

        ImageView deleteBtn = convertView.findViewById(R.id.delete_btn);
        ImageView addBtn = convertView.findViewById(R.id.add_btn);

        deleteBtn.setOnClickListener(v -> {

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.keepSynced(true);

            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            mDatabase.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    GenericTypeIndicator<List<Shop>> genericTypeIndicator = new GenericTypeIndicator<List<Shop>>() {};
                    List<Shop> shopListDb = dataSnapshot.getValue(genericTypeIndicator);
                    shopListDb.remove(position);
                    mDatabase.child(id).setValue(shopListDb);
                    shopList = shopListDb;
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println(databaseError.toString());
                }
            });
        });
        addBtn.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setClass(v.getContext(), EditActivity.class);
            i.putExtra("position", position);
            i.putExtra("shop", shop);
            v.getContext().startActivity(i);
        });

        return convertView;
    }
}