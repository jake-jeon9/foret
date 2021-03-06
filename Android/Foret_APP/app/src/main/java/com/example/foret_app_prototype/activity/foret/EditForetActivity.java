package com.example.foret_app_prototype.activity.foret;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.foret_app_prototype.R;
import com.example.foret_app_prototype.activity.login.GuideActivity;
import com.example.foret_app_prototype.activity.menu.EditMyInfoActivity;
import com.example.foret_app_prototype.helper.FileUtils;
import com.example.foret_app_prototype.helper.PhotoHelper;
import com.example.foret_app_prototype.helper.getIPAdress;
import com.example.foret_app_prototype.model.Foret;
import com.example.foret_app_prototype.model.ForetViewDTO;
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
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class EditForetActivity extends AppCompatActivity implements View.OnClickListener {
    MemberDTO member;
    ForetViewDTO foret;
    String leader;

    AsyncHttpClient client;
    EditForetResponse editForetResponse;
    String url = getIPAdress.getInstance().getIp()+"/foret/foret/foret_modify.do";

    ImageView profile, button_close, button_tag_edit, button_region_edit, button_member_edit;
    Button button_complete;
    TextView textView1, textView_tag, textView_region, textView_member, textView_master, textView_birth;
    EditText editText_intro, textView_name;

    Intent intent;
    File file;
    String filePath = null;

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

    int max_member_count = 0;
    List<String> foret_tag;

    TagListResponse tagListResponse;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_foret);

        // ????????? ?????? ??????
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.foret4));

        getFindId();
        foret = (ForetViewDTO) getIntent().getSerializableExtra("foret");
        member = (MemberDTO) getIntent().getSerializableExtra("memberDTO");
        leader = getIntent().getStringExtra("leader");
        tagListResponse = new TagListResponse();
        client = new AsyncHttpClient();
        dataSetting();
        client.post(getIPAdress.getInstance().getIp()+"/foret/tag/tag_list.do", tagListResponse);
    }

    private void getFindId() {
        profile = findViewById(R.id.profile);
        textView_name = findViewById(R.id.textView_name);
        textView_tag = findViewById(R.id.textView_tag);
        textView_region = findViewById(R.id.textView_region);
        textView_member = findViewById(R.id.textView_member);
        textView_master = findViewById(R.id.textView_master);
        textView_birth = findViewById(R.id.textView_birth);
        textView1 = findViewById(R.id.textView1);
        button_close = findViewById(R.id.button_close);
        button_tag_edit = findViewById(R.id.button_tag_edit);
        button_region_edit = findViewById(R.id.button_region_edit);
        button_complete = findViewById(R.id.button_complete);
        button_member_edit = findViewById(R.id.button_member_edit);
        editText_intro = findViewById(R.id.editText_intro);

        region_si = new ArrayList<>();
        region_gu = new ArrayList<>();
        foret_tag = new ArrayList<>();
        member_tag = new ArrayList<>();
        tag_name = new ArrayList<>();
        tag_list = new ArrayList<>();
        selected_tag = new ArrayList<>();
        str_check = new ArrayList<>();

        textView1.setOnClickListener(this);
        button_close.setOnClickListener(this);
        button_member_edit.setOnClickListener(this);
        button_tag_edit.setOnClickListener(this);
        button_region_edit.setOnClickListener(this);
        button_complete.setOnClickListener(this);
    }

    private void dataSetting() {
        Glide.with(this).load(foret.getPhoto()).
                placeholder(R.drawable.sss).
                error(R.drawable.sss).
                into(profile);

        String tag = "";
        for(int i = 0 ; i<foret.getForet_tag().size();i++){
            tag+=("#"+foret.getForet_tag().get(i)+" ");
        }

        //profile.setImageResource(foret.getForetImage());
        textView_name.setText(foret.getName());
        textView_tag.setText(tag);
        textView_region.setText(foret.getForet_region_si()+" "+foret.getForet_region_gu());
        textView_member.setText(foret.getMember().size() + "/" + foret.getMax_member());
        textView_master.setText("?????? ?????? : " + leader);
        textView_birth.setText(foret.getReg_date());
        editText_intro.setText(foret.getIntroduce());
        max_member_count = foret.getMax_member();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textView1 : // ???????????? - ??? ??????????????? ????????? ???????????? ?????? ????????? ????????? ?????????
                showSelect();
                break;
            case R.id.button_close : // x ??????
                finish();
                break;
            case R.id.button_tag_edit : // ?????? ??????
                tagDialog();
                break;
            case R.id.button_region_edit : // ?????? ??????
                regionDialog();
                break;
            case R.id.button_member_edit : // ?????? ?????? ??????
                memberSelectDialog();
                break;
            case R.id.button_complete : // ????????????
                modify();
                break;
        }
    }

    private void modify() {
        String edit_name = textView_name.getText().toString();
        String edit_intro = editText_intro.getText().toString();
        if(edit_name.equals("")) {
            Toast.makeText(this, "?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
            return;
        } else if(edit_intro.equals("")) {
            Toast.makeText(this, "?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
            return;
        }
        editForetResponse = new EditForetResponse();
        RequestParams params = new RequestParams();

        if(region_si.size()>0 && region_gu.size()>0){
            String[] str_si = new String[region_si.size()];
            String[] str_gu = new String[region_gu.size()];
        for (int a = 0; a < str_si.length; a++) {
            str_si[a] = region_si.get(a);
            str_gu[a] = region_gu.get(a);
            if (a == 0) {
                params.put("region_si", str_si[a]);
                params.put("region_gu", str_gu[a]);
                Log.e("[test]", "???????" + str_si[a] + "," + str_gu[a]);
            } else {
                params.add("region_si", str_si[a]);
                params.add("region_gu", str_gu[a]);
                Log.e("[test]", "???????" + str_si[a] + "," + str_gu[a]);
            }

        }

        }
        if(str_check.size()>0 ){
            String[] str_tag = new String[str_check.size()];
            for (int a = 0; a < str_tag.length; a++) {
                str_tag[a] = str_check.get(a);
                if (a == 0) {
                    params.put("tag", str_tag[a]);
                    Log.e("[test]", "????????" + str_tag[a]);
                } else {
                    params.add("tag", str_tag[a]);
                    Log.e("[test]", "????????" + str_tag[a]);
                }
            }
        }




        params.put("leader_id", member.getId());
        params.put("name", edit_name);
        params.put("introduce", edit_intro);
        params.put("max_member", max_member_count);



        try {
            if (file != null) {
                params.put("photo", file);
                Log.d("----------------]",file.getPath());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        params.put("id", foret.getId());
        client.post(url, params, editForetResponse);
    }

    private void showSelect() {
        final String [] menu = {"?????? ????????????", "??????????????? ????????????"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: //?????? ????????????-????????? ??????
                        filePath = PhotoHelper.getInstance().getNewPhotoPath(); //????????? ?????? ??????
                        Log.d("[TEST]", "photoPath = " + filePath);

                        file = new File(filePath);
                        Uri uri = null;

                        //???????????? ????????? ?????? ????????? ????????? (action??? uri??? ????????????)
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri = FileProvider.getUriForFile(EditForetActivity.this, getApplicationContext().getPackageName() + ".fileprovider", file);
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
        if(resultCode==RESULT_OK) {
            switch (requestCode) {
                case 200:
                    Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                    //?????? ???????????? MediaStore??? ????????????(???????????? ??????). MediaStore??? ???????????? ????????? ?????? ????????? ?????? ????????? ?????? ???????????? ????????? ??? ??????.
                    intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(filePath));
                    Log.d("[TEST]", filePath);
                    sendBroadcast(intent);
                    Glide.with(this).load(filePath).into(profile);
                    break;
                case 300 :
                    String uri1 = data.getData().toString();
                    uri = data.getData();
                    String fileName = uri1.substring(uri1.lastIndexOf("/") + 1);
                    Log.d("[TEST]", "fileName = " + fileName);
                    filePath = FileUtils.getPath(this, data.getData());
                    Log.d("[TEST]", "filePath = " + filePath);
                    file = new File(filePath);
                    Toast.makeText(this, fileName + "??? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    Glide.with(this).load(filePath).into(profile);
            }
        }
    }

    private void modifyCheck() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("?????? ???????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.setNegativeButton("??????", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void memberSelectDialog() { //?????? : ????????? ?????? ??????????????? ??????
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View region_view = getLayoutInflater().inflate(R.layout.guide_select_region, null);
        builder.setTitle("?????? ???????????? ??????????????????.");

        max_member_count = 0;

        Spinner spinner_max_member = region_view.findViewById(R.id.spinner_max_member);
        TextView selected_view = region_view.findViewById(R.id.selected_view);

        spinner_max_member.setVisibility(View.VISIBLE);
        spinner_max_member.setSelection((foret.getMax_member()-1));


        spinner_max_member.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("[TEST]", "foret.getMember().length => " + foret.getMember().size());
                if(foret.getMember().size() < (position+1)) {
                    max_member_count = position+1;
                    Log.d("[TEST]", "max_member_count => " + max_member_count);
                } else {
                    spinner_max_member.setSelection((foret.getMax_member()-1));
                    selected_view.setText("?????? ????????? ??????????????? ????????????.\n" +
                            "?????? ????????? ????????? : " + foret.getMember().size());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //?????? ?????? ?????????
                textView_member.setText(foret.getMember().size() + "/" + max_member_count);
            }
        });
        builder.setNegativeButton("??????", null);

        builder.setView(region_view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void regionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                        Toast.makeText(EditForetActivity.this, "?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
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
                    textView_region.setText(show);
                    textView_region.setVisibility(View.VISIBLE);
                } else if(region_si.size() == 0) {
                    Toast.makeText(EditForetActivity.this, "?????? 1?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }
                last_selected_gu = "";

            }
        });
        builder.setNegativeButton("??????", null);

        builder.setView(region_view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void tagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View region_view = getLayoutInflater().inflate(R.layout.guide_select_region, null);
        builder.setMessage("????????? ??????????????????.");

        str = "";
        show = "";
        ischecked = false;


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
                        Toast.makeText(EditForetActivity.this, "?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
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
                    textView_tag.setText(show);
                    textView_tag.setVisibility(View.VISIBLE);
                    ischecked = false;
                } else if(member_tag.size() == 0) {
                    Toast.makeText(EditForetActivity.this, "?????? 1?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("??????",null);

        builder.setView(region_view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    class EditForetResponse extends AsyncHttpResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                String rt = json.getString("foretRT");
                if(rt.equals("OK")) {
                    modifyCheck();
                } else {
                    Toast.makeText(EditForetActivity.this, "????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(EditForetActivity.this, "?????? ??????", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(EditForetActivity.this, "???????????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

}