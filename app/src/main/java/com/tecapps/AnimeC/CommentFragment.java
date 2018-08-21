package com.tecapps.AnimeC;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.adapter.CommentAdapter;
import com.example.item.ItemComment;
import com.example.util.Constant;
import com.example.util.ItemOffsetDecoration;
import com.example.util.JsonUtils;
import com.example.util.RecyclerTouchListener;
import com.zanjou.http.debug.Logger;
import com.zanjou.http.request.Request;
import com.zanjou.http.request.RequestStateListener;
import com.zanjou.http.response.JsonResponseListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;

public class CommentFragment extends Fragment {

    ArrayList<ItemComment> mCommentList;
    public RecyclerView recyclerView;
    CommentAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout lyt_not_found;
    MyApplication MyApp;
    String selectedVideoId, selectedInWhich;
    private static final String bundleVideoId = "ID";
    private static final String bundleWhich = "inWhich";
    ProgressDialog pDialog;

    public static CommentFragment newInstance(String VideoId) {
        CommentFragment f = new CommentFragment();
        Bundle args = new Bundle();
        args.putString(bundleVideoId, VideoId);
         f.setArguments(args);
        return f;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //No call for super().
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.row_recyclerview, container, false);
        selectedVideoId = getArguments().getString(bundleVideoId);

        MyApp = MyApplication.getAppInstance();
        mCommentList = new ArrayList<>();
        pDialog = new ProgressDialog(getActivity());
        lyt_not_found = (LinearLayout) rootView.findViewById(R.id.lyt_not_found);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.vertical_courses_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.item_offset);
        recyclerView.addItemDecoration(itemDecoration);

        seeComment();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (position == 0) {
                      if (MyApp.getIsLogin()) {
                        showComment();
                     } else {
                        Toast.makeText(getActivity(), getString(R.string.login_msg), Toast.LENGTH_SHORT).show();
                      }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        return rootView;
    }

    private void seeComment() {
        if (JsonUtils.isNetworkAvailable(getActivity())) {
            new getComment().execute(Constant.COMMENT_URL +selectedVideoId+"&api_key="+Constant.SERVER_API_KEY);
         } else {
            Toast.makeText(getActivity(), getString(R.string.network_msg), Toast.LENGTH_SHORT).show();
        }
    }


    private void showComment() {
        final Dialog mDialog = new Dialog(getActivity(), R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.dialog_comment);
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        final EditText edtComment = (EditText) mDialog.findViewById(R.id.edt_comment);
        final ImageView imgSent = (ImageView) mDialog.findViewById(R.id.image_sent);
        ShapedImageView image = (ShapedImageView) mDialog.findViewById(R.id.avatar);

        imgSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (JsonUtils.isNetworkAvailable(getActivity())) {
                    if (!edtComment.getText().toString().isEmpty()) {
                        sentComment(edtComment.getText().toString());
                        mDialog.dismiss();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network_msg), Toast.LENGTH_SHORT).show();
                }
            }
        });


        edtComment.requestFocus();
        mDialog.show();
    }


    private class getComment extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            showProgress(false);
            if (null == result || result.length() == 0) {
                lyt_not_found.setVisibility(View.VISIBLE);
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);

                    if (jsonArray.length() > 0) {
                        JSONObject objJson;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objJson = jsonArray.getJSONObject(i);

                            if (objJson.has(Constant.MSG)) {
                                mCommentList.add(0, null);
                            } else {
                                if (i == 0) {
                                    mCommentList.add(0, null);
                                }
                                JSONArray jsonArraychild = objJson.getJSONArray(Constant.USER_ARRAY_NAME);
                                if (jsonArraychild.length() == 0) {

                                } else {
                                    for (int j = 0; j < jsonArraychild.length(); j++) {
                                        JSONObject objChild = jsonArraychild.getJSONObject(j);
                                        ItemComment objItem = new ItemComment();
                                        objItem.setUserName(objChild.getString(Constant.COMMENT_NAME));
                                        objItem.setImageIcon(objChild.getString(Constant.COMMENT_IMAGE));
                                        objItem.setCommentMsg(objChild.getString(Constant.COMMENT_DESC));
                                        objItem.setReply(false);
                                        mCommentList.add(objItem);
                                    }
                                }
                            }

                         }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                displayData();
            }
        }
    }


    private void displayData() {

        adapter = new CommentAdapter(getActivity(), mCommentList);
        recyclerView.setAdapter(adapter);
    }

    private void sentComment(String comment) {
        Request request = Request.create(Constant.COMMENT_POST_URL);
        request.setMethod("POST")
                .setTimeout(120)
                .setLogger(new Logger(Logger.ERROR))
                .addParameter("user_name",MyApp.getUserName())
                .addParameter("post_id", selectedVideoId)
                 .addParameter("comment_text", comment);

                request.setRequestStateListener(new RequestStateListener() {
                    @Override
                    public void onStart() {
                        showProgressDialog();
                    }

                    @Override
                    public void onFinish() {
                        dismissProgressDialog();
                    }

                    @Override
                    public void onConnectionError(Exception e) {
                        e.printStackTrace();
                    }
                })
                .setResponseListener(new JsonResponseListener() {
                    @Override
                    public void onOkResponse(JSONObject jsonObject) throws JSONException {

                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.LATEST_ARRAY_NAME);
                        JSONObject objJson = jsonArray.getJSONObject(0);
                        if (objJson.has(Constant.MSG)) {
                            Toast.makeText(getActivity(), getString(R.string.comment_success), Toast.LENGTH_SHORT).show();
                            mCommentList.clear();
                            seeComment();
                        }
                    }

                    @Override
                    public void onErrorResponse(JSONObject jsonObject) throws JSONException {

                    }

                    @Override
                    public void onParseError(JSONException e) {

                    }
                }).execute();
    }

    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lyt_not_found.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        pDialog.dismiss();
    }
}
