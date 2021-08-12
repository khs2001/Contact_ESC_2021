package com.example.contact_esc_2021;

import android.app.AlertDialog;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
    private Context context;
    private ArrayList<Contact> datalist;

    public Adapter(Context context,ArrayList<Contact> datalist){
        this.context=context;
        this.datalist=datalist;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.recy_item,parent,false);
        Holder holder=new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Contact contact=datalist.get(position);
        holder.name.setText(contact.getName());
        holder.phonenum.setText(contact.getPhoneNumber());

        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("전화를 걸지, 문자를 보낼지 선택하시오.");
                builder.setMessage("골라주세요.");
                builder.setNeutralButton("전화걸기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String number=datalist.get(position).getPhoneNumber();
                        Uri numberU= Uri.parse("tel:"+number);
                        Intent call=new Intent(Intent.ACTION_CALL,numberU);
                        context.startActivity(call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                });
                builder.setPositiveButton("문자 보내기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri smsUri=Uri.parse("tel:"+datalist.get(position).getPhoneNumber());
                        Intent intent=new Intent(Intent.ACTION_VIEW,smsUri);
                        intent.putExtra("address",contact.getPhoneNumber());
                        intent.putExtra("sms_body","");
                        intent.setType("vnd.android-dir/mms-sms");
                        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                });
                builder.create().show();
            }
        });
        holder.item_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("삭제");
                builder.setMessage("정말 이 연락처를 삭제하시겠습니까?");
                builder.setNegativeButton("아니오",null);
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteContactFromNumber(context.getContentResolver(),datalist.get(position).getPhoneNumber());
                        datalist.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position,datalist.size());
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        protected ConstraintLayout item_container;
        protected TextView name;
        protected TextView phonenum;
        protected Button call;

        public Holder(@NonNull View itemView) {
            super(itemView);

            item_container=itemView.findViewById(R.id.item_container);
            name=itemView.findViewById(R.id.name);
            phonenum=itemView.findViewById(R.id.phonenum);
            call=itemView.findViewById(R.id.call);
        }
    }

    public void filterList(ArrayList<Contact> filteredList){
        datalist=filteredList;
        notifyDataSetChanged();
    }
    private static long getContactIDFromNumber(ContentResolver contactHelper,String number){

        Uri contactUri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));

        String[] projection={ContactsContract.PhoneLookup._ID};

        Cursor cursor=contactHelper.query(contactUri,projection,null,null,null);

        if(cursor.moveToFirst()){
            return cursor.getLong(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
        }
        else if(cursor!=null){
            cursor.close();
        }
        return -1;
    }
    public static void deleteContactFromNumber(ContentResolver contentHelper,String number){

        ArrayList<ContentProviderOperation> ops=new ArrayList<>();
        String[] WhereArgs=new String[]{String.valueOf(getContactIDFromNumber(contentHelper,number))};

        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
        .withSelection(ContactsContract.RawContacts.CONTACT_ID+"?=",WhereArgs).build());

        try {
            contentHelper.applyBatch(ContactsContract.AUTHORITY,ops);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }




}
