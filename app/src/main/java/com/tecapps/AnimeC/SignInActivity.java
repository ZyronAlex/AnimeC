package com.tecapps.AnimeC;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.zanjou.http.debug.Logger;
import com.zanjou.http.request.FileUploadListener;
import com.zanjou.http.request.Request;
import com.zanjou.http.request.RequestStateListener;
import com.zanjou.http.response.JsonResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class SignInActivity extends AppCompatActivity implements Validator.ValidationListener{

    String strEmail, strPassword, strMessage, strName, strUserId;
    @Required(order = 1)
    @Email(order = 2, message = "Por favor, verifique e digite um endereço de e-mail válido")
    EditText edtEmail;

    @Required(order = 3)
    @Password(order = 4, message = "Insira uma senha válida")
    @TextRule(order = 5, minLength = 4, message = "Insira uma senha corretamente")
    EditText edtPassword;
    private Validator validator;
    Button btnSingIn, btnSignUp, btnForgot, btnSkip;
    MyApplication MyApp;
    CheckBox checkBox;
     ProgressDialog pDialog;
     boolean iswhichscreen;
     String videoiddetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MyApp = MyApplication.getAppInstance();

        btnSignUp = (Button) findViewById(R.id.button_register_login_activity);
        btnSingIn = (Button) findViewById(R.id.button_login_activity);
        btnForgot = (Button) findViewById(R.id.button_forgotPassword_login_activity);
        btnSkip = (Button) findViewById(R.id.button_skip_login_activity);

        pDialog = new ProgressDialog(SignInActivity.this);
        edtEmail = (EditText) findViewById(R.id.editText_email_login_activity);
        edtPassword = (EditText) findViewById(R.id.editText_password_login_activity);
        checkBox = (CheckBox) findViewById(R.id.checkBox);

        Intent intent=getIntent();
        iswhichscreen=intent.getBooleanExtra("isfromdetail",false);
        videoiddetail=intent.getStringExtra("isvideoid");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        btnSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validateAsync();
            }
        });


        if (MyApp.getIsRemember()) {
            checkBox.setChecked(true);
            edtEmail.setText(MyApp.getRememberEmail());
            edtPassword.setText(MyApp.getRememberPassword());
        }

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onValidationSucceeded() {
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();
        if (checkBox.isChecked()) {
            MyApp.saveIsRemember(true);
            MyApp.saveRemember(strEmail, strPassword);
        } else {
            MyApp.saveIsRemember(false);
        }

        if (JsonUtils.isNetworkAvailable(SignInActivity.this)) {
            uploadData();
           // finish();
        } else {
            showToast(getString(R.string.network_msg));
        }
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        String message = failedRule.getFailureMessage();
        if (failedView instanceof EditText) {
            failedView.requestFocus();
            ((EditText) failedView).setError(message);
        } else {
            Toast.makeText(this, "Registro não salvo", Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadData() {
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();
        Request request = Request.create(Constant.LOGIN_URL);
        request.setMethod("POST")
                .setTimeout(120)
                .setLogger(new Logger(Logger.ERROR))
                .addParameter("email", strEmail)
                .addParameter("password", strPassword);


        request.setFileUploadListener(new FileUploadListener() {
            @Override
            public void onUploadingFile(File file, long size, long uploaded) {

            }
        })
                .setRequestStateListener(new RequestStateListener() {
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
                            strMessage = objJson.getString(Constant.MSG);
                            Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                        } else {
                            Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                            strName = objJson.getString(Constant.USER_NAME);
                            strUserId = objJson.getString(Constant.USER_ID);
                           strEmail=objJson.getString(Constant.USER_EMAIL);
                        }
                        setResult();

                    }

                    @Override
                    public void onErrorResponse(JSONObject jsonObject) throws JSONException {
                         Toast.makeText(SignInActivity.this,"Falha na autenticação",Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onParseError(JSONException e) {
                     }
                }).execute();
    }

    public void setResult() {

        if (Constant.GET_SUCCESS_MSG == 0) {
            showToast(strMessage);

        } else {
            MyApp.saveIsLogin(true);
            MyApp.saveLogin(strUserId, strName, strEmail);
            if(iswhichscreen)
            {
              //  ActivityCompat.finishAffinity(SignInActivity.this);
                Intent i = new Intent(SignInActivity.this, VideoPlay.class);
                i.putExtra("isvideoid",videoiddetail);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
            else {
                ActivityCompat.finishAffinity(SignInActivity.this);
                Intent i = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

        }

    }

    public void showProgressDialog() {
        pDialog.setMessage(getResources().getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        pDialog.dismiss();
    }

    public void showToast(String msg) {
        Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_LONG).show();
    }


}
