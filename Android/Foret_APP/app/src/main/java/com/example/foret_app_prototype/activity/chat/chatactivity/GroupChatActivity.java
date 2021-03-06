package com.example.foret_app_prototype.activity.chat.chatactivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foret_app_prototype.R;
import com.example.foret_app_prototype.activity.notify.APIService;
import com.example.foret_app_prototype.activity.notify.Client;
import com.example.foret_app_prototype.activity.notify.Data;
import com.example.foret_app_prototype.activity.notify.Response;
import com.example.foret_app_prototype.activity.notify.Sender;
import com.example.foret_app_prototype.activity.notify.Token;
import com.example.foret_app_prototype.adapter.chat.GroupChatAdapter;
import com.example.foret_app_prototype.helper.FileUtils;
import com.example.foret_app_prototype.helper.PhotoHelper;
import com.example.foret_app_prototype.helper.ProgressDialogHelper;
import com.example.foret_app_prototype.model.ModelGroupChat;
import com.example.foret_app_prototype.model.ModelGroupChatList;
import com.example.foret_app_prototype.model.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {

    String grounId, grounName, grounLeader;

    CircleImageView groupImage;
    TextView groupName;
    EditText messageEt;
    ImageButton attachButton, sendBtn;
    RecyclerView chat_recyclerView;
    Button buttonInvite;

    Activity activity;
    List<String> list;

    int countSeen;

    String myPhotoUri;

    List<ModelGroupChat> groupChatList;
    GroupChatAdapter chatAdapter;

    File file;
    String filepath;

    Context context;

    //????????? ?????? uri
    Uri image_rui = null;

    FirebaseAuth firebaseAuth;
    //????????????
    APIService apiService;
    boolean notify = false;

    String myUid;
    String hisUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // ????????? ?????? ??????
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.foret4));

        context = this;
        activity = this;
        grounName = getIntent().getStringExtra("grounName");
        grounId = getIntent().getStringExtra("grounId");

        groupImage = findViewById(R.id.groupImage);
        groupName = findViewById(R.id.groupName);
        messageEt = findViewById(R.id.messagaEt);
        attachButton = findViewById(R.id.attachButton);
        sendBtn = findViewById(R.id.sendBtn);
        buttonInvite = findViewById(R.id.buttonInvite);
        buttonInvite.setVisibility(View.GONE);


        //?????? ?????? ??????
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);


        chat_recyclerView = findViewById(R.id.chat_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chat_recyclerView.setHasFixedSize(true);
        chat_recyclerView.setLayoutManager(linearLayoutManager);


        sendBtn.setOnClickListener(this);
        attachButton.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getCurrentUser().getUid();
        //????????? ??????
        getImageFromFirebaseStorage();
        groupName.setText(grounName);
        myPhotoUri = "";

        loadGroupInfo();
        loadGroupMessage();
        loadAmILeader();

    }


    private void loadAmILeader() {
        //Log.e("[test]","?????? ???????????? ??????");
        //?????? ?????? ????????????
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(grounName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelGroupChatList model = snapshot.getValue(ModelGroupChatList.class);
                grounLeader = model.getGroupLeader();
                //  Log.e("[test]","grounLeader????"+grounLeader);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //?????? ?????????????
        ref.child(grounName).child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    //if(ds.child("uid").getValue().equals(user.getUid())){
                    if (ds.getKey().equals(user.getUid())) {
                        String myNickname = "" + ds.child("participantName").getValue();
                        buttonInvite.setVisibility(View.VISIBLE);
                        buttonInvite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, GroupJoinActivity.class);
                                intent.putExtra("groudId", grounId);
                                intent.putExtra("grounName", grounName);
                                intent.putExtra("grounLeader", grounLeader);
                                startActivity(intent);
                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadGroupMessage() {
        groupChatList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("GroupChats");
        ref.child(grounName).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //   Log.e("[test]", ds.getRef() + "loadGroupMessage ??????");
                    ModelGroupChat chat = ds.getValue(ModelGroupChat.class);
                    //   Log.e("[test]", chat.getSender());
                    groupChatList.add(chat);

                    chatAdapter = new GroupChatAdapter(GroupChatActivity.this, groupChatList, grounName);
                    chat_recyclerView.setAdapter(chatAdapter);
                    chat_recyclerView.scrollToPosition(groupChatList.size()-1);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.attachButton:
                showItemSelectListDialog();

                break;
            case R.id.sendBtn:
                String message = messageEt.getText().toString().trim();
                if (message.equals("") || message == null) {
                    //?????? ??????
                } else {
                    sendMessage(message);
                    messageEt.setText("");
                    notify = true;
                }
                break;

        }
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("GroupChats");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", user.getUid());
        hashMap.put("message", message);
        hashMap.put("type", "text");
        hashMap.put("countSeen", countSeen);
        hashMap.put("timestamp", timestamp);
        hashMap.put("senderPhoto", myPhotoUri);

        databaseReference.child(String.valueOf(grounName)).child("Messages").child(timestamp)
                .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //??????
                messageEt.setText("");

                hisUid = "";
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(grounName).child("participants").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            hisUid = ds.getKey();
                            //
                            if(!myUid.equals(ds.getKey())){
                                updateNewItem("MESSAGE_NEW_ITEM",myUid,hisUid,message,""+System.currentTimeMillis());
                                String msg = message;
                                //??????
                                DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                                database.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ModelUser user = snapshot.getValue(ModelUser.class);

                                        if(notify){
                                            sendNotification(hisUid,user.getNickname(),message);
                                        }
                                        notify = false;
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //??????

            }
        });

    }

    //????????? ???????????? ????????? ?????? ??????
    public void getImageFromFirebaseStorage() {
        StorageReference islandRef = FirebaseStorage.getInstance().getReference("ChatImages").child("iu.jpeg");

        islandRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(activity)
                        .load(islandRef)
                        .into(groupImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Toast.makeText(activity, "???????????? ??????", Toast.LENGTH_LONG).show();
                Glide.with(activity)
                        .load(R.drawable.icon_foret)
                        .into(groupImage);
            }
        });
    }

    private void loadGroupInfo() {
        list = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.child("GroupName").getValue().equals(grounName)) {
                        String convert = "" + ds.child("GroupCurrentJoinedMember").getValue();
                      //  Log.e("[test]", "ds.child(\"GroupCurrentJoinedMember\").getValue()?" + ds.child("GroupCurrentJoinedMember").getValue());
                        countSeen = Integer.parseInt(convert);
                        HashMap<String, Object> update = new HashMap<>();
                        update.put("GroupCurrentJoinedMember", "" + ds.child("participants").getChildrenCount());
                        reference.child(grounName).updateChildren(update);
                        /*
                        String participant = "" + ds.getChildrenCount();
                        Log.e("[test]","participant?" +participant);
                        Log.e("[test]","countSeen?" +countSeen);

                        list.add(participant);
                        Log.e("[test]","?????? ???????" + list.size());
                                                */
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity, "?????? ????????? ????????????.", Toast.LENGTH_LONG).show();
            }
        });

    }

    //????????? ??????
    private void showItemSelectListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] items = {"?????? ?????????", "????????? ?????????"};

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                switch (which) {
                    case 0: // ???????????????
                        showListDialog();
                        break;

                    case 1: // ??????????????????
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("video/*");
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(intent, 300);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //?????? ?????? ????????? ??????
    private void showListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] items = {"?????? ????????????", "??????????????? ????????????"};

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                switch (which) {
                    case 0: // ?????? ???????????? ??????
                        filepath = PhotoHelper.getInstance().getNewPhotoPath();
                        // ????????? ??? ??????
                        file = new File(filepath);
                        image_rui = null;
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            image_rui = FileProvider.getUriForFile(GroupChatActivity.this,
                                    getApplicationContext().getPackageName() + ".fileprovider", file);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        } else {
                            image_rui = Uri.fromFile(file);
                        }
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
                        intent.putExtra(AUDIO_SERVICE, false);
                        startActivityForResult(intent, 100);
                        break;


                    case 1: // ??????????????? ????????????
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(intent, 200);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 100:   // ????????? ??? ?????? ???
                    Intent photoIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(filepath));
                    sendBroadcast(photoIntent);
                    sendImageMessage(image_rui);

                    break;
                case 200:   // ????????? ??? ?????? ???

                    //???????????? ????????? ?????? ??????
                    image_rui = null;
                    image_rui = data.getData();
                    sendImageMessage(image_rui);

                    break;
                case 300:
                    Uri uri = data.getData();
                    String filepath = FileUtils.getPath(this, uri);
                    file = null;
                    file = new File(filepath);
                    sendVideo(file);
                    break;
            }
        }
    }



    //????????? ?????????
    private void sendVideo(File file) {
        notify = true;
        ProgressDialogHelper.getInstance().getProgressbar(this, "????????? ??????????????????..");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String timeStamp = "" + System.currentTimeMillis();
        String fileNameAndPath = "ChatGroupVideos/" + "post_" + timeStamp + " by " + user.getUid() + " file : ";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        Uri file1 = Uri.fromFile(file);

        StorageReference riversRef = storageRef.child(fileNameAndPath + file1.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file1);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                ProgressDialogHelper.getInstance().removeProgressbar();
                Toast.makeText(context, "????????? ??????", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                ProgressDialogHelper.getInstance().removeProgressbar();
                Toast.makeText(context, "????????? ??????", Toast.LENGTH_LONG).show();

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("GroupChats");

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", user.getUid());
                    hashMap.put("message", downloadUri);
                    hashMap.put("timestamp", timeStamp);
                    hashMap.put("type", "video");
                    hashMap.put("countSeen", countSeen);
                    hashMap.put("senderPhoto", myPhotoUri);
                    databaseReference.child(String.valueOf(grounName)).child("Messages").child(timeStamp)
                            .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //??????
                            Toast.makeText(activity, "????????? ????????? ??????", Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //??????

                        }
                    });


                }
            }
        });

    }

    //????????? ?????????
    private void sendImageMessage(Uri image_rui) {
        notify = true;
        ProgressDialogHelper.getInstance().getProgressbar(this, "????????? ??????????????????..");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        String timeStamp = "" + System.currentTimeMillis();
        String fileNameAndPath = "ChatGroupImages/" + "post_" + timeStamp + " by " + user.getUid() + " file : ";

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_rui);
            ByteArrayOutputStream baos = null;
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath + image_rui.getLastPathSegment());
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ProgressDialogHelper.getInstance().removeProgressbar();
                            Toast.makeText(context, "????????? ??????", Toast.LENGTH_LONG).show();
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            String downloadUri = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()) {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("GroupChats");

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", user.getUid());
                                hashMap.put("message", downloadUri);
                                hashMap.put("timestamp", timeStamp);
                                hashMap.put("type", "image");
                                hashMap.put("countSeen", countSeen);
                                hashMap.put("senderPhoto", myPhotoUri);

                                databaseReference.child(String.valueOf(grounName)).child("Messages").child(timeStamp)
                                        .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //??????
                                        Toast.makeText(activity, "?????? ????????? ??????", Toast.LENGTH_LONG).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //??????

                                    }
                                });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ProgressDialogHelper.getInstance().removeProgressbar();
                            Toast.makeText(context, "????????? ??????", Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //???????????????
    public void updateNewItem(String type,String sender,String receiver, String content ,String time){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notify");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("type",type);
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("content",content);
        hashMap.put("time",time);
        hashMap.put("isSeen",false);

        ref.child(receiver).push().setValue(hashMap);
    }

    // ?????? ?????? ??????.
    private void sendNotification(String hisUid, String nickname, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid); //?????? ??????
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for(DataSnapshot ds : snapshot.getChildren()){
                    //?????? ????????? ????????? ??????
                    Token token = ds.getValue(Token.class);

                    //????????? ??????
                    Data data = new Data(myUid,nickname+" : "+message,"New Message",hisUid,R.drawable.foret_logo);

                    //????????? ?????? ??????
                    Sender sender = new Sender(data, token.getToken());
                    //??????
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    //Toast.makeText(ChatActivity.this,""+response.message(),Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    //?????? ?????? ??????
    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //????????? ???????????? ??????
            updateuserActiveStatusOn();
        } else {
            //????????? ????????? ?????? ??????
            Toast.makeText(this, "???????????? ???????????????.", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //updateuserActiveStatusOff();
    }

    //????????? ?????? ?????????
    private void updateuserActiveStatusOn() {
        FirebaseUser currentUseruser = FirebaseAuth.getInstance().getCurrentUser();
        final String userUid = currentUseruser.getUid();
        DatabaseReference userAcitive = FirebaseDatabase.getInstance().getReference("Users").child(userUid);
        HashMap<String, Object> onlineStatus = new HashMap<>();
        onlineStatus.put("onlineStatus", "online");
        onlineStatus.put("listlogined_date", "?????? ?????????");
        userAcitive.updateChildren(onlineStatus);
    }
    //???????????? ?????? ?????????
    private void updateuserActiveStatusOff() {
        FirebaseUser currentUseruser = FirebaseAuth.getInstance().getCurrentUser();
        final String userUid = currentUseruser.getUid();
        DatabaseReference userAcitive = FirebaseDatabase.getInstance().getReference("Users").child(userUid);
        HashMap<String, Object> onlineStatus = new HashMap<>();
        onlineStatus.put("onlineStatus", "offline");

        java.util.Calendar cal = java.util.Calendar.getInstance(Locale.KOREAN);
        cal.setTimeInMillis(Long.parseLong(String.valueOf(System.currentTimeMillis())));
        String dateTime = DateFormat.format("yy/MM/dd hh:mm aa", cal).toString();

        onlineStatus.put("listlogined_date", "Last Seen at : " + dateTime);
        userAcitive.updateChildren(onlineStatus);
    }


}