package com.example.go_chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        Button login_page_create_account_button = findViewById(R.id.login_page_create_account_button); //회원가입 버튼
        EditText login_editText_email = findViewById(R.id.login_page_Edit_text_id); //이메일 에딧 텍스트
        EditText login_editText_password = findViewById(R.id.login_page_Edit_text_password); //비밀번호 에딧 텍스트
        Button Button_login = findViewById(R.id.login_page_login_Button); //로그인 버튼
        CheckBox star = findViewById(R.id.cb); //비밀번호 체크박스
        TextView text1 = findViewById(R.id.text1); //로그인 실패시 메세지
        GradientDrawable fail_password = (GradientDrawable) login_editText_password.getBackground(); //edittext background 그림
        GradientDrawable fail_email = (GradientDrawable) login_editText_email.getBackground();
        int color2 = Color.parseColor("#8BC34A");


        firebaseAuth = FirebaseAuth.getInstance(); // 객체 선언
        ConnectivityManager connectivity_mg = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); //인터넷 연결 감지

        login_page_create_account_button.setOnClickListener(new View.OnClickListener() { //회원가입 버튼 누를 시
            @Override
            public void onClick(View v) { //인터넷 연결 안됬을 시
                if (!(connectivity_mg.getActiveNetworkInfo() != null && connectivity_mg.getActiveNetworkInfo().isConnected())){
                    checkinternet();
                }
                else { //인터넷 연결이 됬을 시
                    Intent signin_intent = new Intent(MainActivity.this, signinActivity.class);
                    fail_password.setStroke(2, color2);
                    fail_email.setStroke(2, color2);
                    text1.setText(null);
                    startActivity(signin_intent); //회원가입 화면으로 이동
                }
            }
        });
        star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { //체크박스 체크시 비밀번호 노출

            public void onCheckedChanged(CompoundButton buttonview, boolean isChecked) {
                if (!isChecked) {
                    login_editText_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                else {
                    login_editText_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        Button_login.setOnClickListener(new View.OnClickListener() { //로그인 버튼 누를 시
            @Override
            public void onClick(View v) { //인터넷 연결 안됬을 시
                if (!(connectivity_mg.getActiveNetworkInfo() != null && connectivity_mg.getActiveNetworkInfo().isConnected())){
                    checkinternet();
                }
                else { //인터넷 연결 됬을 시
                    if (!login_editText_email.getText().toString().equals("") && !login_editText_password.getText().toString().equals("")) {
                        // 이메일과 비밀번호가 공백이 아닌 경우
                        loginUser(login_editText_email.getText().toString().trim(), login_editText_password.getText().toString().trim());
                    } else {
                        // 이메일과 비밀번호가 공백인 경우
                        text1.setText("이메일 또는 비밀번호가 공백입니다.");
                        faillogin();
                    }
                }
            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() { //상태가 바뀔 때
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser(); //유저가 로그인 되었는지 확인
                if (user != null) { //로그인 시
                    User.setUseruid(user.getUid()); //유저 uid 설정
                    User.setLogin_username(user.getDisplayName());
                    Intent intent = new Intent(MainActivity.this, Gochat.class);
                    startActivity(intent); //채팅화면으로 이동
                    finish(); //기존 페이지 삭제
                }
            }
        };
        login_editText_password.addTextChangedListener(new TextWatcher() { //비밀번호 입력시 실시간으로 가능 여부 출력
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                EditText login_editText_email = findViewById(R.id.login_page_Edit_text_id); //이메일 에딧 텍스트
                EditText login_editText_password = findViewById(R.id.login_page_Edit_text_password); //비밀번호 에딧 텍스트
                GradientDrawable fail_password = (GradientDrawable) login_editText_password.getBackground(); //edittext background 그림
                GradientDrawable fail_email = (GradientDrawable) login_editText_email.getBackground();
                int color = Color.parseColor("#ff0000");
                int color2 = Color.parseColor("#8BC34A");
                String input = login_editText_password.getText().toString();
                int num = input.length();
                if ((0 < num) && (num<6)) {
                    text1.setText("비밀번호는 6자 이상이여야 합니다.");
                    fail_password.setStroke(4, color);
                    fail_email.setStroke(2, color2);
                }
                else{
                    text1.setText(null);
                    fail_password.setStroke(2, color2);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password) //데이터베이스에서 이메일 비번의 일치여부 판단
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        TextView text1 = findViewById(R.id.text1); //로그인 실패시 메세지
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                            firebaseAuth.addAuthStateListener(firebaseAuthListener); //로그인
                        } else {
                            // 로그인 실패
                            text1.setText("이메일 또는 비밀번호가 일치하지 않습니다");
                            faillogin();
                        }
                    }
                });
    }
    private void checkinternet(){
        ConnectivityManager connectivity_mg = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); //인터넷 연결 감지
        assert connectivity_mg != null;
        if (!(connectivity_mg.getActiveNetworkInfo() != null && connectivity_mg.getActiveNetworkInfo().isConnected())) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("회원 가입 또는 로그인을 하려면 인터넷을 연결해주세요")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }
    private  void faillogin(){
        EditText login_editText_email = findViewById(R.id.login_page_Edit_text_id); //이메일 에딧 텍스트
        EditText login_editText_password = findViewById(R.id.login_page_Edit_text_password); //비밀번호 에딧 텍스트
        GradientDrawable fail_password = (GradientDrawable) login_editText_password.getBackground(); //edittext background 그림
        GradientDrawable fail_email = (GradientDrawable) login_editText_email.getBackground();
        int color = Color.parseColor("#ff0000");
        fail_email.setStroke(4, color);
        fail_password.setStroke(4, color);
    }

    @Override
    protected void onStop() { //사용자가 활동하지 않을 시 초기화
        super.onStop();
        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener); //listner 해지
        }
    }

}