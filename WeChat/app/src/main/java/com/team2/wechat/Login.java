package com.team2.wechat;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.team2.wechat.myservice.LoginService;
import com.team2.wechat.utils.ActivityCollector;
import com.team2.wechat.utils.Net;
import com.team2.wechat.utils.UserTools;

import java.util.HashMap;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    LinearLayout mobile_login_hint_layout;
    ImageView iv_return;
    ImageView iv_menu;
    Button login;
    EditText phonenumber;
    EditText passwd;
    boolean passwdcorrect = false;
    boolean inner;
    String inputPasswd;
    ProgressDialog progressDialog;
    AlertDialog.Builder alertDialog;
    LoginReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //活动入栈
        ActivityCollector.addActivity(this);
        //Log.d(TAG, " debug01");
         mobile_login_hint_layout = (LinearLayout)findViewById(R.id.mobile_login_hint);
        iv_return = (ImageView)findViewById(R.id.returnButton);
        iv_menu = (ImageView)findViewById(R.id.menuButton);
        login = (Button)findViewById(R.id.userlogin);
        phonenumber = (EditText)findViewById(R.id.PhoneNumber);
        passwd = (EditText)findViewById(R.id.password);
        iv_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        phonenumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mobile_login_hint_layout != null && mobile_login_hint_layout.getVisibility() == View.VISIBLE)
                    mobile_login_hint_layout.setVisibility(View.GONE);
            }
        });
        phonenumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(!TextUtils.isEmpty(phonenumber.getText().toString().trim())
                        && !TextUtils.isEmpty(passwd.getText().toString().trim())){
                    login.setBackgroundColor(getResources().getColor(R.color.button_color_green));
                    login.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(TextUtils.isEmpty(phonenumber.getText().toString().trim())) {
                    login.setBackgroundColor(Color.parseColor("#A2E08D"));
                    login.setEnabled(false);
                }
            }
        });
        passwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b && mobile_login_hint_layout != null && mobile_login_hint_layout.getVisibility() == View.VISIBLE) {
                        mobile_login_hint_layout.setVisibility(View.GONE);
                    //Log.d(TAG, " debug01");
                }
            }
        });
        passwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        passwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(phonenumber.getText().toString().trim())
                        && !TextUtils.isEmpty(passwd.getText().toString().trim())){
                    login.setBackgroundColor(getResources().getColor(R.color.button_color_green));
                    login.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(TextUtils.isEmpty(passwd.getText().toString().trim())) {
                    login.setBackgroundColor(Color.parseColor("#A2E08D"));
                    login.setEnabled(false);
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String PhoneNumber = phonenumber.getText().toString().trim();
                inputPasswd = passwd.getText().toString().trim();
                HashMap<String, Object> map = new HashMap<>();
                map.put("password", inputPasswd);
                map.put("telephone", PhoneNumber);
                UserTools.writeUserInfo(Login.this, map);
                Log.d(TAG, " 点击了登录按钮" + " 电话号：" + PhoneNumber + " 密码：" + inputPasswd);
                if(!Net.isNetworkConnected(Login.this)) {
                    Toast.makeText(Login.this, "网络不可用", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, " 网络可用");
                Intent intent = new Intent(Login.this, LoginService.class);
                intent.putExtra("telephone", PhoneNumber);
                intent.putExtra("inputPasswd", inputPasswd);

                progressDialog = new ProgressDialog(Login.this);
                progressDialog.setMessage("登录中……");
                progressDialog.setCancelable(false);
                //进度条对话框应为灰色
                Log.d(TAG, " 进度条显示前");
                progressDialog.show();
                Log.d(TAG, " 进度条显示后");

                startService(intent);
                Log.d(TAG, " 服务启动了吗？");

                //注册广播接收器
                receiver=new LoginReceiver();
                IntentFilter filter= new IntentFilter();
                filter.addAction("com.team2.wechat.internet.LoginService");
                Login.this.registerReceiver(receiver, filter);
            }
        });
    }

    /**
     * 获取广播数据（验证结果）
     */
    class LoginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            boolean passwordCorrect = bundle.getBoolean("passwordCorrect");
            progressDialog.dismiss();
            if(passwordCorrect) {
                Intent intent2app = new Intent(Login.this, MainActivity.class);
                intent2app.putExtra("phone_no", phonenumber.getText().toString().trim());
                startActivity(intent2app);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                ActivityCollector.finishAll();
            }
            else {
                alertDialog = new AlertDialog.Builder(Login.this);
                alertDialog.setMessage("账号/密码作物或账号密码组合错误。详情请查看帮助");
                alertDialog.setPositiveButton("帮助", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, " 帮助");

                    }
                });

                alertDialog.setNegativeButton("重试", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, " 重试");
                    }
                });
                alertDialog.show();
                unregisterReceiver(receiver);
            }
        }
    }

}
