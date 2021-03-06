package com.example.foret_app_prototype.activity.foret;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.bumptech.glide.Glide;
import com.example.foret_app_prototype.R;
import com.example.foret_app_prototype.activity.foret.board.WriteForetBoardActivity;
import com.example.foret_app_prototype.activity.notify.APIService;
import com.example.foret_app_prototype.activity.notify.Client;
import com.example.foret_app_prototype.activity.notify.Data;
import com.example.foret_app_prototype.activity.notify.Response;
import com.example.foret_app_prototype.activity.notify.Sender;
import com.example.foret_app_prototype.activity.notify.Token;
import com.example.foret_app_prototype.activity.search.SearchFragment;
import com.example.foret_app_prototype.adapter.foret.BoardViewAdapter;
import com.example.foret_app_prototype.adapter.foret.ViewForetBoardAdapter;
import com.example.foret_app_prototype.helper.getIPAdress;
import com.example.foret_app_prototype.model.Foret;
import com.example.foret_app_prototype.model.ForetBoardDTO;
import com.example.foret_app_prototype.model.ForetDTO;
import com.example.foret_app_prototype.model.ForetViewDTO;
import com.example.foret_app_prototype.model.MemberDTO;
import com.example.foret_app_prototype.model.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;

public class ViewForetActivity extends AppCompatActivity implements View.OnClickListener {
    MemberDTO memberDTO;
    int foret_id;
    int member_id;
    String foret_leader;

    ForetViewDTO foretViewDTO;
    ForetBoardDTO foretBoardDTO;
    List<ForetBoardDTO> noticeBoardDTOList;
    List<ForetBoardDTO> foretBoardDTOList;
    List<ForetBoardDTO> noticeTotalList;
    List<ForetBoardDTO> boardTotalList;

    ViewForetBoardAdapter viewForetBoardAdapter;
    BoardViewAdapter boardViewAdapter;

    Toolbar toolbar;
    Intent intent;
    RecyclerView board_list, listView_notice;
    TextView textView_tag, textView_region, textView_intro, textView_member,
            textView_master, textView_foretName, textView_date, button10, button11;
    Button button1, button2, button3;
    ImageView imageView_profile;
    LinearLayout noti_layout, board_layout;
    FloatingActionButton write_fab_add;

    String url;
    AsyncHttpClient client;
    ViewForetResponse viewForetResponse;
    MemberResponse memberResponse;
    NoticeResponse noticeResponse;
    BoardResponse boardResponse;
    JoinResponse joinResponse;
    LeaveResponse leaveResponse;
    LeaderResponse leaderResponse;
    BoardTotalResponse boardTotalResponse;
    NoticeTotalResponse noticeTotalResponse;

    // ???????????? ?????????
    int noti_pg = 1;
    final int noti_size = 3;
    int total_noti_pg;

    // ?????? ?????????
    int board_pg = 1;
    final int board_size = 5;
    int total_board_pg;

    String rank = ""; // guest = ????????????, member = ?????????, leader = ??????

    String foretname;
    String myNickName;

    APIService apiService;
    boolean notify = false;
    String hisUid, myUid;
    //SearchFragment searchFragment;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        memberDTO = (MemberDTO) getIntent().getSerializableExtra("memberDTO");
        //  searchFragment = new SearchFragment(memberDTO);
        //  searchFragment = searchFragment.getSearchFragment();
        context = this;
        setContentView(R.layout.activity_view_foret);

        // ????????? ?????? ??????
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.foret4));
        memberDTO = (MemberDTO) getIntent().getSerializableExtra("memberDTO");
        foret_id = getIntent().getIntExtra("foret_id",0);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFindId(); // ?????? ?????????
        addFab(); // ????????? ??????
//        getMember(); // ?????? ????????????
        getTotalNotice();
        getTotalBoard();
        checkUserStatus();

        Log.d("[TEST]", "????????? ?????? ?????? => " + foret_id);
        Log.d("[TEST]", "????????? ?????? ?????? => " + memberDTO.getId());
        Log.d("[TEST]", "????????? ?????? ?????? => " + memberDTO.getNickname());
        myNickName = memberDTO.getNickname();

        // ?????? ?????? ??????
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

    }

    @Override
    protected void onStart() {
        super.onStart();
        getMember(); // ?????? ?????? ????????????
    }

    @Override
    protected void onResume() {
        super.onResume();
//        getForet(); // ?????? ??????

        board_list.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView board_list, int newState) {
                super.onScrollStateChanged(board_list, newState);
                Log.d("[TEST]", "onScrollStateChanged ?????? => " + newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView board_list, int dx, int dy) {
                super.onScrolled(board_list, dx, dy);
                Log.d("[TEST]", "onScrolled ?????? => " + dx + " / " + dy);
                int lastPosition = ((LinearLayoutManager) board_list.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int totalCount = board_list.getAdapter().getItemCount();

                if (lastPosition == totalCount) {
                    board_pg++;
                    getBoard();
                    Log.d("[TEST]", "board_pg ?????? => " + board_pg);
                }
            }
        });

    }

    // ????????? ??????
    private void addFab() {
        write_fab_add = findViewById(R.id.fab_add3);
        write_fab_add.bringToFront();
        write_fab_add.setVisibility(View.GONE);
        write_fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(ViewForetActivity.this, WriteForetBoardActivity.class);
                intent.putExtra("foret_id", foret_id);
                intent.putExtra("memberDTO", memberDTO);
                intent.putExtra("foretname", foretname);
                startActivity(intent);
            }
        });
    }

    private void getMember() {
        url = getIPAdress.getInstance().getIp()+"/foret/search/member.do";
        client = new AsyncHttpClient();
        memberResponse = new MemberResponse();
        RequestParams params = new RequestParams();
        params.put("id", memberDTO.getId());
        client.post(url, params, memberResponse);
    }

    private void getLeader() {
        url = getIPAdress.getInstance().getIp()+"/foret/search/member.do";
        client = new AsyncHttpClient();
        leaderResponse = new LeaderResponse();
        RequestParams params = new RequestParams();
        params.put("id", foretViewDTO.getLeader_id());
        client.post(url, params, leaderResponse);
    }

    private void getForet() {
        url = getIPAdress.getInstance().getIp()+"/foret/search/foretSelect.do";
        client = new AsyncHttpClient();
        viewForetResponse = new ViewForetResponse();
        RequestParams params = new RequestParams();

        Log.e("[test]","????????? ??????,"+foret_id+"/"+memberDTO.getId());

        params.put("foret_id", foret_id);
        params.put("member_id", memberDTO.getId());
        client.post(url, params, viewForetResponse);
    }

    private void getNotice() {
        url = getIPAdress.getInstance().getIp()+"/foret/search/boardList.do";
        client = new AsyncHttpClient();
        noticeResponse = new NoticeResponse();
        RequestParams params = new RequestParams();
        params.put("type", 2);
        params.put("foret_id", foret_id);
        params.put("pg", noti_pg);
        params.put("size", noti_size);
        params.put("inquiry_type", 1);
        client.post(url, params, noticeResponse);
    }

    private void getBoard() {
        url = getIPAdress.getInstance().getIp()+"/foret/search/boardList.do";
        client = new AsyncHttpClient();
        boardResponse = new BoardResponse();
        RequestParams params = new RequestParams();
        params.put("type", 4);
        params.put("foret_id", foret_id);
        params.put("pg", board_pg);
        params.put("size", board_size);
        params.put("inquiry_type", 1);
        client.post(url, params, boardResponse);
    }

    private void getTotalBoard() {
        url = getIPAdress.getInstance().getIp()+"/foret/search/boardList.do";
        client = new AsyncHttpClient();
        boardTotalResponse = new BoardTotalResponse();
        RequestParams params = new RequestParams();
        params.put("type", 4);
        params.put("foret_id", foret_id);
        params.put("inquiry_type", 1);
        client.post(url, params, boardTotalResponse);
    }

    private void getTotalNotice() {
        url = getIPAdress.getInstance().getIp()+"/foret/search/boardList.do";
        client = new AsyncHttpClient();
        noticeTotalResponse = new NoticeTotalResponse();
        RequestParams params = new RequestParams();
        params.put("type", 2);
        params.put("foret_id", foret_id);
        params.put("inquiry_type", 1);
        client.post(url, params, noticeTotalResponse);
    }

    private void foretJoin() {
        url = getIPAdress.getInstance().getIp()+"/foret/foret/foret_member_insert.do";
        client = new AsyncHttpClient();
        joinResponse = new JoinResponse();
        RequestParams params = new RequestParams();
        params.put("foret_id", foret_id);
        params.put("member_id", memberDTO.getId());
        client.post(url, params, joinResponse);
    }

    private void foretLeave() {
        url = getIPAdress.getInstance().getIp()+"/foret/foret/foret_member_delete.do";
        client = new AsyncHttpClient();
        leaveResponse = new LeaveResponse();
        RequestParams params = new RequestParams();
        params.put("foret_id", foret_id);
        params.put("member_id", memberDTO.getId());
        client.post(url, params, leaveResponse);
    }


    private void getFindId() {
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button10 = findViewById(R.id.button10);
        button11 = findViewById(R.id.button11);
        imageView_profile = findViewById(R.id.imageView_profile);
        textView_foretName = findViewById(R.id.textView_foretName);
        textView_tag = findViewById(R.id.textView_tag);
        textView_region = findViewById(R.id.textView_region);
        textView_member = findViewById(R.id.textView_member);
        textView_master = findViewById(R.id.textView_master);
        textView_date = findViewById(R.id.textView_date);
        textView_intro = findViewById(R.id.textView_intro);
        listView_notice = findViewById(R.id.listView_notice);
        board_list = findViewById(R.id.board_list);
        noti_layout = findViewById(R.id.noti_layout);
        board_layout = findViewById(R.id.board_layout);
        foretBoardDTOList = new ArrayList<>();

        button1.setOnClickListener(this); // ????????????
        button2.setOnClickListener(this); // ????????????
        button3.setOnClickListener(this); // ????????????
        button10.setOnClickListener(this); // ??????
        button11.setOnClickListener(this); // ??????
    }

    private void dataSetting() {

        if(!foretViewDTO.getPhoto().equals("null") || (foretViewDTO.getPhoto() != null)){
            Glide.with(this).load(foretViewDTO.getPhoto()).
                    placeholder(R.drawable.sss).
                    error(R.drawable.sss)
                    .into(imageView_profile);
        }



        textView_foretName.setText(foretViewDTO.getName());
        String[] str_tag = new String[foretViewDTO.getForet_tag().size()];
        for (int i = 0; i < foretViewDTO.getForet_tag().size(); i++) {
            str_tag[i] = "#" + foretViewDTO.getForet_tag().get(i) + " ";
        }
        textView_tag.setText(Arrays.toString(str_tag));
        String[] str_si = new String[foretViewDTO.getForet_region_si().size()];
        String[] str_gu = new String[foretViewDTO.getForet_region_gu().size()];
        String str_region = "";
        for (int i = 0; i < foretViewDTO.getForet_region_si().size(); i++) {
            str_si[i] = foretViewDTO.getForet_region_si().get(i);
            str_gu[i] = foretViewDTO.getForet_region_gu().get(i);
            str_region += str_si[i] + " " + str_gu[i] + ", ";
        }
        textView_region.setText(str_region.substring(0, str_region.length() - 2));
        textView_member.setText(foretViewDTO.getMember().size() + "/" + foretViewDTO.getMax_member());
        textView_master.setText("?????? ?????? : " + memberDTO.getNickname());
        String date = foretViewDTO.getReg_date().substring(0, 10);
        textView_date.setText("Since : " + date);
        textView_intro.setText(foretViewDTO.getIntroduce());

        //????????? ????????????
        foretname = foretViewDTO.getName();
    }

    private void setBoard() {
        viewForetBoardAdapter = new ViewForetBoardAdapter(this, memberDTO, foretBoardDTOList);
        listView_notice.setLayoutManager(new LinearLayoutManager(this));
        listView_notice.setAdapter(viewForetBoardAdapter);

        boardViewAdapter = new BoardViewAdapter(this, memberDTO, foretBoardDTOList);
        board_list.setLayoutManager(new LinearLayoutManager(this));
        board_list.setAdapter(boardViewAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1: // ???????????? ?????? ?????? ??? -> ?????????????????? ????????? ??????
                joinDialog();
                break;
            case R.id.button2: // ???????????? ?????? ??? -> ????????????
                leaveDialog();
                break;
            case R.id.button3: // ???????????? ??????, ???????????? ??? -> ???????????? ???????????? ??????
                intent = new Intent(this, EditForetActivity.class);
                intent.putExtra("foret", foretViewDTO);
                intent.putExtra("memberDTO", memberDTO);
                intent.putExtra("leader",foret_leader);
                startActivity(intent);
                break;
            case R.id.button10: // ???????????? ??????
                noti_pg++;
                getNotice();
                if (noti_pg > 1) {
                    button11.setVisibility(View.VISIBLE);
                } else if (viewForetBoardAdapter.getItemCount() < 3) {
                    button11.setVisibility(View.INVISIBLE);
                } else if (viewForetBoardAdapter.getItemCount() == 3) {
                    button11.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.button11: // ???????????? ??????
                noti_pg--;
                getNotice();
                if (noti_pg == 1) {
                    button11.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }

    private void joinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("?????? ???????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ?????? ??????
                foretJoin();
            }
        });
        builder.setNegativeButton("??????", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void leaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("????????? ?????? ???????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ?????? ??????
                foretLeave();
            }
        });
        builder.setNegativeButton("??????", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //?????? ????????? ?????? ?????? ?????? ????????????
    class MemberResponse extends AsyncHttpResponseHandler {
        @Override
        public void onStart() {
            super.onStart();
            Log.d("[TEST]", "MemeberResponse onStart() ??????");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            Log.d("[TEST]", "MemeberResponse onStart() ??????");
            getForet();
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
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getId());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getNickname());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getName());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getBirth());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getPhoto());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getEmail());
                    Log.d("[member]", "?????? ????????? ==== " + memberDTO.getPassword());
                    Log.d("[member]", "????????? ????????? ==== " + memberDTO.getReg_date());
                    Log.d("[TEST]", "???????????? ?????????");
                } else {
                    Log.d("[TEST]", "???????????? ????????????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ViewForetActivity.this, "MemeberResponse ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

    class LeaderResponse extends AsyncHttpResponseHandler {

        @Override
        public void onFinish() {
            super.onFinish();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                String RT = json.getString("RT");
                if (RT.equals("OK")) {
                    JSONArray member = json.getJSONArray("member");
                    JSONObject temp = member.getJSONObject(0);
                    foret_leader = temp.getString("nickname");
                    Log.d("[TEST]", "?????? ?????? ?????????");
                    Log.d("[TEST]", "foret_leader => " + foret_leader);
                    textView_master.setText("?????? ?????? : " + foret_leader);
                } else {
                    Log.d("[TEST]", "?????? ?????? ????????????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ViewForetActivity.this, "MemeberResponse ?????? ??????", Toast.LENGTH_SHORT).show();
            Log.d("[SERVER]", "LeaderResponse ?????? ??????" + statusCode);
        }
    }

    //?????? ????????? ?????? ?????? ??????
    class ViewForetResponse extends AsyncHttpResponseHandler {
        @Override
        public void onStart() {
            super.onStart();
            Log.d("[TEST]", "ViewForetResponse onStart() ??????");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            Log.d("[TEST]", "ViewForetResponse onFinish() ??????");
            dataSetting();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            Gson gson = new Gson();
            try {
                JSONObject json = new JSONObject(str);
                String RT = json.getString("RT");

                if (RT.equals("OK")) {
                    JSONArray foret = json.getJSONArray("foret");
                    JSONObject temp = foret.getJSONObject(0);
                    foretViewDTO = new ForetViewDTO();
                    foretViewDTO = gson.fromJson(temp.toString(), ForetViewDTO.class);

                    JSONArray region_si = temp.getJSONArray("region_si");
                    List<String> si_list = new ArrayList<>();
                    for (int i = 0; i < region_si.length(); i++) {
                        String str_si = String.valueOf(region_si.get(i));
                        si_list.add(str_si);
                    }
                    foretViewDTO.setForet_region_si(si_list);

                    JSONArray region_gu = temp.getJSONArray("region_gu");
                    List<String> gu_list = new ArrayList<>();
                    for (int i = 0; i < region_gu.length(); i++) {
                        String str_gu = String.valueOf(region_gu.get(i));
                        gu_list.add(str_gu);
                    }
                    foretViewDTO.setForet_region_gu(gu_list);

                    JSONArray tag = temp.getJSONArray("tag");
                    List<String> tag_list = new ArrayList<>();
                    for (int i = 0; i < tag.length(); i++) {
                        String str_tag = String.valueOf(tag.get(i));
                        tag_list.add(str_tag);
                    }
                    foretViewDTO.setForet_tag(tag_list);

                    JSONArray member = temp.getJSONArray("member");
                    List<Integer> member_list = new ArrayList<>();
                    for (int i = 0; i < member.length(); i++) {
                        int mem = (int) member.get(i);
                        member_list.add(mem);
                    }
                    foretViewDTO.setMember(member_list);

                    foretViewDTO.setRank(json.getString("rank"));
                    Log.d("[TEST]", "???????????? ?????????");
                    Log.d("[TEST]", "foretDTO.getId => " + foretViewDTO.getId());
                    Log.d("[TEST]", "foretDTO.getPhoto => " + foretViewDTO.getPhoto());
                    Log.d("[TEST]", "foretDTO.getName => " + foretViewDTO.getName());
                    Log.d("[TEST]", "foretDTO.getIntroduce => " + foretViewDTO.getIntroduce());
                    Log.d("[TEST]", "foretDTO.getLeader_id => " + foretViewDTO.getLeader_id());
                    Log.d("[TEST]", "foretDTO.getForet_region_gu => " + foretViewDTO.getForet_region_gu().size());
                    Log.d("[TEST]", "foretDTO.getForet_region_si => " + foretViewDTO.getForet_region_si().size());
                    Log.d("[TEST]", "foretDTO.getForet_tag => " + foretViewDTO.getForet_tag().size());
                    Log.d("[TEST]", "foretDTO.getRank => " + foretViewDTO.getRank());


                    rank = foretViewDTO.getRank();
                    if (rank.equals("guest")) { // ?????? ????????? - ????????????
                        button1.setVisibility(View.VISIBLE);
                        noti_layout.setVisibility(View.GONE);
                        board_layout.setVisibility(View.GONE);
                        write_fab_add.setVisibility(View.GONE);
                    } else if (rank.equals("member")) { // ????????? ?????? - ????????????
                        button2.setVisibility(View.VISIBLE);
                        noti_layout.setVisibility(View.VISIBLE);
                        board_layout.setVisibility(View.VISIBLE);
                        write_fab_add.setVisibility(View.VISIBLE);
                    } else if (rank.equals("leader")) { // ?????? - ????????????
                        button3.setVisibility(View.VISIBLE);
                        noti_layout.setVisibility(View.VISIBLE);
                        board_layout.setVisibility(View.VISIBLE);
                        write_fab_add.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(ViewForetActivity.this, "?????????", Toast.LENGTH_SHORT).show();
                    }
                    getLeader();

                } else {
                    Log.d("[TEST]", "???????????? ????????????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ViewForetActivity.this, "ViewForetResponse ?????? ??????", Toast.LENGTH_SHORT).show();
            Log.e("[test]", "ViewForetResponse ?????? ?????? ???????????? " + statusCode + "/ error? " + error.getStackTrace());
        }
    }

    //?????? ????????? ?????? ?????????
    class NoticeResponse extends AsyncHttpResponseHandler {
        @Override
        public void onStart() {
            Log.d("[TEST]", "NoticeResponse onStart() ??????");
        }

        @Override
        public void onFinish() {
            Log.d("[TEST]", "NoticeResponse onFinish() ??????");
            viewForetBoardAdapter = new ViewForetBoardAdapter(ViewForetActivity.this, memberDTO, noticeBoardDTOList);
            listView_notice.setLayoutManager(new LinearLayoutManager(ViewForetActivity.this));
            listView_notice.setAdapter(viewForetBoardAdapter);
            viewForetBoardAdapter.setItems(noticeBoardDTOList);
            viewForetBoardAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            noticeBoardDTOList = new ArrayList<>();
            String str = new String(responseBody);
            Gson gson = new Gson();
            try {
                JSONObject json = new JSONObject(str);
                String RT = json.getString("RT");
                if (RT.equals("OK")) {
                    JSONArray board = json.getJSONArray("board");
                    for (int i = 0; i < board.length(); i++) {
                        JSONObject temp = board.getJSONObject(i);
                        foretBoardDTO = new ForetBoardDTO();
                        foretBoardDTO = gson.fromJson(temp.toString(), ForetBoardDTO.class);
                        noticeBoardDTOList.add(foretBoardDTO);
                        Log.d("[TEST]", "???????????????????????? => " + noticeBoardDTOList.get(i).getId());
                    }

                    Log.d("[TEST]", "foretBoardDTOList.size() => " + noticeBoardDTOList.size());
                    Log.d("[TEST]", "?????? ????????? ?????????");
                } else {
                    Log.d("[TEST]", "?????? ????????? ????????????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ViewForetActivity.this, "NoticeResponse ?????? ??????", Toast.LENGTH_SHORT).show();
            Log.d("[SERVER]", "NoticeResponse ?????? ??????" + statusCode);
        }
    }

    //?????? ????????? ?????? ?????????
    class BoardResponse extends AsyncHttpResponseHandler {
        @Override
        public void onStart() {
            Log.d("[TEST]", "BoardResponse onStart() ??????");
        }

        @Override
        public void onFinish() {
            Log.d("[TEST]", "BoardResponse onFinish() ??????");
            boardViewAdapter = new BoardViewAdapter(ViewForetActivity.this, memberDTO, foretBoardDTOList);
            board_list.setLayoutManager(new LinearLayoutManager(ViewForetActivity.this));
            board_list.setAdapter(boardViewAdapter);
            boardViewAdapter.setItems(foretBoardDTOList);
            boardViewAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            foretBoardDTOList = new ArrayList<>();
            String str = new String(responseBody);
            Gson gson = new Gson();
            try {
                JSONObject json = new JSONObject(str);
                String RT = json.getString("RT");
                if (RT.equals("OK")) {
                    JSONArray board = json.getJSONArray("board");
                    for (int i = 0; i < board.length(); i++) {
                        JSONObject temp = board.getJSONObject(i);
                        foretBoardDTO = new ForetBoardDTO();
                        foretBoardDTO = gson.fromJson(temp.toString(), ForetBoardDTO.class);
                        foretBoardDTOList.add(foretBoardDTO);
                        Log.d("[TEST]", "?????? ????????? => " + foretBoardDTOList.get(i).getId());
                    }
                    Log.d("[TEST]", "foretBoardDTOList.size() => " + foretBoardDTOList.size());
                    Log.d("[TEST]", "?????? ????????? ?????????");
                } else {
                    Log.d("[TEST]", "?????? ????????? ????????????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ViewForetActivity.this, "BoardResponse ?????? ??????", Toast.LENGTH_SHORT).show();
            Log.e("[SERVER]", "BoardResponse ?????? ??????" + statusCode);
        }
    }

    //?????? ????????? ????????????
    class NoticeTotalResponse extends AsyncHttpResponseHandler {

        @Override
        public void onFinish() {
            super.onFinish();
            getNotice();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            noticeTotalList = new ArrayList<>();
            String str = new String(responseBody);
            Gson gson = new Gson();
            try {
                JSONObject json = new JSONObject(str);
                String RT = json.getString("RT");
                if (RT.equals("OK")) {
                    JSONArray board = json.getJSONArray("board");
                    for (int i = 0; i < board.length(); i++) {
                        JSONObject temp = board.getJSONObject(i);
                        foretBoardDTO = new ForetBoardDTO();
                        foretBoardDTO = gson.fromJson(temp.toString(), ForetBoardDTO.class);
                        noticeTotalList.add(foretBoardDTO);
                    }

                    Log.d("[TEST]", "?????? ?????? ?????????");

                    total_noti_pg = noticeTotalList.size();
                    Log.e("[TEST]", "?????? ????????? ??? ??? => " + total_noti_pg);

                } else {
                    Log.d("[TEST]", "?????? ????????? ????????? ????????????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ViewForetActivity.this, "NoticeResponse ?????? ??????", Toast.LENGTH_SHORT).show();
            Log.d("[SERVER]", "NoticeResponse ?????? ??????" + statusCode);
        }
    }

    //?????? ?????? ?????? ??????
    class BoardTotalResponse extends AsyncHttpResponseHandler {

        @Override
        public void onFinish() {
            super.onFinish();
            getBoard();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            boardTotalList = new ArrayList<>();
            String str = new String(responseBody);
            Gson gson = new Gson();
            try {
                JSONObject json = new JSONObject(str);
                String RT = json.getString("RT");
                if (RT.equals("OK")) {
                    JSONArray board = json.getJSONArray("board");
                    for (int i = 0; i < board.length(); i++) {
                        JSONObject temp = board.getJSONObject(i);
                        foretBoardDTO = new ForetBoardDTO();
                        foretBoardDTO = gson.fromJson(temp.toString(), ForetBoardDTO.class);
                        boardTotalList.add(foretBoardDTO);
                    }
                    Log.d("[TEST]", "?????? ?????? ?????????");

                    total_board_pg = boardTotalList.size();
                    Log.e("[TEST]", "?????? ????????? ??? ??? => " + total_board_pg);

                } else {
                    Log.d("[TEST]", "?????? ????????? ????????? ????????????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ViewForetActivity.this, "BoardResponse ?????? ??????", Toast.LENGTH_SHORT).show();
            Log.e("[SERVER]", "BoardResponse ?????? ??????" + statusCode);
        }
    }

    //?????? ?????????
    class JoinResponse extends AsyncHttpResponseHandler {
        @Override
        public void onStart() {
            super.onStart();
            Log.d("[TEST]", "JoinResponse onStart() ??????");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            Log.d("[TEST]", "JoinResponse onStart() ??????");
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                String foretMemberRT = json.getString("foretMemberRT");
                if (foretMemberRT.equals("OK")) {
                    Toast.makeText(ViewForetActivity.this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    Log.d("[TEST]", "?????? ??????");
                    button2.setVisibility(View.VISIBLE);
                    button1.setVisibility(View.GONE);
                    write_fab_add.setVisibility(View.VISIBLE);
                    noti_layout.setVisibility(View.VISIBLE);
                    board_layout.setVisibility(View.VISIBLE);

                    //????????? ????????? ??????
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    myUid = user.getUid();
                    String timestamp = "" + System.currentTimeMillis();
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("uid", myUid);
                    hashMap.put("joinedDate", timestamp);
                    hashMap.put("participantName", myNickName);

                    hisUid = "";


                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                    ref.child(foretname).child("participants").child(user.getUid()).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ViewForetActivity.this, "????????? ?????? ??????", Toast.LENGTH_LONG).show();

                                    DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Groups");
                                    ref2.child(foretname).child("participants").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                hisUid = ds.getKey();
                                                //
                                                notify = true;
                                                if (!myUid.equals(ds.getKey())) {
                                                    //????????????
                                                    updateNewItem("GROUP_NEW_ITEM", myUid, hisUid, "????????? ????????? ?????????????????????.", "" + System.currentTimeMillis());
                                                    String msg = "?????? Foret ?????? ??????! ??????????????????";
                                                    //??????
                                                    DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                                                    database.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            ModelUser user = snapshot.getValue(ModelUser.class);

                                                            if (notify) {
                                                                sendNotification(hisUid, user.getNickname(), msg);
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
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ViewForetActivity.this, "????????? ?????? ??????" + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                } else {
                    Log.d("[TEST]", "?????? ??????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ViewForetActivity.this, "JoinResponse ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

    //?????????
    class LeaveResponse extends AsyncHttpResponseHandler {
        @Override
        public void onStart() {
            super.onStart();
            Log.d("[TEST]", "LeaveResponse onStart() ??????");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            Log.d("[TEST]", "LeaveResponse onStart() ??????");
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                String foretMemberRT = json.getString("foretMemberRT");
                if (foretMemberRT.equals("OK")) {
                    Toast.makeText(ViewForetActivity.this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    Log.d("[TEST]", "?????? ??????");
                    button2.setVisibility(View.VISIBLE);
                    button1.setVisibility(View.GONE);
                    noti_layout.setVisibility(View.GONE);
                    board_layout.setVisibility(View.GONE);
                } else {
                    Log.d("[TEST]", "?????? ??????");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(ViewForetActivity.this, "LeaveResponse ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }


    // ?????? ?????? ??????
    private void checkUserStatus() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

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
                    Data data = new Data(myUid, message, foretname + "??? ?????? ??????", hisUid,
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
