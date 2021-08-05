package com.example.go_chat;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signinActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth; // 파이어베이스 인증 객체 생성
    private DatabaseReference mDatabase; //데이터 추가/조회

    private String gender; //사용자 성별 저장


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);

        // 파이어베이스 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance();
        //firebase 정의
        mDatabase = FirebaseDatabase.getInstance().getReference();

        EditText editText_email = findViewById(R.id.signin_create_id_Edit_text);
        EditText editText_password = findViewById(R.id.signin_create_password_Edit_text);
        EditText editText_name = findViewById(R.id.signin_create_name_Edit_text);
        Button button_create_account = findViewById(R.id.signup_create_account_Button);
        RadioGroup gender_group = findViewById(R.id.signin_gender_Radio_Grop);
        EditText editText_age = findViewById(R.id.signin_age_Edit_Text);
        TextView text2 = findViewById(R.id.text2);

        gender_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { //성별 버튼을 누를 시
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.signin_gender_man) {
                    gender = "남자";
                } else {
                    gender = "여자";
                }
            }
        });

        button_create_account.setOnClickListener(new View.OnClickListener() { //등록 버튼을 누를 시
            @Override
            public void onClick(View v) {
                String getuser_name = editText_name.getText().toString(); //유저 이름 담음
                String  getuser_age = editText_age.getText().toString(); //유저 나이 담음
                String m = editText_email.getText().toString();
                int color = Color.parseColor("#FF000000"); //검정
                int color2 = Color.parseColor("#FFFF0000"); //빨강

                if ( !(editText_email.getText().toString().equals("")) && !(editText_password.getText().toString().equals("")) && !(editText_name.getText().toString().equals("")) && !(editText_age.getText().toString().equals("")) && !(gender == null) &&(android.util.Patterns.EMAIL_ADDRESS.matcher(m).matches()) ) {
                    // 모든 입력란이 공백이 아닐 시
                    User user = new User(getuser_name, getuser_age, gender); //유저 개인정보 저장
                    createUser(editText_email.getText().toString().trim(), editText_password.getText().toString().trim(), user, getuser_name);
                    text2.setText(null);
                } else {
                    if ( !(editText_email.getText().toString().equals("")) && !(editText_password.getText().toString().equals("")) && !(editText_name.getText().toString().equals("")) && !(editText_age.getText().toString().equals("")) && !(gender == null) ) {
                        text2.setText("이메일 형식을 확인해주십시오.");
                        editText_email.getBackground().setColorFilter(color2, PorterDuff.Mode.SRC_ATOP);
                        editText_password.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        editText_name.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        editText_age.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    }
                    else {
                        if ( !(editText_email.getText().toString().equals("")) ) {
                            editText_email.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        }
                        if ( !(editText_password.getText().toString().equals("")) ) {
                            editText_password.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        }
                        if ( !(editText_name.getText().toString().equals("")) ) {
                            editText_name.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        }
                        if ( !(editText_age.getText().toString().equals("")) ) {
                            editText_age.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        }
                        if ( editText_email.getText().toString().equals("")) {
                            editText_email.getBackground().setColorFilter(color2, PorterDuff.Mode.SRC_ATOP);
                        }
                        if ( editText_password.getText().toString().equals("") ) {
                            editText_password.getBackground().setColorFilter(color2, PorterDuff.Mode.SRC_ATOP);
                        }
                        if ( editText_name.getText().toString().equals("") ) {
                            editText_name.getBackground().setColorFilter(color2, PorterDuff.Mode.SRC_ATOP);
                        }
                        if ( editText_age.getText().toString().equals("") ) {
                            editText_age.getBackground().setColorFilter(color2, PorterDuff.Mode.SRC_ATOP);
                        }
                        // 이메일과 비밀번호가 공백인 경우
                        text2.setText("입력란에 공백이 있는지 확인하십시오.");
                    }
                }
            }

        });
    }


    private void createUser(String email, String password, User user, String name) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        ConnectivityManager connectivityManager =(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); //인터넷 연결 감지

                        EditText editText_password = findViewById(R.id.signin_create_password_Edit_text);
                        EditText editText_email = findViewById(R.id.signin_create_id_Edit_text);
                        TextView text2 = findViewById(R.id.text2);
                        String input = editText_password.getText().toString();
                        int num = input.length();
                        int color = Color.parseColor("#FF000000"); //검정
                        int color2 = Color.parseColor("#FFFF0000"); //빨강

                        if (task.isSuccessful()) {
                            // 회원가입 성공시
                            Toast.makeText(signinActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                            String uid = firebaseAuth.getUid();
                            mDatabase.child("users").child("userUID: " + uid).setValue(user); //데이터베이스에 저장
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().
                                    setDisplayName(name).build();
                            user.updateProfile(profileUpdates);
                            finish();
                        } else {
                            if ((num<6) && (!editText_email.getText().toString().equals(""))) { //비밀번호가 6자리 미만이라면
                                text2.setText("비밀번호는 6자 이상이여 합니다.");
                                editText_password.getBackground().setColorFilter(color2, PorterDuff.Mode.SRC_ATOP);
                                editText_email.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                            }
                            else if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())){ //인터넷 X
                                Toast.makeText(signinActivity.this, "인터넷 연결을 확인해 주세요", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                // 계정이 중복된 경우
                                text2.setText("이미 존재하는 계정입니다.");
                                editText_password.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                            }
                        }
                    }
                });
    }
}