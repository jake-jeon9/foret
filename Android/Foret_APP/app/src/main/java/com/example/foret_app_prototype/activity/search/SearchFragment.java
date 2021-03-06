package com.example.foret_app_prototype.activity.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foret_app_prototype.R;
import com.example.foret_app_prototype.activity.MainActivity;
import com.example.foret_app_prototype.activity.foret.MakeForetActivity;
import com.example.foret_app_prototype.activity.foret.ViewForetActivity;
import com.example.foret_app_prototype.activity.login.SessionManager;
import com.example.foret_app_prototype.activity.menu.EditMyInfoActivity;
import com.example.foret_app_prototype.activity.menu.MyInfoActivity;
import com.example.foret_app_prototype.adapter.search.RecyclerAdapter2;
import com.example.foret_app_prototype.adapter.search.RecyclerAdapter3;
import com.example.foret_app_prototype.adapter.search.SearchAdapter;
import com.example.foret_app_prototype.helper.ProgressDialogHelper;
import com.example.foret_app_prototype.helper.getIPAdress;
import com.example.foret_app_prototype.model.ForetDTO;
import com.example.foret_app_prototype.model.MemberDTO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SearchFragment extends Fragment implements View.OnClickListener,
        TextView.OnEditorActionListener, AdapterView.OnItemClickListener {

    Toolbar toolbar;
    MainActivity activity;
    LinearLayout layout_search;
    ImageView button_back, button1, button_searchINTO, button_reset;
    Button button2, button3, button4, button5, button6, button7;
    RecyclerView recyclerView1, recyclerView2;
    FloatingActionButton button9;
    SearchAdapter searchAdapter;
    RecyclerAdapter2 adapter2;
    RecyclerAdapter3 adapter3;
    Context context;
    AutoCompleteTextView autoCompleteTextView;
    TextView button_searchGO;
    ListView search_listView;
    List<String> autoCompleteList; //???????????? ???????????? ????????? ?????????
    List<ForetDTO> search_resultList;
    //Button button;
    InputMethodManager inputMethodManager; //????????? ????????? ?????????
    Intent intent;
    ForetDTO foretDTO;
    MemberDTO memberDTO;

    List<String> tag_name;
    List<String> foret_name;
    List<String> region_gu;
    List<String> region_si;

    RecommandListResponse recommandListResponse;
    KeywordSearchResult keywordSearchResultResponse;

    AsyncHttpClient client;

    public SearchFragment() {
    }

    public SearchFragment(Context context) {
        this.context = context;
    }
    /*

    SearchFragment searchFragment;

    public SearchFragment getSearchFragment() {
        return searchFragment;
    }
    public void setSearchFragment(SearchFragment searchFragment) {
        this.searchFragment = searchFragment;
    }

    public SearchFragment() {
    }
    */


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("[test]","SearchFragment ??? onCreateView");
        //searchFragment = this;

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        toolbar = (androidx.appcompat.widget.Toolbar) rootView.findViewById(R.id.search_toolbar);
        activity = (MainActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(null);
        memberDTO = activity.getMemberDTO();
        setHasOptionsMenu(true);
        layout_search = rootView.findViewById(R.id.layout_search);
        button_back = rootView.findViewById(R.id.button_back);
        button1 = rootView.findViewById(R.id.button1); //??? ???????????? ?????? ???????????? ??????(????????? ????????? ?????? ??????)
        button2 = rootView.findViewById(R.id.button2); //????????????1
        button3 = rootView.findViewById(R.id.button3); //????????????2
        button4 = rootView.findViewById(R.id.button4); //????????????3
        button5 = rootView.findViewById(R.id.button5); //????????????4
        button6 = rootView.findViewById(R.id.button6); //????????????5
        button7 = rootView.findViewById(R.id.button7); //?????? ????????? ???????????? ??????
        button9 = rootView.findViewById(R.id.button9); //??????
        recyclerView1 = rootView.findViewById(R.id.recyclerView1); //???????????? ??????
        recyclerView2 = rootView.findViewById(R.id.recyclerView2); //???????????? ??????
        button_searchINTO = rootView.findViewById(R.id.button_searchINTO);
        button_reset = rootView.findViewById(R.id.button_reset);
        autoCompleteTextView = rootView.findViewById(R.id.autoCompleteTextView);
        button_searchGO = rootView.findViewById(R.id.button_searchGO);
        search_listView = rootView.findViewById(R.id.search_list); //?????? ????????? ????????? ????????????
        tag_name = new ArrayList<>();
        foret_name = new ArrayList<>();
        region_gu = new ArrayList<>();
        region_si = new ArrayList<>();
        search_resultList = new ArrayList<>();
        foretDTO = new ForetDTO();
        searchAdapter = new SearchAdapter(getContext(), R.layout.recycle_item3, search_resultList, memberDTO);

        autoCompleteList = new ArrayList<String>(); //??????????????? ????????? ?????????

        client = new AsyncHttpClient();
        recommandListResponse = new RecommandListResponse();
        keywordSearchResultResponse = new KeywordSearchResult();

        inputMethodManager = (InputMethodManager)activity.getSystemService(INPUT_METHOD_SERVICE); //????????? ??? ???????????? ????????? ???????????? Manager??????

        layout_search.setVisibility(View.GONE);

        autoCompleteTextView.setOnEditorActionListener(this);
        button_back.setOnClickListener(this);
        button_searchINTO.setOnClickListener(this);
        button_reset.setOnClickListener(this);
        button_searchGO.setOnClickListener(this);
        search_listView.setOnItemClickListener(this);

        recyclerView1.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerView2.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false)); //???????????? ?????????
        search_listView.setAdapter(searchAdapter);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button6.setOnClickListener(this);
        button7.setOnClickListener(this);
        button9.setOnClickListener(this);

        button_back.setOnClickListener(this);

        myTagData(); //??? ?????? ?????? ????????????
        hotTagData(); //?????? ?????? ????????????
        recommandForetData(); //???????????? ????????????
        autoCompleteData(); //??????????????? ????????? ????????? ????????????

        return rootView;
    }

    //??? ?????? ?????? ????????????
    private void myTagData() {
        ProgressDialogHelper.getInstance().getProgressbar(context,"????????? ??????????????????.");
        List<String> myTag = memberDTO.getTag();
        adapter2 = new RecyclerAdapter2(myTag, context,SearchFragment.this);
        recyclerView1.setAdapter(adapter2);
    }

    private void hotTagData() {
        RequestParams params = new RequestParams();
        params.put("rank", 5);
        client.post(getIPAdress.getInstance().getIp()+"/foret/tag/tag_rank.do", params, new HotTagResponse());
    }

    private void recommandForetData() {
        RequestParams params = new RequestParams();
        params.put("rank", 15);
        client.post(getIPAdress.getInstance().getIp()+"/foret/search/foret_rank.do", params, recommandListResponse);
    }

    private void autoCompleteData() {
        RequestParams params = new RequestParams();
        params.put("type", "name");
        client.post(getIPAdress.getInstance().getIp()+"/foret/search/search_keyword.do", new TagListResponse4());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_fragment_toolbar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_search:
                searchAdapter.clear();
                layout_search.setVisibility(View.VISIBLE);
                break;
            case R.id.item_menu: //????????? ?????? ??????
                DrawerLayout container = activity.findViewById(R.id.container);
                container.openDrawer(GravityCompat.END);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        String keyword = "";
        switch (v.getId()) {
            case R.id.button1: //??? ?????? ?????????????????? ??????
                intent = new Intent(context, MyInfoActivity.class);
                intent.putExtra("memberDTO", memberDTO);

                startActivity(intent);
                Toast.makeText(context, "??? ?????? ?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button2:
                keyword = button2.getText().toString().trim();
                search_keyword(keyword);
                break;
            case R.id.button3: //????????????2 ?????????????????? ??????
                keyword = button3.getText().toString().trim();
                search_keyword(keyword);
                break;
            case R.id.button4: //????????????3 ?????????????????? ??????
                keyword = button4.getText().toString().trim();
                search_keyword(keyword);
                break;
            case R.id.button5: //????????????4 ?????????????????? ??????
                keyword = button5.getText().toString().trim();
                search_keyword(keyword);
                break;
            case R.id.button6: //????????????5 ?????????????????? ??????
                keyword = button6.getText().toString().trim();
                search_keyword(keyword);
                break;
            case R.id.button7: //?????? ????????? ???????????? ??????
                goToMakeNewForet();
                break;
            case R.id.button9: //?????? ????????? ???????????? ??????
                //goToMakeNewForet();
                startActivity(new Intent(context,SearchTagActivity.class));
                break;
            case R.id.button_back:
                searchAdapter.clear();
                autoCompleteTextView.setText("");
                layout_search.setVisibility(View.GONE);
                break;
            case R.id.button_searchINTO :
                //toggleSoftInput : autoCompleteTextView??? ???????????? ??????, ????????? ?????????
                autoCompleteTextView.requestFocus(); //autoCompleteTextView??? ????????? ??????
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY); //????????? ?????????
                break;
            case R.id.button_reset :
                autoCompleteTextView.setText("");
                break;
            case R.id.button_searchGO:
                requsetSearch(); //????????? ?????? ??????
                break;
        }
    }

    public void search_keyword(String keyword) {
        searchAdapter.clear();
        layout_search.setVisibility(View.VISIBLE);
        autoCompleteTextView.setText(keyword);
        RequestParams params = new RequestParams();
        if (tag_name.contains(keyword)) {
            params.put("type", "tag");
        } else if (foret_name.contains(keyword)) {
            params.put("type", "name");
        } else if (region_si.contains(keyword)) {
            params.put("type", "region_si");
        } else if (region_gu.contains(keyword)) {
            params.put("type", "region_gu");
        }
        params.put("name", keyword);
        Log.e("[keyword]", keyword);
        client.post(getIPAdress.getInstance().getIp()+"/foret/search/foret_keyword_search.do", params, keywordSearchResultResponse);
    }

    //???????????? ??????
    private void requsetSearch() {
        searchAdapter.clear();
        String search_word = autoCompleteTextView.getText().toString().trim();
        if(search_word.equals("")) { //????????????
            Toast.makeText(context, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
            return;
        }
        //autoCompleteTextView??? ????????? ???????????? ????????????
        RequestParams params = new RequestParams();
        if (tag_name.contains(search_word)) {
            params.put("type", "tag");
        } else if (foret_name.contains(search_word)) {
            params.put("type", "name");
        } else if (region_si.contains(search_word)) {
            params.put("type", "region_si");
        } else if (region_gu.contains(search_word)) {
            params.put("type", "region_gu");
        }
        params.put("name", search_word);
        Log.e("[keyword]", search_word);
        client.post(getIPAdress.getInstance().getIp()+"/foret/search/foret_keyword_search.do", params, keywordSearchResultResponse);
        //?????? ????????? ???????????? ???????????? ??????.
        inputMethodManager.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
        Toast.makeText(context, search_word+"??? ???????????????.", Toast.LENGTH_SHORT).show();
    }

    private void goToMakeNewForet() {
        Intent intent = new Intent(context, MakeForetActivity.class);
        intent.putExtra("memberDTO",memberDTO);
        activity.startActivity(intent);
    }

    //???????????? ?????? ??????????????? ????????? ?????? ????????? ??????
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.e("[test]",searchAdapter.getItem(position).toString());

        ForetDTO foretDTO = searchAdapter.getItem(position);
        Intent intent = new Intent(context, ViewForetActivity.class);
        intent.putExtra("foret_id", foretDTO.getForet_id());
        intent.putExtra("memberDTO", memberDTO);
        startActivity(intent);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_SEARCH :
                requsetSearch();
                break;
            default: //???????????? ????????? ?????????
                requsetSearch();
        }
        return false;
    }

    class HotTagResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            List<String> hotTag = new ArrayList<>();
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if(json.getInt("total") > 0) {
                    JSONArray tag = json.getJSONArray("tag");
                    for (int a=0; a<tag.length(); a++) {
                        JSONObject object = tag.getJSONObject(a);
                        hotTag.add(object.getString("tag_name"));
                    }
                }
                button2.setText(hotTag.get(0));
                button3.setText(hotTag.get(1));
                button4.setText(hotTag.get(2));
                button5.setText(hotTag.get(3));
                button6.setText(hotTag.get(4));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ProgressDialogHelper.getInstance().removeProgressbar();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(context, "?????? ?????? ?????? ????????????", Toast.LENGTH_SHORT).show();
            ProgressDialogHelper.getInstance().removeProgressbar();
        }
    }

    //???????????? ?????????
    class RecommandListResponse extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if(json.getString("RT").equals("OK")) {
                    JSONArray foret = json.getJSONArray("foret");
                    for (int a=0; a<foret.length(); a++) {
                        JSONObject object = foret.getJSONObject(a);
                        foretDTO = new ForetDTO();
                        foretDTO.setForet_name(object.getString("name"));
                        List<String> tag = new ArrayList<>();
                        JSONArray tag_list = object.getJSONArray("tag");
                        for(int b=0; b<tag_list.length(); b++) {
                            tag.add(tag_list.getString(b));
                        }
                        foretDTO.setForet_tag(tag);
                        foretDTO.setForet_id(object.getInt("id"));
                        foretDTO.setIntroduce(object.getString("introduce"));
                        foretDTO.setReg_date(object.getString("reg_date"));

                        if (!object.isNull("photo")){
                            foretDTO.setForet_photo(object.getString("photo"));
                        }

                        if(object.getJSONArray("region_si").length() != 0) {
                            List<String> si = new ArrayList<>();
                            JSONArray si_list = object.getJSONArray("region_si");
                            for (int b=0; b<si_list.length(); b++) {
                                si.add(si_list.getString(b));
                            }
                            foretDTO.setForet_region_si(si);
                        }
                        if(object.getJSONArray("region_gu").length() != 0) {
                            List<String> gu = new ArrayList<>();
                            JSONArray gu_list = object.getJSONArray("region_gu");
                            for (int b=0; b<gu_list.length(); b++) {
                                gu.add(gu_list.getString(b));
                            }
                            foretDTO.setForet_region_gu(gu);
                        }
                        /*if(object.getString("photo")!= null) {
                            foretDTO.setForet_photo("http://34.72.240.24:8085/foret/storage/" + object.getString("photo"));
                        } else {
                            foretDTO.setForet_photo("");
                        }
                        Log.e("[TEST]", foretDTO.getForet_photo());*/
                        search_resultList.add(foretDTO);
                    }
                    Log.e("[test]","memberDTO? " +memberDTO.toString());
                    adapter3 = new RecyclerAdapter3(search_resultList, context,memberDTO);


                    recyclerView2.setAdapter(adapter3);
                    ProgressDialogHelper.getInstance().removeProgressbar();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ProgressDialogHelper.getInstance().removeProgressbar();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(context, "???????????? ?????? ???????????? ????????????", Toast.LENGTH_SHORT).show();
            ProgressDialogHelper.getInstance().removeProgressbar();
        }
    }

    //??????????????? ?????? ????????? ?????? ????????????
    class TagListResponse4 extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if (json.getString("RT").equals("OK")) {
                    JSONArray keyword = json.getJSONArray("keyword");
                    for (int a = 0; a < keyword.length(); a++) {
                        JSONObject object = keyword.getJSONObject(a);
                        autoCompleteList.add(object.getString("name"));
                        if (object.getString("type").equals("tag")) {
                            tag_name.add(object.getString("name"));
                        } else if (object.getString("type").equals("region_si")) {
                            region_si.add(object.getString("name"));
                        } else if (object.getString("type").equals("region_gu")) {
                            region_gu.add(object.getString("name"));
                        } else if (object.getString("type").equals("foret")) {
                            foret_name.add(object.getString("name"));
                        }
                        /*Log.e("[????????? ????????? ??????]", tag_name.size()+"");
                        Log.e("[????????? ????????? ??????]", region_si.size()+"");
                        Log.e("[????????? ????????? ??????]", region_gu.size()+"");
                        Log.e("[????????? ????????? ??????]", foret_name.size()+"");*/
                    }
                    Log.e("[????????????]", autoCompleteList.size()+"");
                }
                autoCompleteTextView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, autoCompleteList));
                ProgressDialogHelper.getInstance().removeProgressbar();
            } catch (JSONException e) {
                e.printStackTrace();
                ProgressDialogHelper.getInstance().removeProgressbar();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(context, "???????????? ?????? ?????? ?????? ????????????", Toast.LENGTH_SHORT).show();
            ProgressDialogHelper.getInstance().removeProgressbar();
        }
    }

    class KeywordSearchResult extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String str = new String(responseBody);
            try {
                JSONObject json = new JSONObject(str);
                if(json.getString("RT").equals("OK")) {
                    JSONArray foret = json.getJSONArray("foret");
                    search_resultList.clear();
                    for (int a=0; a<foret.length(); a++) {
                        JSONObject object = foret.getJSONObject(a);
                        foretDTO = new ForetDTO();
                        foretDTO.setForet_name(object.getString("name"));
                        List<String> tag = new ArrayList<>();
                        JSONArray tag_list = object.getJSONArray("tag");
                        for(int b=0; b<tag_list.length(); b++) {
                            tag.add(tag_list.getString(b));
                        }
                        foretDTO.setForet_tag(tag);
                        foretDTO.setForet_id(object.getInt("id"));
                        foretDTO.setIntroduce(object.getString("introduce"));
                        foretDTO.setReg_date(object.getString("reg_date"));
                        if (!object.isNull("photo")){
                            foretDTO.setForet_photo(object.getString("photo"));
                        }
                        if(object.getJSONArray("region_si").length() != 0) {
                            List<String> si = new ArrayList<>();
                            JSONArray si_list = object.getJSONArray("region_si");
                            for (int b=0; b<si_list.length(); b++) {
                                si.add(si_list.getString(b));
                            }
                            foretDTO.setForet_region_si(si);
                        }
                        if(object.getJSONArray("region_gu").length() != 0) {
                            List<String> gu = new ArrayList<>();
                            JSONArray gu_list = object.getJSONArray("region_gu");
                            for (int b=0; b<gu_list.length(); b++) {
                                gu.add(gu_list.getString(b));
                            }
                            foretDTO.setForet_region_gu(gu);
                        }
                        /*if(object.getString("photo")!= null) {
                            foretDTO.setForet_photo("http://34.72.240.24:8085/foret/storage/" + object.getString("photo"));
                        } else {
                            foretDTO.setForet_photo("");
                        }
                        Log.e("[TEST]", foretDTO.getForet_photo());*/

                        searchAdapter.add(foretDTO);
                        Log.e("????????? ?????????", searchAdapter.getCount()+"");
                    }
                    Toast.makeText(context, "???????????? : "+searchAdapter.getCount()+"???", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                }
                ProgressDialogHelper.getInstance().removeProgressbar();
            } catch (JSONException e) {
                e.printStackTrace();
                ProgressDialogHelper.getInstance().removeProgressbar();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(context, "?????? ?????? ?????? ??? ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
            ProgressDialogHelper.getInstance().removeProgressbar();
        }
    }
}