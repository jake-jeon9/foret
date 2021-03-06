package com.example.foret_app_prototype.activity.free;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foret_app_prototype.R;
import com.example.foret_app_prototype.activity.login.SessionManager;
import com.example.foret_app_prototype.activity.notify.APIService;
import com.example.foret_app_prototype.activity.notify.Client;
import com.example.foret_app_prototype.activity.notify.Data;
import com.example.foret_app_prototype.activity.notify.Response;
import com.example.foret_app_prototype.activity.notify.Sender;
import com.example.foret_app_prototype.activity.notify.Token;
import com.example.foret_app_prototype.adapter.free.CommentListFreeBoardAdapter;
import com.example.foret_app_prototype.helper.getIPAdress;
import com.example.foret_app_prototype.model.ForetBoard;
import com.example.foret_app_prototype.model.ForetBoardComment;
import com.example.foret_app_prototype.model.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;

public class ReadFreeActivity extends AppCompatActivity
        implements OnClickListener, CommentListFreeBoardAdapter.CommentClickListener {

    Toolbar toolbar_writer, toolbar_noWriter;
    TextView textView_writer, textView_like, textView_subject, textView_date, textView_seq,
            textView_reply, textView_content, textView_comment;
    ToggleButton likeButton;
    ImageView button_cancel; //?????? ??????
    EditText editText_comment;
    RecyclerView comment_listView;
    Button button_input; //?????? ?????? ??????

    int memberID;
    Intent intent;
    AsyncHttpClient client;
    ViewFreeBoardResponse viewResponse;
    InsertCommentResponse writeCommentResponse;
    ViewFreeBoardResponse readBoardResponse;
    LikeChangeResponse likeChangeResponse;
    DeleteBoardResponse deleteBoardResponse;
    InsertReCommentResponse writeReCommentResponse;

    CommentListFreeBoardAdapter adapter;
    List<ForetBoardComment> commentlist;
    ForetBoard foretBoard;
    ForetBoardComment foretBoardComment;

    int like_count;
    int comment_count;
    InputMethodManager inputMethodManager;
    String target;
    boolean replying = false;
    int group_no;
    int initial_likecount; //?????? ?????? ?????? ?????? ?????? ????????? ?????? ?????? ??????
    int position;            // ?????? ??? ?????? ??????

    // ????????????
    APIService apiService;
    boolean notify = false;
    String myUid;
    String hisUid;
    String takeMessage;
    String takerSender;
    String takeReceiver;
    String originalReciver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_free);

        // ????????? ?????? ??????
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.foret4));

        //?????? ??????
        toolbar_writer = findViewById(R.id.toolbar_writer);
        toolbar_noWriter = findViewById(R.id.toolbar_noWriter);
        setSupportActionBar(toolbar_writer); //?????????????????? ???????????? ????????? ??????
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textView_writer = findViewById(R.id.textView_writer);
        textView_like = findViewById(R.id.textView_like); //????????? ??????
        textView_subject = findViewById(R.id.textView_subject);
        textView_date = findViewById(R.id.textView_date);
        textView_seq = findViewById(R.id.textView_seq);
        textView_reply = findViewById(R.id.textView_reply); //OO?????? ?????? ?????????????????? ?????????
        textView_comment = findViewById(R.id.textView_comment_count);
        textView_content = findViewById(R.id.textView_content);
        likeButton = findViewById(R.id.likeButton); //????????? ??????
        button_cancel = findViewById(R.id.button_cancel); //???????????? ??????
        editText_comment = findViewById(R.id.editText_comment); //????????????
        comment_listView = findViewById(R.id.comment_listView); //????????? ?????? ??????????????????
        button_input = findViewById(R.id.button_input);

        inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        SessionManager sessionManager = new SessionManager(this);
        memberID = sessionManager.getSession();

        commentlist = new ArrayList<>();
        client = new AsyncHttpClient();
        viewResponse = new ViewFreeBoardResponse();
        writeCommentResponse = new InsertCommentResponse();
        readBoardResponse = new ViewFreeBoardResponse();
        likeChangeResponse = new LikeChangeResponse();
        deleteBoardResponse = new DeleteBoardResponse();
        writeReCommentResponse = new InsertReCommentResponse();

        foretBoard = (ForetBoard) getIntent().getSerializableExtra("foretBoard");
        initial_likecount = foretBoard.getLike_count(); //?????? ????????? ??? ??????
        like_count = foretBoard.getLike_count();
        comment_count = foretBoard.getComment_count();

        setDataBoard(foretBoard);
        comment_listView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        textView_reply.setVisibility(View.GONE);
        button_cancel.setVisibility(View.GONE);
        button_cancel.setOnClickListener(this); //?????? ?????? ??????
        button_input.setOnClickListener(this); //???????????? ->?????? ????????? ???
        likeButton.setOnClickListener(this); //???????????????

        // ?????? ?????? ??????
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

    }

    // ????????? ??? ?????? ????????? ????????? ???????????? ????????? ??????????????? ??????
    private void setDataBoard(ForetBoard foretBoard) {
        textView_writer.setText("????????? : "+foretBoard.getWriter());
        textView_subject.setText(foretBoard.getSubject());
        textView_content.setText(foretBoard.getContent());
        textView_like.setText("??????("+foretBoard.getLike_count() + ")");
        textView_seq.setText(foretBoard.getId() + "");
        textView_date.setText(foretBoard.getReg_date());
        textView_comment.setText("??????("+foretBoard.getComment_count() + ")");
        if (foretBoard.isLike()) {
            likeButton.setChecked(true);
        }

        // ????????????
        takeReceiver =foretBoard.getWriter();
        originalReciver = takeReceiver;
    }

    @Override
    protected void onResume() {
        super.onResume();
        commentlist.clear();

        checkUserStatus();// ????????? ?????? ??????

        //??? ????????????
        RequestParams params = new RequestParams();
        params.put("id", foretBoard.getId());
        params.put("type", 0);
        client.post(getIPAdress.getInstance().getIp()+"/foret/search/boardSelect.do", params, viewResponse);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (String.valueOf(memberID).equals(foretBoard.getWriter())) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.edit_toolbar, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                break;
            case R.id.btn_modify:
                intent = new Intent(this, EditFreeActivity.class);
                intent.putExtra("foretBoard", foretBoard);
                startActivity(intent);
                break;
            case R.id.btn_delete :
                showDeleteDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("?????? ???????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RequestParams params = new RequestParams();
                params.put("id", foretBoard.getId());
                client.post(getIPAdress.getInstance().getIp()+"/foret/board/board_delete.do", params, deleteBoardResponse);
            }
        });
        builder.setNegativeButton("??????", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_cancel : //?????? ?????? ??????
                textView_reply.setVisibility(View.GONE);
                button_cancel.setVisibility(View.GONE);
                editText_comment.setText("");
                replying = false;
                break;
            case R.id.button_input : //?????? ??????
                if(editText_comment.getText().toString().trim().equals("")){
                    Toast.makeText(this, "????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                } else {
                    inputComment();
                }
                break;
            case R.id.likeButton : //????????? ??????
                if(likeButton.isChecked()) {
                    like_count++;
                    textView_like.setText("??????("+like_count+")");
                } else {
                    like_count--;
                    textView_like.setText("??????("+like_count + ")");
                }
                break;
        }
    }

    private void inputComment() { // ?????? ??????
        foretBoardComment = new ForetBoardComment();
        foretBoardComment.setWriter(String.valueOf(memberID));
        foretBoardComment.setContent(editText_comment.getText().toString().trim());
        foretBoardComment.setBoard_id(foretBoard.getId());
        RequestParams params = new RequestParams();
        String url = "";
        if(!replying){
            // ???
            params.put("board_id", foretBoard.getId());
            params.put("writer", memberID);
            params.put("content", editText_comment.getText().toString().trim());
            client.post(getIPAdress.getInstance().getIp()+"/foret/comment/comment_insert.do", params, writeCommentResponse);
        } else {
            // ??????
            Log.d("[TEST]", "inputComment: ?????? ??????");
            Log.d("[TEST]", "inputComment: ??????????????? " + group_no);
            params.put("board_id", foretBoard.getId());
            params.put("writer", memberID);
            params.put("comment_id", group_no);
            params.put("content", editText_comment.getText().toString().trim());
            client.post(getIPAdress.getInstance().getIp()+"/foret/comment/recomment_insert.do", params, writeReCommentResponse);
            replying = false;
        }
        // ????????? ????????? ?????? ??????
        notify = true;
        takeMessage = editText_comment.getText().toString().trim();
        takerSender = String.valueOf(memberID);

    }

    // ???????????? ????????? ???
    @Override
    public void onReplyButtonClick(View v, String target, int group_no, int position, boolean reply) {
        if (reply) {
            textView_reply.setText(target + "????????? ?????? ????????? ?????????.");
            editText_comment.setText("@" + target + " ");
            textView_reply.setVisibility(View.VISIBLE);
            button_cancel.setVisibility(View.VISIBLE);
            this.target = target;
            replying = reply;
            this.group_no = group_no;
            this.position = position;
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            // ????????????
            notify = true;
            takeReceiver = target;

        }
    }

    // ???????????? ????????? ???
    @Override
    public void onModifyButtonClick(View v, boolean modify) {
        if (modify) {
            textView_reply.setVisibility(View.GONE);
            button_cancel.setVisibility(View.GONE);
            editText_comment.setVisibility(View.GONE);
            button_input.setVisibility(View.GONE);
        } else {
            // textView_reply.setVisibility(View.VISIBLE);
            button_cancel.setVisibility(View.VISIBLE);
            editText_comment.setVisibility(View.VISIBLE);
            button_input.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteButtonClick(boolean delete) {
        if (delete) {
            comment_count--;
            textView_comment.setText(String.valueOf(comment_count));
        }
    }

    // ????????? ?????? ??????
    @Override
    protected void onPause() {
        super.onPause();
        if (initial_likecount != like_count) {
            // ?????? ????????? ?????? ???????????? ???, ????????? ????????? ??????????????????. ??? ??????????????? ????????????(-1) ????????? ????????????.(????????? ??????)
            // ??? ??????????????? ?????????(+1) ???????????? ????????? ???????????? DB??? ????????????.
            RequestParams params = new RequestParams();
            params.put("id", memberID);
            params.put("board_id", foretBoard.getId());
            params.put("type", 0);

            //updateuserActiveStatusOff(); // ???????????? ?????? ?????????

            if (initial_likecount > like_count) { // ????????? ?????? 1?????????->????????? ??????
                client.post(getIPAdress.getInstance().getIp()+"/foret/member/member_board_dislike.do", params,
                        likeChangeResponse);
            } else { // ????????? ?????? if?????? ?????? ?????????????????? ?????? ????????? ???????????? else??? ?????? ????????? ?????? ????????? ????????? ??????
                client.post(getIPAdress.getInstance().getIp()+"/foret/member/member_board_like.do", params, likeChangeResponse);
            }
        }
    }

    class ViewFreeBoardResponse extends AsyncHttpResponseHandler { // ??? ???????????????

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if (json.getString("RT").equals("OK")) {

                    JSONArray board = json.getJSONArray("board");
                    JSONObject object = board.optJSONObject(0);
                    foretBoard.setSubject(object.getString("subject"));
                    foretBoard.setLike_count(object.getInt("board_like"));
                    foretBoard.setType(0);
                    foretBoard.setContent(object.getString("content"));
                    foretBoard.setReg_date(object.getString("reg_date"));
                    foretBoard.setComment_count(object.getInt("board_comment"));
                    setDataBoard(foretBoard);

                    if (object.getJSONArray("comment").length() != 0) {
                        JSONArray comment = object.getJSONArray("comment");
                        for (int a = 0; a < comment.length(); a++) {
                            JSONObject commnetOject = comment.getJSONObject(a);
                            foretBoardComment = new ForetBoardComment();
                            foretBoardComment.setId(commnetOject.getInt("id"));
                            foretBoardComment.setReg_date(commnetOject.getString("reg_date"));
                            foretBoardComment.setGroup_no(commnetOject.getInt("group_no"));
                            foretBoardComment.setWriter(String.valueOf(commnetOject.getInt("writer")));

                            foretBoardComment.setContent(commnetOject.getString("content"));
                            if (foretBoardComment.getId() == foretBoardComment.getGroup_no()) {
                                foretBoardComment.setParent(true);
                            } else {
                                foretBoardComment.setParent(false);
                            }
                            commentlist.add(foretBoardComment);
                        }
                        adapter = new CommentListFreeBoardAdapter(commentlist, ReadFreeActivity.this, memberID);
                        adapter.setCommentClickListener(ReadFreeActivity.this);
                        comment_listView.setAdapter(adapter);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ReadFreeActivity.this, "?????? ?????? ???????????? ?????????", Toast.LENGTH_SHORT).show();
        }
    }

    class DeleteBoardResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if (json.getString("boardRT").equals("OK")) {
                    Toast.makeText(ReadFreeActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ReadFreeActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

    class InsertCommentResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if(json.getString("commentRT").equals("OK")) {
                    Toast.makeText(ReadFreeActivity.this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                    Log.e("[TEST1]", commentlist.size()+"");
                    int id = json.getInt("id");
                    String reg_date = json.getString("reg_date");
                    foretBoardComment.setId(id);
                    foretBoardComment.setReg_date(reg_date);
                    foretBoardComment.setGroup_no(id);
                    if (foretBoardComment.getId() == foretBoardComment.getGroup_no()) {
                        foretBoardComment.setParent(true);
                    } else {
                        foretBoardComment.setParent(false);
                    }
                    commentlist.add(foretBoardComment);

                    Log.e("[TEST2]", commentlist.size()+"");
                    editText_comment.setText("");
                    inputMethodManager.hideSoftInputFromWindow(editText_comment.getWindowToken(), 0);
                    adapter = new CommentListFreeBoardAdapter(commentlist, ReadFreeActivity.this, memberID);
                    adapter.setCommentClickListener(ReadFreeActivity.this);
                    Log.e("[TEST3]", commentlist.size()+"");
                    comment_listView.setAdapter(adapter);
                    comment_listView.scrollToPosition(commentlist.size());
                }

                Log.e("[test]","takeMessage : "+takeMessage);
                Log.e("[test]","takeReceiver : "+takeReceiver);
                Log.e("[test]","takerSender : "+takerSender);

                myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                // ?????????????????? ??????
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //Log.e("[test]","ds ref?: "+ds.getRef());
                            //Log.e("[test]","ds id??: "+ds.child("id").getValue());
                            Log.e("[test]","ds ???????: "+ds.child("id").getValue());
                            if (ds.child("id").getValue().toString().equals(takeReceiver)) {
                                // ????????????
                                hisUid = ds.child("uid").getValue() + "";

                                String message = takeMessage;
                                Log.e("[test]","myUid"+myUid);
                                Log.e("[test]","hisUid"+hisUid);

                                updateNewItem("ANONYMOUS_BOARD_NEW_ITEM", myUid, hisUid, message, "" + System.currentTimeMillis());

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



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ReadFreeActivity.this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

    class InsertReCommentResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if(json.getString("commentRT").equals("OK")) {
                    Toast.makeText(ReadFreeActivity.this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                    Log.e("[TEST1]", commentlist.size()+"");
                    // ?????? ??? ????????? ??????
                    int id = json.getInt("id");
                    String reg_date = json.getString("reg_date");
                    foretBoardComment.setId(id);
                    foretBoardComment.setReg_date(reg_date);
                    int group_no = commentlist.get(position).getGroup_no();
                    foretBoardComment.setGroup_no(group_no);
                    boolean group = false;
                    int index = 0;
                    for(int i = 0; i <commentlist.size(); i++){
                        if(!group){
                            if(commentlist.get(i).getGroup_no() == group_no) {
                                group = true;
                            }
                        } else {
                            if(commentlist.get(i).getGroup_no() != group_no){
                                index = i;
                                break;
                            }
                        }
                    }
                    if(index == 0){
                        commentlist.add(foretBoardComment);
                    } else {
                        commentlist.add(index,foretBoardComment);
                    }
                    Log.e("[TEST2]", commentlist.size()+"");
                    editText_comment.setText("");
                    inputMethodManager.hideSoftInputFromWindow(editText_comment.getWindowToken(), 0);
                    adapter = new CommentListFreeBoardAdapter(commentlist, ReadFreeActivity.this, memberID);
                    adapter.setCommentClickListener(ReadFreeActivity.this);
                    Log.e("[TEST3]", commentlist.size()+"");
                    comment_listView.setAdapter(adapter);
                    comment_listView.scrollToPosition(commentlist.size());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ReadFreeActivity.this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

    class LikeChangeResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if (json.getString("memberRT").equals("OK")) {
                    Toast.makeText(ReadFreeActivity.this, "????????? ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ReadFreeActivity.this, "????????? ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

    // ????????? ?????? ?????????
    private void updateuserActiveStatusOn() {
        FirebaseUser currentUseruser = FirebaseAuth.getInstance().getCurrentUser();
        final String userUid = currentUseruser.getUid();
        DatabaseReference userAcitive = FirebaseDatabase.getInstance().getReference("Users").child(userUid);
        HashMap<String, Object> onlineStatus = new HashMap<>();
        onlineStatus.put("onlineStatus", "online");
        onlineStatus.put("listlogined_date", "?????? ?????????");
        userAcitive.updateChildren(onlineStatus);
    }

    // ???????????? ?????? ?????????
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

    // ?????? ?????? ??????
    private void checkUserStatus() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            updateuserActiveStatusOn();
        } else {
            // ????????? ????????? ?????? ??????
            Toast.makeText(this, "???????????? ???????????????.", Toast.LENGTH_LONG).show();
            finish();
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
                    Data data = new Data(myUid, nickname + " : " + message, "??????????????? ????????????", hisUid,
                            R.drawable.foret_logo);

                    // ????????? ?????? ??????
                    Sender sender = new Sender(data, token.getToken());
                    // ??????
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<com.example.foret_app_prototype.activity.notify.Response>() {
                                @Override
                                public void onResponse(
                                        Call<com.example.foret_app_prototype.activity.notify.Response> call,
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