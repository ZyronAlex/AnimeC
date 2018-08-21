package com.tecapps.AnimeC;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class SignUpActivity extends AppCompatActivity implements Validator.ValidationListener {
    @Required(order = 1)
    EditText edtFullName;

    @Required(order = 2)
    @Email(order = 3, message = "Por favor, verifique e digite um endereço de e-mail válido")
    EditText edtEmail;

    @Required(order = 4)
    @Password(order = 5, message = "Insira uma senha válida")
    @TextRule(order = 6, minLength = 4, message = "Insira uma senha corretamente")
    EditText edtPassword;

    @Required(order = 7)
    @TextRule(order = 8, message = "Digite um número de telefone válido", minLength = 0, maxLength = 14)
    EditText edtMobile;

    Button btnSignUp;
    String strFullname, strEmail, strPassword, strMessage, strMobile;
    private Validator validator;
    TextView txtLogin;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        pDialog = new ProgressDialog(this);

        edtFullName = (EditText) findViewById(R.id.editText_name_register);
        edtEmail = (EditText) findViewById(R.id.editText_email_register);
        edtPassword = (EditText) findViewById(R.id.editText_password_register);
        edtMobile = (EditText) findViewById(R.id.editText_phoneNo_register);
        btnSignUp = (Button) findViewById(R.id.button_submit);
        txtLogin = (TextView) findViewById(R.id.textView_login_register);

        btnSignUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                validator.validateAsync();
            }
        });

        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onValidationSucceeded() {
        strFullname = edtFullName.getText().toString().replace(" ", "%20");
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();
        strMobile = edtMobile.getText().toString();

        if (JsonUtils.isNetworkAvailable(SignUpActivity.this)) {
            uploadData();
            finish();
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
        strFullname = edtFullName.getText().toString().replace(" ", "%20");
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();
        strMobile = edtMobile.getText().toString();

        Request request = Request.create(Constant.REGISTER_URL);
        request.setMethod("POST")
                .setTimeout(120)
                .setLogger(new Logger(Logger.ERROR))
                .addParameter("name", strFullname)
                .addParameter("email", strEmail)
                .addParameter("password", strPassword)
                .addParameter("phone", strMobile);

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
                        }
                        setResult();
                    }

                    @Override
                    public void onErrorResponse(JSONObject jsonObject) throws JSONException {
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
            showToast(strMessage);

            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();
        }
    }

    public void showToast(String msg) {
        Toast.makeText(SignUpActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    public void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        if (!isDestroyed()) {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }
}
