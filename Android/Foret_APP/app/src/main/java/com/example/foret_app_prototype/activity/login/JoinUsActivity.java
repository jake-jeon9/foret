package com.example.foret_app_prototype.activity.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.foret_app_prototype.R;
import com.example.foret_app_prototype.helper.CalendarHelper;
import com.example.foret_app_prototype.helper.ProgressDialogHelper;
import com.example.foret_app_prototype.helper.getIPAdress;
import com.example.foret_app_prototype.model.Member;
import com.example.foret_app_prototype.model.MemberDTO;
import com.example.foret_app_prototype.model.ModelUser;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class JoinUsActivity extends AppCompatActivity implements View.OnClickListener {
    Member member;

    Toolbar toolbar;

    LinearLayout layout1, layout2, layout3, layout4, layout5, layout6;
    EditText editText1, editText2, editText3, editText4, editText5, editText6;
    TextView textView1, textView2, textView3, textView4, textView5;
    ImageView show_pw, logo, check1, check2, check3, check4, check5, check6;
    Intent intent = null;

    int birth_length = 8; // ???????????? ????????? ??????
    int pw_length = 6; // ???????????? ???????????????
    int pw_length2 = 12; // ???????????? ???????????????

    String name;
    String nickname;
    String birth;
    String email;
    String pw;
    String check_email;

    String nameValidation = "^[A-z|???-???]([A-z|???-???]*)$";
    String nickValidation = "^[A-z|???-???|0-9]([A-z|???-???|0-9]*)$";
    String emailValidation = "^[A-z|0-9]([A-z|0-9]*)(@)([A-z]*)(\\.)([a-zA-Z]){2,3}$";
    //    String pwValidation = "^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{6,12}$";
    String pwValidation = "^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#$%^&*])(?=.*[0-9!@#$%^&*])[A-Za-z[0-9]$@$!%*#?&].{5,12}$";

    int name_eq = 0;     // ?????? ??????
    int nick_eq = 0;     // ????????? ??????
    int birty_eq = 0;    // ?????? ??????
    int email_eq = 0;    // ?????? ??????
    int pw_eq = 0;       // ?????? ??????
    int pw2_eq = 0;      // ???????????? ??????
    int check_count = 0; // ?????? ?????????

    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_us);

        // ????????? ?????? ??????
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.foret4));

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // ?????? ????????? ??????
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // ???????????? ??????

        getFind(); // ?????? ?????????

        editText1.requestFocus(); // ?????? ?????????
        checkName();

//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void checkName() {
        editText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                name = editText1.getText().toString().trim();
                if (name.matches(nameValidation) && s.length() > 0) {
                    textView1.setText("????????? ??????????????????.");
                    textView1.setTextColor(Color.parseColor("#FF0000FF"));
                    check1.setVisibility(View.VISIBLE);
                } else {
                    textView1.setText("????????? ???????????????.");
                    textView1.setTextColor(Color.parseColor("#FF0000"));
                    check1.setVisibility(View.INVISIBLE);
                }
                if (check1.getVisibility() == View.VISIBLE) {
                    name_eq = 1;
                    Log.d("[TEST]", "[NAME]name_eq => " + name_eq);
                } else {
                    name_eq = 0;
                    Log.d("[TEST]", "[NAME]name_eq => " + name_eq);
                }
                Log.d("[TEST]", "name => " + name);
                Log.d("[TEST]", "name.matches(emailValidation) => " + name.matches(nameValidation));
                Log.d("[TEST]", "s.length() => " + s.length());
            }
        });
    }

    private void editName() {
        name = editText1.getText().toString().trim();
        if (name.equals("")) {
            textView1.setText("????????? ??????????????????.");
            textView1.setTextColor(Color.parseColor("#FF0000"));
            return;
        }
        check_count++;
        Log.d("[TEST]", "[name]check_count => " + check_count);
        animation = new AlphaAnimation(0, 1);
        animation.setDuration(1000);
        layout2.startAnimation(animation);
        layout2.setVisibility(View.VISIBLE);
        editText2.requestFocus();
        checkNick();
    }

    private void checkNick() {
        editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                nickname = editText2.getText().toString().trim();
                if (nickname.matches(nickValidation) && s.length() > 0) {
                    textView2.setText("?????? ????????? ??????????????????.");
                    textView2.setTextColor(Color.parseColor("#FF0000FF"));
                    check2.setVisibility(View.VISIBLE);
                } else {
                    textView2.setText("????????? ??? ?????? ??????????????????.");
                    textView2.setTextColor(Color.parseColor("#FF0000"));
                    check2.setVisibility(View.INVISIBLE);
                }
                if (check2.getVisibility() == View.VISIBLE) {
                    nick_eq = 1;
                    Log.d("[TEST]", "[NICK]nick_eq => " + nick_eq);
                } else {
                    nick_eq = 0;
                    Log.d("[TEST]", "[NICK]nick_eq => " + nick_eq);
                }
                Log.d("[TEST]", "nickname => " + nickname);
                Log.d("[TEST]", "nickname.matches(emailValidation) => " + nickname.matches(nickValidation));
                Log.d("[TEST]", "s.length() => " + s.length());
            }
        });
    }

    private void editNick() {
        nickname = editText2.getText().toString().trim();
        if (nickname.equals("")) {
            textView2.setText("???????????? ??????????????????.");
            textView2.setTextColor(Color.parseColor("#FF0000"));
            return;
        }
        check_count++;
        Log.d("[TEST]", "[nick]check_count => " + check_count);
        animation = new AlphaAnimation(0, 1);
        animation.setDuration(1000);
        layout3.startAnimation(animation);
        layout3.setVisibility(View.VISIBLE);
        editText3.requestFocus();
        checkBirth();
    }

    private void checkBirth() {
        editText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                birth = editText3.getText().toString().trim();
                if (birth_length == s.length()) {
                    textView3.setText("??????????????? ??????????????????.");
                    textView3.setTextColor(Color.parseColor("#FF0000FF"));
                    check3.setVisibility(View.VISIBLE);
                } else {
                    textView3.setText("???????????? 8????????? ??????????????????.");
                    textView3.setTextColor(Color.parseColor("#FF0000"));
                    check3.setVisibility(View.INVISIBLE);
                }
                if (check3.getVisibility() == View.VISIBLE) {
                    birty_eq = 1;
                    Log.d("[TEST]", "[BIRTH]birty_eq => " + birty_eq);
                } else {
                    birty_eq = 0;
                    Log.d("[TEST]", "[BIRTH]birty_eq => " + birty_eq);
                }
                Log.d("[TEST]", "birth => " + birth);
                Log.d("[TEST]", "s.length() => " + s.length());
            }
        });
    }

    private void editBirth() {
        birth = editText3.getText().toString().trim();
        if (birth.equals("")) {
            textView3.setText("??????????????? ??????????????????.");
            textView3.setTextColor(Color.parseColor("#FF0000"));
            return;
        } else if (birth.length() < birth_length) {
            textView3.setText("??????????????? 8????????? ??????????????????.");
            textView3.setTextColor(Color.parseColor("#FF0000"));
            return;
        }
        check_count++;
        Log.d("[TEST]", "[birth]check_count => " + check_count);
        animation = new AlphaAnimation(0, 1);
        animation.setDuration(1000);
        layout4.startAnimation(animation);
        layout4.setVisibility(View.VISIBLE);
        editText4.requestFocus();
        checkEmail();
    }


    private void checkEmail() {
        editText4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                email = editText4.getText().toString().trim();
                if (email.matches(emailValidation) && s.length() > 0) {
                    textView4.setText("??????????????? ???????????????.");
                    textView4.setTextColor(Color.parseColor("#FF0000"));
                    check4.setImageResource(R.drawable.check2);
                    check4.setVisibility(View.VISIBLE);
                } else {
                    textView4.setText("????????? ???????????? ??????????????????.");
                    textView4.setTextColor(Color.parseColor("#FF0000"));
                    check4.setVisibility(View.INVISIBLE);
                }

                Log.d("[TEST]", "email => " + email);
                Log.d("[TEST]", "email.matches(emailValidation) => " + email.matches(emailValidation));
                Log.d("[TEST]", "s.length() => " + s.length());
            }
        });
    }

    private void checkedEmail() {
        email = editText4.getText().toString().trim();
        String url = getIPAdress.getInstance().getIp()+"/foret/search/check_email.do";
        AsyncHttpClient client = new AsyncHttpClient();
        EmailResponse emailResponse = new EmailResponse();
        RequestParams params = new RequestParams();
        params.put("email", email);
        client.post(url, params, emailResponse);
    }

    private void editEmail() {
        if(email_eq == 1) {
            email = editText4.getText().toString().trim();
            if (email.equals("")) {
                textView4.setText("???????????? ??????????????????.");
                textView4.setTextColor(Color.parseColor("#FF0000"));
                return;
            } else if (!email.matches(emailValidation)) {
                textView4.setText("????????? ???????????? ??????????????????.");
                textView4.setTextColor(Color.parseColor("#FF0000"));
                return;
            }
            check_count++;
            Log.d("[TEST]", "[email]check_count => " + check_count);
            animation = new AlphaAnimation(0, 1);
            animation.setDuration(1000);
            layout5.startAnimation(animation);
            layout5.setVisibility(View.VISIBLE);
            editText5.requestFocus();
            checkPw();
        } else {
            Toast.makeText(this, "????????? ??????????????? ????????????.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPw() {
        editText5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                pw = editText5.getText().toString().trim();
                if (pw.matches(pwValidation) && s.length() > 5) {
                    textView5.setText("?????? ????????? ?????????????????????.");
                    textView5.setTextColor(Color.parseColor("#FF0000FF"));
                    check5.setVisibility(View.VISIBLE);
                } else if (pw.length() < pw_length) {
                    textView5.setText("?????? 6????????? ??????????????????.");
                    textView5.setTextColor(Color.parseColor("#FF0000"));
                    check5.setVisibility(View.INVISIBLE);
                } else if (pw.length() > pw_length2) {
                    textView5.setText("?????? 12????????? ??????????????????.");
                    textView5.setTextColor(Color.parseColor("#FF0000"));
                    check5.setVisibility(View.INVISIBLE);
                } else if (!pw.matches(pwValidation)) {
                    textView5.setText("??????,??????,??????????????? 2????????? ???????????????.");
                    textView5.setTextColor(Color.parseColor("#FF0000"));
                    check5.setVisibility(View.INVISIBLE);
                }
                if (check5.getVisibility() == View.VISIBLE) {
                    pw_eq = 1;
                    Log.d("[TEST]", "[PW]pw_eq => " + pw_eq);
                } else {
                    pw_eq = 0;
                    Log.d("[TEST]", "[PW]pw_eq => " + pw_eq);
                }
                Log.d("[TEST]", "pw => " + pw);
                Log.d("[TEST]", "pw.matches(emailValidation) => " + pw.matches(pwValidation));
                Log.d("[TEST]", "s.length() => " + s.length());
            }
        });
    }

    private void editPw() {
        pw = editText5.getText().toString().trim();
        Log.d("[TEST]", "pw.length() => " + pw.length());
        if (pw.equals("")) {
            textView5.setText("??????????????? ??????????????????.");
            textView5.setTextColor(Color.parseColor("#FF0000"));
            return;
        } else if (pw.length() < pw_length) {
            textView5.setText("?????? 6????????? ??????????????????.");
            textView5.setTextColor(Color.parseColor("#FF0000"));
            return;
        } else if (pw.length() > pw_length2) {
            textView5.setText("?????? 12????????? ??????????????????.");
            textView5.setTextColor(Color.parseColor("#FF0000"));
            return;
        } else if (!pw.matches(pwValidation)) {
            textView5.setText("??????,??????,??????????????? 2????????? ???????????????.");
            textView5.setTextColor(Color.parseColor("#FF0000"));
            return;
        }
        check_count++;
        Log.d("[TEST]", "[pw]check_count => " + check_count);
        animation = new AlphaAnimation(0, 1);
        animation.setDuration(1000);
        layout6.startAnimation(animation);
        layout6.setVisibility(View.VISIBLE);
        editText6.requestFocus();
        checkpw2();
    }

    private void checkpw2() {
        editText6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String pw2 = editText6.getText().toString().trim();
                if (pw2.equals(pw)) {
                    textView5.setText("??????????????? ??????????????????.");
                    textView5.setTextColor(Color.parseColor("#FF0000FF"));
                    check6.setVisibility(View.VISIBLE);
                } else {
                    textView5.setText("??????????????? ??????????????????.");
                    textView5.setTextColor(Color.parseColor("#FF0000"));
                    check6.setVisibility(View.INVISIBLE);
                }
                if (check6.getVisibility() == View.VISIBLE) {
                    pw2_eq = 1;
                    Log.d("[TEST]", "[PW]pw_eq => " + pw_eq);
                } else {
                    pw2_eq = 0;
                    Log.d("[TEST]", "[PW]pw_eq => " + pw_eq);
                }
                Log.d("[TEST]", "s.length() => " + s.length());
            }
        });
    }

    private void editPw2() {
        String pw2 = editText6.getText().toString().trim();
        if (!pw2.equals(pw)) {
            textView5.setText("??????????????? ????????????.");
            textView5.setTextColor(Color.parseColor("#FF0000"));
            return;
        }
        check_count++;
        Log.d("[TEST]", "[pw2]check_count => " + check_count);
        inputCheck();
    }

    private boolean checkJoin() {
        if (check_count != 6) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_pw:
                if (show_pw.getTag().equals("0")) { //???????????? ??? ????????? ?????? ??????
                    show_pw.setTag("1");
                    show_pw.setImageResource(R.drawable.pw_show); //???????????? ??????????????? ?????????.
                    //??????????????? ????????? ??????.
                    editText5.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    editText6.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else { //???????????? ????????? ?????? ??????
                    show_pw.setTag("0");
                    show_pw.setImageResource(R.drawable.pw); //???????????? ??????????????? ?????????.
                    //??????????????? ?????????.
                    editText5.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText6.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                }
                //?????? ??? ??????
                editText5.setSelection(editText5.getText().length());
                editText6.setSelection(editText6.getText().length());
                break;
            case R.id.check4:
                checkedEmail();
                break;
        }
    }

    // ?????? ??????
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ?????? ???????????? ????????? ?????? ?????? ??? ????????? ?????????.
        getMenuInflater().inflate(R.menu.login_activity_sign_up_menu, menu);
        return true;
    }

    // ?????? ?????? ?????????
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("[TEST]", "[?????????]check_count => " + check_count);
        switch (item.getItemId()) {
            case R.id.next: // ??????
                switch (check_count) {
                    case 0:
                        editName();
                        break;
                    case 1:
                        editNick();
                        break;
                    case 2:
                        editBirth();
                        break;
                    case 3:
                        editEmail();
                        break;
                    case 4:
                        editPw();
                        break;
                    case 5:
                        editPw2();
                        break;
                    case 6:
                        inputCheck();
                }
                break;
            case android.R.id.home: // ???????????? ??????
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inputCheck() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("???????????? ????????? ?????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkJoin()) {
                    intent = new Intent(getApplicationContext(), GuideActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("nickname", nickname);
                    intent.putExtra("birth", birth);
                    intent.putExtra("email", email);
                    intent.putExtra("pw2", pw);
                    startActivity(intent);
                    finish();
                }
            }
        });
        builder.setNegativeButton("??????", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void getFind() {
        layout1 = findViewById(R.id.layout1);
        layout2 = findViewById(R.id.layout2);
        layout3 = findViewById(R.id.layout3);
        layout4 = findViewById(R.id.layout4);
        layout5 = findViewById(R.id.layout5);
        layout6 = findViewById(R.id.layout6);
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        editText3 = findViewById(R.id.editText3);
        editText4 = findViewById(R.id.editText4);
        editText5 = findViewById(R.id.editText5);
        editText6 = findViewById(R.id.editText6);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);
        logo = findViewById(R.id.logo);
        show_pw = findViewById(R.id.show_pw);
        check1 = findViewById(R.id.check1);
        check2 = findViewById(R.id.check2);
        check3 = findViewById(R.id.check3);
        check4 = findViewById(R.id.check4);
        check5 = findViewById(R.id.check5);
        check6 = findViewById(R.id.check6);

        check4.setOnClickListener(this);
        show_pw.setOnClickListener(this);
    }

    private class EmailResponse extends AsyncHttpResponseHandler {
        @Override
        public void onStart() {
            super.onStart();
            Log.e("[TEST]","EmailResponse onStart() ??????");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            Log.e("[TEST]","EmailResponse onFinish() ??????");
            Log.e("[TEST]","my ip?" + getIPAdress.getInstance().getIp());
            if (check_email.equals("OK")) {
                textView4.setText("?????? ????????? ??????????????????.");
                textView4.setTextColor(Color.parseColor("#FF0000FF"));
                check4.setImageResource(R.drawable.check);
                check4.setVisibility(View.VISIBLE);
                email_eq = 1;
            } else if (check_email.equals("FAIL")) {
                textView4.setText("?????? ???????????? ??????????????????.");
                textView4.setTextColor(Color.parseColor("#FF0000"));
                check4.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                String RT = json.getString("RT");
                if(RT.equals("OK")) {
                    check_email = "OK";
                    Log.e("[TEST]","????????? ????????????");
                } else {
                    check_email = "FAIL";
                    Log.e("[TEST]","????????? ????????????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(JoinUsActivity.this, "EmailResponse ?????? ??????", Toast.LENGTH_SHORT).show();
            Log.e("[TEST]","check_email => " + check_email);
        }
    }
}