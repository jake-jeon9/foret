package com.example.foret_app_prototype.activity.foret.board;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.foret_app_prototype.R;
import com.example.foret_app_prototype.activity.MainActivity;
import com.example.foret_app_prototype.activity.foret.ViewForetActivity;
import com.example.foret_app_prototype.activity.free.EditFreeActivity;
import com.example.foret_app_prototype.activity.free.ReadFreeActivity;
import com.example.foret_app_prototype.activity.login.SessionManager;
import com.example.foret_app_prototype.activity.notify.APIService;
import com.example.foret_app_prototype.activity.notify.Client;
import com.example.foret_app_prototype.activity.notify.Data;
import com.example.foret_app_prototype.activity.notify.Response;
import com.example.foret_app_prototype.activity.notify.Sender;
import com.example.foret_app_prototype.activity.notify.Token;
import com.example.foret_app_prototype.adapter.foret.BoardViewPagerAdapter;
import com.example.foret_app_prototype.adapter.foret.CommentBoardAdapter;
import com.example.foret_app_prototype.adapter.foret.CommentsAdapter;
import com.example.foret_app_prototype.adapter.foret.ReadViewPagerAdapter;
import com.example.foret_app_prototype.adapter.free.CommentListFreeBoardAdapter;
import com.example.foret_app_prototype.helper.getIPAdress;
import com.example.foret_app_prototype.model.FBCommentDTO;
import com.example.foret_app_prototype.model.ForetBoard;
import com.example.foret_app_prototype.model.ForetBoardComment;
import com.example.foret_app_prototype.model.ForetBoardDTO;
import com.example.foret_app_prototype.model.MemberDTO;
import com.example.foret_app_prototype.model.ModelUser;
import com.example.foret_app_prototype.model.ReadForetDTO;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class ReadForetBoardActivity extends AppCompatActivity implements View.OnClickListener, CommentBoardAdapter.CommentClickListener {
    MemberDTO memberDTO;
    ReadForetDTO foretBoardDTO;

    TextView textView_writer, textView_like, textView_subject, textView_date,
            textView_reply, textView_content, textView_comment2;
    ToggleButton likeButton;
    ImageView button_cancel, image_profile; //?????? ??????
    EditText editText_comment;
    RecyclerView recyclerView;
    Button button_input; //?????? ?????? ??????

    int memberID;
    int board_id;
    Intent intent;
    String url = "";
    AsyncHttpClient client;
    MemberResponse memberResponse;
//    BoardResponse boardResponse;

    ViewFreeBoardResponse viewResponse;
    InsertCommentResponse writeCommentResponse;
    ViewFreeBoardResponse readBoardResponse;
    LikeChangeResponse likeChangeResponse;
    DeleteBoardResponse deleteBoardResponse;
    String wirterPhotoGet;
    CommentBoardAdapter adapter;
    List<FBCommentDTO> commentlist;
    FBCommentDTO foretBoardComment;

    ViewPager imageViewpager;
    TabLayout tab_layout;
    ReadViewPagerAdapter readViewPagerAdapter;

    int noGet = 0;
    int like_count;
    int comment_count;
    InputMethodManager inputMethodManager;
    String target;
    boolean replying = false;
    int initial_likecount; //?????? ?????? ?????? ?????? ?????? ????????? ?????? ?????? ??????

    // ????????????
    APIService apiService;
    boolean notify = false;
    String myUid;
    String hisUid;
    String takeMessage;
    String takerSender;
    String takeReceiver;
    String originalReciver;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_foret_board);
        context =this;
        // ????????? ?????? ??????
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.foret4));

        //?????? ??????
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // ?????? ????????? ??????
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // ???????????? ??????

        textView_writer = findViewById(R.id.textView_writer);
        textView_like = findViewById(R.id.textView_like); //????????? ??????
        textView_subject = findViewById(R.id.textView_subject);
        textView_date = findViewById(R.id.textView_date);
        textView_reply = findViewById(R.id.textView_reply); //OO?????? ?????? ?????????????????? ?????????
        textView_comment2 = findViewById(R.id.textView_comment2);
        textView_content = findViewById(R.id.textView_content);
        likeButton = findViewById(R.id.likeButton); //????????? ??????
        button_cancel = findViewById(R.id.button_cancel); //???????????? ??????
        editText_comment = findViewById(R.id.editText_comment); //????????????
        recyclerView = findViewById(R.id.recyclerView); //????????? ?????? ??????????????????
        button_input = findViewById(R.id.button_input);
        image_profile = findViewById(R.id.image_profile);
        tab_layout = findViewById(R.id.tab_layout);
        imageViewpager = findViewById(R.id.imageViewpager);


        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//        SessionManager sessionManager = new SessionManager(this);
//        memberID = sessionManager.getSession();

        commentlist = new ArrayList<>();
        client = new AsyncHttpClient();
        viewResponse = new ViewFreeBoardResponse();
        writeCommentResponse = new InsertCommentResponse();
        readBoardResponse = new ViewFreeBoardResponse();
        likeChangeResponse = new LikeChangeResponse();
        deleteBoardResponse = new DeleteBoardResponse();

        board_id = getIntent().getIntExtra("board_id", 0);
        memberDTO = (MemberDTO) getIntent().getSerializableExtra("memberDTO");


        getMember(memberDTO.getId());

        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        textView_reply.setVisibility(View.GONE);
        button_cancel.setVisibility(View.GONE);
        button_cancel.setOnClickListener(this); //?????? ?????? ??????
        button_input.setOnClickListener(this); //???????????? ->?????? ????????? ???
        likeButton.setOnClickListener(this); //???????????????
        // ?????? ?????? ??????
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
    }

    private void getBoard(int board_id) {
        //??? ????????????
        url = getIPAdress.getInstance().getIp()+"/foret/search/boardSelect.do";
        RequestParams params = new RequestParams();
        params.put("id", board_id);
        client.post(url, params, viewResponse);
    }

    //????????? ??? ?????? ????????? ????????? ???????????? ????????? ??????????????? ??????
    private void setDataBoard(ReadForetDTO foretBoardDTO) {

//        if (foretBoardDTO.getWriter_photo() != null) {
//            Glide.with(this).load(foretBoardDTO.getWriter_photo())
//                    .placeholder(R.drawable.icon_defalut).into(image_profile);
//        } else {
//            Glide.with(this).load(R.drawable.icon4).into(image_profile);
//        }
            Glide.with(this).load(foretBoardDTO.getWriter_photo()).
                    placeholder(R.drawable.icon4).
                    error(R.drawable.icon4)
                    .into(image_profile);



        textView_writer.setText(foretBoardDTO.getWriter_nickname());
        textView_subject.setText(foretBoardDTO.getSubject());
        textView_content.setText(foretBoardDTO.getContent());
        textView_like.setText(foretBoardDTO.getBoard_like() + "");
        textView_date.setText(foretBoardDTO.getReg_date());
        textView_comment2.setText(foretBoardDTO.getBoard_comment() + "");
        if (foretBoardDTO.isLike()) {
            likeButton.setChecked(true);
        }

        int photoSize = foretBoardDTO.getPhoto().size();
        List<String> photolist = new ArrayList<>();

        List<String> test = foretBoardDTO.getPhoto();


        for (int a = 0; a < photoSize; a++) {
            if(!test.removeAll(Collections.singleton(null))){
                Log.e("[test]", "?????? ???????" + foretBoardDTO.getPhoto().get(a).replace("[", "").replace("]", ""));
                photolist.add(getIPAdress.getInstance().getIp()+"/foret/storage/" + foretBoardDTO.getPhoto().get(a).toString().replace("[", "").replace("]", ""));
            }

        }


        foretBoardDTO.setPhoto(photolist);

        // ????????????
        readViewPagerAdapter = new ReadViewPagerAdapter(this, foretBoardDTO);
        if (foretBoardDTO.getPhoto().size() != 0) {
            imageViewpager.setAdapter(readViewPagerAdapter);
            tab_layout.setupWithViewPager(imageViewpager, true);
        } else {
            imageViewpager.setVisibility(View.GONE);
            tab_layout.setVisibility(View.GONE);
        }
        //????????? ?????????
        takeReceiver = foretBoardDTO.getWriter() + "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (noGet != 0) {
            commentlist.clear();
            //??? ????????????
            url = getIPAdress.getInstance().getIp()+"/foret/search/boardSelect.do";
            RequestParams params = new RequestParams();
            params.put("id", foretBoardDTO.getId());
            client.post(url, params, viewResponse);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if(memberDTO.getId() == foretBoardDTO.getWriter()) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.edit_toolbar, menu);
//        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // ????????????
                finish();
                break;
            case R.id.btn_modify: // ??????
                intent = new Intent(this, EditForetBoardActivity.class);
                intent.putExtra("readForetDTO", foretBoardDTO);
                startActivity(intent);
                break;
            case R.id.btn_delete: // ??????
                getDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_cancel: //?????? ?????? ??????
                textView_reply.setVisibility(View.GONE);
                button_cancel.setVisibility(View.GONE);
                editText_comment.setText("");
                replying = false;
                break;
            case R.id.button_input: //?????? ??????
                inputComment();
                break;
            case R.id.likeButton: //????????? ??????
                if (likeButton.isChecked()) {
                    like_count++;
                    textView_like.setText(like_count + "");
                } else {
                    like_count--;
                    textView_like.setText(like_count + "");
                }
                break;
        }
    }

    private void inputComment() { //?????? ??????
        foretBoardComment = new FBCommentDTO();
        foretBoardComment.setWriter((memberDTO.getId()));
        String comment = editText_comment.getText().toString().trim();
        if(comment.equals("")) {
            Toast.makeText(this, "????????? ???????????????.", Toast.LENGTH_SHORT).show();
            return;
        }
        foretBoardComment.setId(foretBoardDTO.getId());
        foretBoardComment.setContent(comment);

        RequestParams params = new RequestParams();
        url = getIPAdress.getInstance().getIp()+"/foret/comment/recomment_insert.do";
        params.put("board_id", foretBoardDTO.getId());
        params.put("writer", memberDTO.getId());
        params.put("content", editText_comment.getText().toString().trim());

        takeMessage = editText_comment.getText().toString().trim();

        client.post(url, params, writeCommentResponse);
    }

    //???????????? ????????? ???
    @Override
    public void onReplyButtonClick(View v, String target, boolean reply) {
        if (reply) {
            textView_reply.setText(target + "????????? ?????? ????????? ?????????.");
            editText_comment.setText("@" + target + " ");
            textView_reply.setVisibility(View.VISIBLE);
            button_cancel.setVisibility(View.VISIBLE);
            this.target = target;
            replying = true;
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    //???????????? ????????? ???
    @Override
    public void onModifyButtonClick(View v, boolean modify) {
        if (modify) {
            textView_reply.setVisibility(View.GONE);
            button_cancel.setVisibility(View.GONE);
            editText_comment.setVisibility(View.GONE);
            button_input.setVisibility(View.GONE);
        } else {
            //textView_reply.setVisibility(View.VISIBLE);
            button_cancel.setVisibility(View.VISIBLE);
            editText_comment.setVisibility(View.VISIBLE);
            button_input.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteButtonClick(boolean delete) {
        if (delete) {
            comment_count--;
            textView_comment2.setText(String.valueOf(comment_count));
        }
    }

    //????????? ?????? ??????
    @Override
    protected void onPause() {
        super.onPause();
        if (initial_likecount != like_count) {
            //?????? ????????? ?????? ???????????? ???, ????????? ????????? ??????????????????. ??? ??????????????? ????????????(-1) ????????? ????????????.(????????? ??????)
            //??? ??????????????? ?????????(+1) ???????????? ????????? ???????????? DB??? ????????????.
            RequestParams params = new RequestParams();
            params.put("id", memberID);
            params.put("board_id", foretBoardDTO.getId());
            params.put("type", 0);
            if (initial_likecount > like_count) { //????????? ?????? 1?????????->????????? ??????
                client.post(getIPAdress.getInstance().getIp()+"/foret/member/member_board_dislike.do", params, likeChangeResponse);
            } else { //????????? ?????? if?????? ?????? ?????????????????? ?????? ????????? ???????????? else??? ?????? ????????? ?????? ????????? ????????? ??????
                client.post(getIPAdress.getInstance().getIp()+"/foret/member/member_board_like.do", params, likeChangeResponse);
            }
        }
    }

    private void getDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("????????? ??????");
        builder.setMessage("?????? ?????????????????????????");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setCancelable(false);

        // ??????
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                url = getIPAdress.getInstance().getIp()+"/foret/board/board_delete.do";
                RequestParams params = new RequestParams();
                params.put("id", foretBoardDTO.getId());
                client.post(url, params, deleteBoardResponse);
            }
        });
        // ??????
        builder.setNeutralButton("??????", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getMember(int board_id) {
        url = getIPAdress.getInstance().getIp()+":8085/foret/search/member.do";
        client = new AsyncHttpClient();
        memberResponse = new MemberResponse();
        RequestParams params = new RequestParams();
        params.put("id", board_id);
        client.post(url, params, memberResponse);
    }

    class MemberResponse extends AsyncHttpResponseHandler {
        @Override
        public void onStart() {
            super.onStart();
            //Log.d("[TEST]", "MemeberResponse onStart() ??????");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            //Log.d("[TEST]", "MemeberResponse onStart() ??????");

        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            Gson gson = new Gson();
            try {
                JSONObject json = new JSONObject(str);
                String RT = json.getString("RT");
                if (RT.equals("OK")) {
                    JSONArray member = json.getJSONArray("member");
                    JSONObject temp = member.getJSONObject(0);
                    memberDTO = gson.fromJson(temp.toString(), MemberDTO.class);
                    /*
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getId());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getNickname());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getName());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getBirth());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getPhoto());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getEmail());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getPassword());
                    Log.d("[member]", "????????? ????????? ==== " + memberDTO.getReg_date());
                    Log.d("[TEST]", "???????????? ?????????");

                     */

                    getBoard(board_id);
                } else {
                    //Log.d("[TEST]", "???????????? ????????????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ReadForetBoardActivity.this, "MemeberResponse ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

    class ViewFreeBoardResponse extends AsyncHttpResponseHandler { //??? ???????????????

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            Gson gson = new Gson();
            try {
                JSONObject json = new JSONObject(str);
                if (json.getString("RT").equals("OK")) {
                    JSONArray board = json.getJSONArray("board");
                    JSONObject object = board.optJSONObject(0);
                    foretBoardDTO = gson.fromJson(object.toString(), ReadForetDTO.class);
                    initial_likecount = foretBoardDTO.getBoard_like(); //?????? ????????? ??? ??????
                    like_count = foretBoardDTO.getBoard_like();
                    comment_count = foretBoardDTO.getBoard_comment();

                    Log.e("[TEST1]", "???????????? ??????????????? ==== " + foretBoardDTO.getId());
                   Log.e("[TEST1]", "???????????? ???????????????  ==== " + foretBoardDTO.getWriter());
                    Log.e("[TEST1]", "???????????? ??????  ==== " + foretBoardDTO.getType());
                    Log.e("[TEST1]", "????????? ??????  ==== " + object.isNull("writer_photo"));
                    if (!object.isNull("writer_photo")) {
                        Log.e("[TEST1]", "????????? ??????  ==== " + object.getString("writer_photo"));
                        foretBoardDTO.setWriter_photo(object.getString("writer_photo"));
                        wirterPhotoGet = object.getString("writer_photo");
                        String myPhoto = getIPAdress.getInstance().getIp()+"/foret/storage/" + wirterPhotoGet.toString().replace("[", "").replace("]", "");
                        Glide.with(context).load(myPhoto)
                                .placeholder(R.drawable.icon_defalut).into(image_profile);
                    }
                    setDataBoard(foretBoardDTO);

                    if (object.getJSONArray("comment").length() != 0) {
                        JSONArray comment = object.getJSONArray("comment");
                        for (int a = 0; a < comment.length(); a++) {
                            JSONObject commnetOject = comment.getJSONObject(a);
                            foretBoardComment = new FBCommentDTO();
                            foretBoardComment.setId(commnetOject.getInt("id"));
                            if (!commnetOject.isNull("reg_date")) {
                                foretBoardComment.setReg_date(commnetOject.getString("reg_date"));
                            }
                            foretBoardComment.setGroup_no(commnetOject.getInt("group_no"));
                            foretBoardComment.setWriter(commnetOject.getInt("writer"));


                            if (!commnetOject.isNull("content")) {

                                foretBoardComment.setContent(commnetOject.getString("content"));
                            }

                            if (foretBoardComment.getId() == foretBoardComment.getGroup_no()) {
                                foretBoardComment.setParent(true);
                            } else {
                                foretBoardComment.setParent(false);
                            }
                            commentlist.add(foretBoardComment);
                        }
                        adapter = new CommentBoardAdapter(commentlist, context, memberID);
                        adapter.setCommentClickListener(ReadForetBoardActivity.this);
                        recyclerView.setAdapter(adapter);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ReadForetBoardActivity.this, "?????? ?????? ???????????? ?????????", Toast.LENGTH_SHORT).show();
        }
    }

    class DeleteBoardResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if (json.getString("boardRT").equals("OK")) {
                    Toast.makeText(ReadForetBoardActivity.this, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ReadForetBoardActivity.this, "DeleteBoardResponse ?????? ?????? " + statusCode, Toast.LENGTH_SHORT).show();
        }
    }


    class InsertCommentResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if (json.getString("commentRT").equals("OK")) {
                    Toast.makeText(ReadForetBoardActivity.this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                    Log.e("[TEST1]", commentlist.size() + "");
                    commentlist.add(foretBoardComment);
                    Log.e("[TEST2]", commentlist.size() + "");
                    editText_comment.setText("");
                    inputMethodManager.hideSoftInputFromWindow(editText_comment.getWindowToken(), 0);
                    adapter = new CommentBoardAdapter(commentlist, context, memberID);
                    Log.e("[TEST3]", commentlist.size() + "");
                    recyclerView.setAdapter(adapter);
                    recyclerView.scrollToPosition(commentlist.size());

                    //?????????????????? ??????
                    Log.e("[test]", "takeMessage : " + takeMessage);
                    Log.e("[test]", "takeReceiver : " + takeReceiver);


                    myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    // ?????????????????? ??????
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                //Log.e("[test]","ds ref?: "+ds.getRef());
                                //Log.e("[test]","ds id??: "+ds.child("id").getValue());
                                //Log.e("[test]","ds ???????: "+ds.child("id").getValue().equals(takeReceiver));
                                if (ds.child("id").getValue().toString().equals(takeReceiver)) {
                                    // ????????????
                                    hisUid = ds.child("uid").getValue() + "";

                                    String message = takeMessage;
                                    Log.e("[test]", "myUid" + myUid);
                                    Log.e("[test]", "hisUid" + hisUid);

                                    updateNewItem("REPLIED_NEW_ITEM", myUid, hisUid, message, "" + System.currentTimeMillis());

                                    String msg = message;
                                    // ??????
                                    DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                                    database.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ModelUser user = snapshot.getValue(ModelUser.class);

                                            if (notify) {
                                                sendNotification(hisUid, user.getNickname(), message);
                                            }
                                            notify = false;
                                            //?????? ?????????
                                            takeReceiver = originalReciver;
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ReadForetBoardActivity.this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

    class LikeChangeResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if (json.getString("memberRT").equals("OK")) {
                    Toast.makeText(ReadForetBoardActivity.this, "????????? ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ReadForetBoardActivity.this, "????????? ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }


    // ???????????????
    public void updateNewItem(String type, String sender, String receiver, String content, String time) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notify");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", type);
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("content", content);
        hashMap.put("time", time);
        hashMap.put("isSeen", false);

        ref.child(receiver).push().setValue(hashMap);
    }

    // ?????? ?????? ??????.
    private void sendNotification(String hisUid, String nickname, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid); // ?????? ??????
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // ?????? ????????? ????????? ??????
                    Token token = ds.getValue(Token.class);

                    // ????????? ??????
                    Data data = new Data(myUid, nickname + " : " + message, "???????????? ????????? ????????????", hisUid,
                            R.drawable.foret_logo);

                    // ????????? ?????? ??????
                    Sender sender = new Sender(data, token.getToken());
                    // ??????
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(
                                        Call<Response> call,
                                        retrofit2.Response<com.example.foret_app_prototype.activity.notify.Response> response) {
                                    // Toast.makeText(ChatActivity.this,""+response.message(),Toast.LENGTH_LONG).show();
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
}
