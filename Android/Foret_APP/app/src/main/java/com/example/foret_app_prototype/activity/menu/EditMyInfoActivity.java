package com.example.foret_app_prototype.activity.menu;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.foret_app_prototype.R;
import com.example.foret_app_prototype.activity.login.GuideActivity;
import com.example.foret_app_prototype.helper.FileUtils;
import com.example.foret_app_prototype.helper.PhotoHelper;
import com.example.foret_app_prototype.helper.ProgressDialogHelper;
import com.example.foret_app_prototype.helper.getIPAdress;
import com.example.foret_app_prototype.model.MemberDTO;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class EditMyInfoActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    MemberDTO memberDTO;
    TextView textView1, textView2, button1, button2, textView_confirm;
    ImageView profile;
    EditText editText1, editText2, editText3;
    String filePath = null;
    Intent intent;

    // ????????? ??????
    List<String> selected_tag;
    List<String> selected_si;
    List<String> selected_gu;
    String last_selected_si = "";
    String last_selected_gu = "";
    String str = "";
    String show = "";
    boolean ischecked = false;
    boolean ischecked2 = false;
    List<String> str_check;

    List<String> region_si;
    List<String> region_gu;
    List<String> tag_name;
    List<String> member_tag;
    List<String> tag_list;

    AsyncHttpClient client;


    MyInfoEditResponse response;
    TagListResponse tagListResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        region_gu = new ArrayList<>();
        region_si = new ArrayList<>();
        member_tag = new ArrayList<>();
        setContentView(R.layout.activity_edit_my_info);

        // ????????? ?????? ??????
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.foret4));

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        response = new MyInfoEditResponse();
        memberDTO = (MemberDTO) getIntent().getSerializableExtra("memberDTO");

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        editText3 = findViewById(R.id.editText3);
        textView_confirm = findViewById(R.id.textView_confirm);
        profile = findViewById(R.id.profile);
        client = new AsyncHttpClient();

        tagListResponse = new TagListResponse();
        tag_list = new ArrayList<>();
        region_si = new ArrayList<>();
        region_gu = new ArrayList<>();
        member_tag = new ArrayList<>();
        tag_name = new ArrayList<>();

        //??? ??????, ?????? ???????????? DB??? ????????? ?????? ??????
        //client.post("http://34.72.240.24:8085/foret/region/region_list.do", regionListResponse);
        client.post(getIPAdress.getInstance().getIp()+"/foret/tag/tag_list.do", tagListResponse);

        dataSetting();

        profile.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        editText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (editText2.getText().toString().trim().equals(editText3.getText().toString().trim())) {
                    textView_confirm.setTextColor(Color.BLUE);
                    textView_confirm.setText("??????????????? ???????????????.");
                }else{
                    textView_confirm.setVisibility(View.VISIBLE);
                    textView_confirm.setTextColor(Color.RED);
                    textView_confirm.setText("??????????????? ??????????????????.");
                }
            }
        });

    }

    private void dataSetting() {

        textView1.setText(memberDTO.getEmail());
        textView2.setText(memberDTO.getId() + "");
        editText1.setText(memberDTO.getNickname());
        button1.setText(getIntent().getStringExtra("region"));
        button2.setText(getIntent().getStringExtra("tag"));
        Glide.with(this).load(memberDTO.getPhoto()).
                fallback(R.drawable.icon2).error(R.drawable.icon2).placeholder(R.drawable.icon2)
                .into(profile);
    }

    @Override //?????? ??????
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.modify_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override //?????? ????????? ??????
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.modify:
                if (editText2.getText().toString().trim().equals("") || editText3.getText().toString().trim().equals("")) {
                    Toast.makeText(this, "??????????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (!editText2.getText().toString().trim().equals(editText3.getText().toString().trim())) {
                    return false;
                }
                memberDTO.setPassword(editText2.getText().toString().trim());
                requestModify();
                break;
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile:
                showSelect();
                break;
            case R.id.button1:
                regionDialog();
                break;
            case R.id.button2:
                tagDialog();
                break;
        }
    }

    public void regionDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View region_view = getLayoutInflater().inflate(R.layout.guide_select_region, null);
        builder.setTitle("????????? ??????????????????.");

        str = "";
        show = "";
        ischecked = false;
        ischecked2 = false;
        selected_si = new ArrayList<>();
        selected_gu = new ArrayList<>();
        str_check = new ArrayList<>();

        Spinner spinner_si = region_view.findViewById(R.id.spinner_si);
        Spinner spinner_gu = region_view.findViewById(R.id.spinner_gu);
        TextView selected_view = region_view.findViewById(R.id.selected_view);

        spinner_si.setVisibility(View.VISIBLE);
        spinner_si.setSelection(0);

        spinner_si.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("[TEST]", "region_si position => " + position);
                String select_si = (String) parent.getSelectedItem();
                if (position != 0 && !select_si.equals("")) {
                    last_selected_si = select_si;
                    Log.d("[TEST]", "select_si => " + select_si);
                    spinner_gu.setVisibility(View.VISIBLE);
                    ischecked = false;
                } else {
                    spinner_gu.setVisibility(View.INVISIBLE);
                }
                Log.d("[TEST]", "ischecked => " + ischecked);
                Log.d("[TEST]", "ischecked2 => " + ischecked2);

                ArrayAdapter guAdapter;
                switch (position) {
                    case 1:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.seuol,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 2:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.incheon,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 3:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sejong,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 4:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.daejeon,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 5:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.gwangju,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 6:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.daegu,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 7:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.ulsan,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 8:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.busan,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 9:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.gyeonggi,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 10:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.gangwon,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 11:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.chungbuk,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 12:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.chungnam,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 13:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.jeonbuk,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 14:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.jeonnam,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 15:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.gyeongbuk,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 16:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.gyeongnam,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                    case 17:
                        guAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.jeju,
                                R.layout.support_simple_spinner_dropdown_item);
                        spinner_gu.setAdapter(guAdapter);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_gu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("[TEST]", "region_gu onItemSelected ??????");
                Log.d("[TEST]", "region_gu position => " + position);
                String select_gu = (String) parent.getSelectedItem();
                Log.d("[TEST]", "select_gu => " + select_gu);
                for(int i=0; i<str_check.size(); i++) {
                    if (str_check.get(i).equals(select_gu)) {
                        Toast.makeText(EditMyInfoActivity.this, "?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                        spinner_gu.setSelection(0);
                        return;
                    }
                }

                if (position > 0 && !select_gu.equals("")) {
                    Log.d("[TEST]", "select_gu => " + select_gu);
                    selected_si.add(last_selected_si);
                    selected_gu.add(select_gu);
                    str += last_selected_si + " " + select_gu + "\n";

                    selected_view.setText(str);
                    spinner_si.setSelection(0);
                    spinner_gu.setSelection(0);
                    ischecked = true;
                    ischecked2 = true;
                    str_check.add(select_gu);
                }


                Log.d("[TEST]", "ischecked => " + ischecked);
                Log.d("[TEST]", "ischecked2 => " + ischecked2);
                Log.d("[TEST]", "str_check.size() => " + str_check.size());
                for(int a=0; a<str_check.size(); a++) {
                    Log.d("[TEST]", "str_check.size() => " + str_check.get(a));
                }
            }



            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ?????? ?????? ?????????
                if(ischecked2) {
                    region_si = selected_si;
                    region_gu = selected_gu;
                    Log.d("[TEST]", "region_si.size() => " + region_si.size());
                    Log.d("[TEST]", "region_gu.size() => " + region_gu.size());

                    for (int a=0; a<region_si.size(); a++) {
                        show += region_si.get(a) + " " + region_gu.get(a) + "\n";
                        Log.d("[TEST]", "region_si.get(a) => " + region_si.get(a));
                        Log.d("[TEST]", "region_gu.get(a) => " + region_gu.get(a));
                    }
                    button1.setText(show);
                    button1.setVisibility(View.VISIBLE);
                } else if(region_si.size() == 0) {
                    Toast.makeText(EditMyInfoActivity.this, "?????? 1?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }
                last_selected_gu = "";

            }
        });
        builder.setNegativeButton("??????", null);

        builder.setView(region_view);
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void tagDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View region_view = getLayoutInflater().inflate(R.layout.guide_select_region, null);
        builder.setMessage("????????? ??????????????????.");

        str = "";
        show = "";
        ischecked = false;
        selected_tag = new ArrayList<>();
        str_check = new ArrayList<>();

        Spinner spinner_tag = region_view.findViewById(R.id.spinner_tag);
        TextView selected_view = region_view.findViewById(R.id.selected_view);

        ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, tag_list);
        spinner_tag.setVisibility(View.VISIBLE);
        spinner_tag.setAdapter(adapter);

        spinner_tag.setSelection(0);

        spinner_tag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("[TEST]", "position => " + position);
                String select_tag = (String) parent.getSelectedItem();
                for(int i=0; i<str_check.size(); i++) {
                    if (str_check.get(i).equals(select_tag)) {
                        Toast.makeText(EditMyInfoActivity.this, "?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                        spinner_tag.setSelection(0);
                        return;
                    }
                }
                if (position != 0) {
                    selected_tag.add(select_tag);
                    str += "#" + select_tag + " ";
                    selected_view.setText(str);
                    spinner_tag.setSelection(0);
                    str_check.add(select_tag);
                    ischecked = true;
                }
                Log.d("[TEST]", "ischecked => " + ischecked);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ?????? ?????? ?????????
                if(ischecked) {
                    member_tag = selected_tag;
                    Log.d("[TEST]", "member_tag.size() => " + member_tag.size());

                    for (int a=0; a<member_tag.size(); a++) {
                        show += "#" + member_tag.get(a) + " ";
                        Log.d("[TEST]", "foret_tag.get(a) => " + member_tag.get(a));
                    }
                    button2.setText(show);
                    button2.setVisibility(View.VISIBLE);
                    ischecked = false;
                } else if(member_tag.size() == 0) {
                    Toast.makeText(EditMyInfoActivity.this, "?????? 1?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("??????",null);

        builder.setView(region_view);
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showSelect() {
        final String[] menu = {"?????? ????????????", "??????????????? ????????????"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: //?????? ????????????-????????? ??????
                        filePath = PhotoHelper.getInstance().getNewPhotoPath(); //????????? ?????? ??????
                        Log.d("[TEST]", "photoPath = " + filePath);

                        File file = new File(filePath);
                        Uri uri = null;

                        //???????????? ????????? ?????? ????????? ????????? (action??? uri??? ????????????)
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri = FileProvider.getUriForFile(EditMyInfoActivity.this, getApplicationContext().getPackageName() + ".fileprovider", file);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            uri = Uri.fromFile(file);
                        }
                        Log.d("[TEST]", "uri : " + uri.toString());

                        //????????? ????????? ??????????????? ??????
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        intent.putExtra(AUDIO_SERVICE, false);

                        //????????? ??? ??????
                        startActivityForResult(intent, 200);
                        break;
                    case 1: //??????????????? ????????????-????????? ??????
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*"); //?????? ????????? ??????
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(intent, 300); //????????? ????????? ??????????????????
                        break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 200:
                    Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                    //?????? ???????????? MediaStore??? ????????????(???????????? ??????). MediaStore??? ???????????? ????????? ?????? ????????? ?????? ????????? ?????? ???????????? ????????? ??? ??????.
                    intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(filePath));
                    Log.d("[TEST]", filePath);
                    sendBroadcast(intent);
                    Glide.with(this).load(filePath).into(profile);
                    break;
                case 300:
                    String uri = data.getData().toString();
                    String fileName = uri.substring(uri.lastIndexOf("/") + 1);
                    Log.d("[TEST]", "fileName = " + fileName);
                    filePath = FileUtils.getPath(this, data.getData());
                    Log.d("[TEST]", "filePath = " + filePath);
                    Toast.makeText(this, fileName + "??? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    Glide.with(this).load(filePath).into(profile);
            }
        }
    }

    private void requestModify() {
        RequestParams params = new RequestParams();

        //?????? ?????? ????????? ???????????????
        String[] str_si = new String[region_si.size()];
        String[] str_gu = new String[region_gu.size()];

        for (int a = 0; a < str_si.length; a++) {
            str_si[a] = region_si.get(a);
            str_gu[a] = region_gu.get(a);
            if (a == 0) {
                params.put("region_si", str_si[a]);
                params.put("region_gu", str_gu[a]);
            //    Log.e("[test]", "???????" + str_si[a] + "," + str_gu[a]);
            } else {
                params.add("region_si", str_si[a]);
                params.add("region_gu", str_gu[a]);
           //     Log.e("[test]", "???????" + str_si[a] + "," + str_gu[a]);
            }

        }
        String[] str_tag = new String[member_tag.size()];
        for (int a = 0; a < str_tag.length; a++) {
            str_tag[a] = member_tag.get(a);
            if (a == 0) {
                params.put("tag", str_tag[a]);
            //    Log.e("[test]","????????"+str_tag[a]);
            } else {
                params.add("tag", str_tag[a]);
             //   Log.e("[test]","????????"+str_tag[a]);
            }
        }

        String tag = "";
        String region = (memberDTO.getRegion_si().toString() + "," + memberDTO.getRegion_gu().toString()).replace("[", "").replace("]", "");
        for (int a = 0; a < memberDTO.getTag().size(); a++) {
            tag += "#" + memberDTO.getTag().get(a) + " ";
        }



        memberDTO.setPassword(editText2.getText().toString().trim());
        memberDTO.setNickname(editText1.getText().toString().trim());


      //  Log.e("[test]","name??"+memberDTO.getName());
     //   Log.e("[test]","birth??"+memberDTO.getBirth());
     //   Log.e("[test]","nickname??"+memberDTO.getNickname());
    //    Log.e("[test]","password??"+ memberDTO.getPassword());
    //    Log.e("[test]","id??"+memberDTO.getId());

        params.put("name", memberDTO.getName());
        params.put("birth", memberDTO.getBirth());
        params.put("nickname", memberDTO.getNickname());
        params.put("password", memberDTO.getPassword());
        params.put("id", memberDTO.getId());

        if (filePath != null) {
            Log.d("tag","-----[][]filePath :"+filePath.toString()+"[][]---------------");
            try {
                params.put("photo", new File(filePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        params.setForceMultipartEntityContentType(true);

        final int DEFAULT_TIME = 50 * 1000;
        client.setConnectTimeout(DEFAULT_TIME);
        client.setResponseTimeout(DEFAULT_TIME);
        client.setTimeout(DEFAULT_TIME);
        client.setResponseTimeout(DEFAULT_TIME);

        ProgressDialogHelper.getInstance().getProgressbar(this, "?????? ?????? ?????????.");
        client.post(getIPAdress.getInstance().getIp()+"/foret/member/member_modify.do", params, response);
    }

    class MyInfoEditResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str_json = new String(responseBody);
            try {
                ProgressDialogHelper.getInstance().removeProgressbar();
                JSONObject json = new JSONObject(str_json);
                String line = "";

                if (json.getString("rt").equals("OK")) {
                    if(json.getString("memberRT").equals("OK")) line +="[????????????]";
                    if(json.getString("memberRegionRT").equals("OK")) line+="[????????????]";
                    if(json.getString("memberTagRT").equals("OK")) line+="[????????????]";
                    if(json.getString("memberPhotoRT").equals("OK")) line+="[?????? ??????]";

                    Toast.makeText(EditMyInfoActivity.this, line+"??? ????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("memberDTO", memberDTO);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(EditMyInfoActivity.this, "??? ?????? ?????? ??????", Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            ProgressDialogHelper.getInstance().removeProgressbar();
            Toast.makeText(EditMyInfoActivity.this, "???????????? 500?????? ???", Toast.LENGTH_SHORT).show();
        }
    }

    class RegionListResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if (json.getInt("total") != 0) {
                    JSONArray region = json.getJSONArray("region");
                    for (int a = 0; a < region.length(); a++) {
                        JSONObject object = region.getJSONObject(a);
                        region_si.add(object.getString("region_si"));
                        region_gu.add(object.getString("region_gu"));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(EditMyInfoActivity.this, "???????????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

    //???????????? ?????? ????????????
    class TagListResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if (json.getInt("total") != 0) {
                    JSONArray tag = json.getJSONArray("tag");
                    for (int a = 0; a < tag.length(); a++) {
                        JSONObject object = tag.getJSONObject(a);
                        tag_list.add(object.getString("tag_name"));
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(EditMyInfoActivity.this, "???????????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

}