package com.example.foret_app_prototype.activity.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.foret_app_prototype.R;
import com.example.foret_app_prototype.activity.MainActivity;
import com.example.foret_app_prototype.activity.notify.Token;
import com.example.foret_app_prototype.helper.ProgressDialogHelper;
import com.example.foret_app_prototype.helper.getIPAdress;
import com.example.foret_app_prototype.model.MemberDTO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static LoginActivity loginActivity;

    private boolean saveLoginData;
    private String email;
    private String pwd;
    String id;
    AsyncHttpClient client;
    HttpResponse response;
    String url = getIPAdress.getInstance().getIp()+"/foret/search/member_login.do";
    Button button0;
    TextView button3, button4;
    EditText emailEditText, passwordEditText;

    FirebaseAuth mAuth;
    Context context;
    FirebaseUser user;
    String deviceToken;
    String myUid;
    String myPw;

    boolean switcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loginActivity = LoginActivity.this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // ????????? ?????? ??????
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.foret4));

        button0 = findViewById(R.id.button0);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        emailEditText = findViewById(R.id.editText1);
        passwordEditText = findViewById(R.id.editText2);
        context = this;
        client = new AsyncHttpClient();
        final int DEFAULT_TIME = 20 * 1000;
        client.setConnectTimeout(DEFAULT_TIME);
        client.setResponseTimeout(DEFAULT_TIME);
        client.setTimeout(DEFAULT_TIME);
        client.setResponseTimeout(DEFAULT_TIME);
        response = new HttpResponse();

        button0.setOnClickListener(this); //?????????
        button3.setOnClickListener(this); //???????????? ??????
        button4.setOnClickListener(this); //????????????

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SessionManager sessionManager = new SessionManager(this);
        int userID = sessionManager.getSession();

        if (userID != -1) {
            moveToMainActivity();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.button0:
                //????????? ????????????
                email = emailEditText.getText().toString().trim();
                pwd = passwordEditText.getText().toString().trim();

                if(email.equals("")){
                    Toast.makeText(this,"???????????? ??????????????????.",Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.e("[test]", email + "/" + pwd);

                RequestParams params = new RequestParams();
                params.put("email", email);
                params.put("password", pwd);
                ProgressDialogHelper.getInstance().getProgressbar(this, "????????? ?????????");
                client.post(url, params, response);
                break;
            case R.id.button3:
                if (emailEditText.getText().toString().trim().equals("") || emailEditText.getText().toString().trim() == null) {
                    Toast.makeText(this, "????????? ???????????? ??????????????????.", Toast.LENGTH_LONG).show();
                    break;
                }

                if(!switcher){
                    Log.e("[test]", "????????? ????????? : " + emailEditText.getText().toString());
                    passwordEditText.setText("");

                    ProgressDialogHelper.getInstance().getProgressbar(context, "????????? ??????????????????.");

                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (!task.isSuccessful()) {
                                        Log.e("[test]", "Fetching FCM registration token failed", task.getException());
                                        return;
                                    }
                                    deviceToken = task.getResult();

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
                                    ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                if (ds.child("token").getValue().equals(deviceToken)) {
                                                    //??? ?????? ??????
                                                    myUid = ds.getKey();
                                                    DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Users");

                                                    ref2.child(myUid).child("user_id").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            String myPw = snapshot.getValue()+"";

                                                            passwordEditText.setText(myPw);
                                                            Toast.makeText(context, "????????? ???????????? ????????????" + myPw + "??? ?????????????????????. ????????? ??? ??????????????? ??????????????????.", Toast.LENGTH_LONG).show();
                                                            ProgressDialogHelper.getInstance().removeProgressbar();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Toast.makeText(context, "????????? ???????????? ????????????. ????????? ??????????????????.", Toast.LENGTH_LONG).show();
                                                            ProgressDialogHelper.getInstance().removeProgressbar();
                                                        }
                                                    });

                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "??????????????? ????????? ??????????????? ??????????????????", Toast.LENGTH_LONG).show();
                                            Log.e("[test]", "error?" + error.getMessage() + "/" + error.getDetails());
                                            ProgressDialogHelper.getInstance().removeProgressbar();
                                        }
                                    });


                                }
                            });

                    switcher = false;
                }

                break;
            case R.id.button4:
                intent = new Intent(this, JoinUsActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void moveToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
        finish();
    }

    class HttpResponse extends AsyncHttpResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                String RT = json.getString("RT");
                Log.e("[test]", RT);
                Log.e("[test]", json.getString("id"));

                if (RT.equals("OK")) {
                    //????????? ?????????
                    joinedMember(email, pwd);
                    id = json.getString("id");

                    Log.e("[test]", "????????????/" + statusCode);
                    ProgressDialogHelper.getInstance().removeProgressbar();

                }
            } catch (JSONException e) {
                e.printStackTrace();
                ProgressDialogHelper.getInstance().removeProgressbar();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(LoginActivity.this, "???????????? ??????????????? ????????? ?????????", Toast.LENGTH_SHORT).show();
            Log.e("[test]", error.getMessage() + "/" + statusCode);
            ProgressDialogHelper.getInstance().removeProgressbar();
        }
    }

    //????????? ????????? ?????????
    public void joinedMember(String member_email, String member_id) {
        Log.d("TAG", "signInWithEmail:??????");
        Log.d("TAG", "member_email:" + member_email);
        Log.d("TAG", "member_id:" + member_id);
        mAuth.signInWithEmailAndPassword(member_email, member_id).
                addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "signInWithEmail:success");
                            user = mAuth.getCurrentUser();
                            moveToMainActivity();
                        } else {
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}