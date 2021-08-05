package com.example.go_chat;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class Gochat extends AppCompatActivity {

    private TextView status;
    private Button btnConnect;
    private ListView listView;
    private Dialog dialog;
    private TextInputLayout inputLayout;
    private ArrayAdapter<String> chatAdapter;
    private ArrayList<String> chatMessages;
    private BluetoothAdapter bluetoothAdapter;

    //블루투스 서비스에서 핸들러로 보내온 메세지 종류
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private ChatController chatController;
    private BluetoothDevice connectingDevice;
    private ArrayAdapter<String> discoveredDevicesAdapter;

    private final ArrayList<String> dis_device = new ArrayList<>(); //중복 기기 검색 방지를 위한 저장소

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewsByIds(); //xml의 아이디 불러오기

        //블루투스를 지원하는 기기인지 확인
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "블루투스를 지원하지 않는 기기입니다", Toast.LENGTH_SHORT).show();
            finish();
        }

        //블루투스 연결버튼 띄우기
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrinterPickDialog();
                dis_device.clear();
            }
        });

        //대화내용을 출력하기 위한 어뎁터 객체
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, chatMessages) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) { //리스트뷰 텍스트 색상지정
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);
                return view;
            }
        };
        listView.setAdapter(chatAdapter);

        Log.d("test", "유저의 이름은 " + User.getLogin_username());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {//액티비티를 넘어올때 실행
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) { //블루투스 허용할 시
                chatController = new ChatController(this, handler);
            } else {
                Toast.makeText(this, "블루투스를 허용하지 않았습니다 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private final Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE: //메세지 상태가 바뀌었을 때
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            setStatus("Connected to: " + connectingDevice.getName());
                            btnConnect.setEnabled(false);
                            break;
                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting...");
                            btnConnect.setEnabled(false);
                            break;
                        case ChatController.STATE_LISTEN:
                        case ChatController.STATE_NONE:
                            setStatus("Not connected");
                            chatAdapter.clear();
                            chatAdapter.notifyDataSetChanged();
                            btnConnect.setEnabled(true);
                            break;
                    }
                    break;
                case MESSAGE_WRITE: //채팅을 쓸때
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    chatMessages.add(User.getLogin_username() + ":  " + writeMessage);
                    listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL); //채팅을 칠때 자동으로 화면 내려감
                    chatAdapter.notifyDataSetChanged(); // 채팅 데이터 업데이트
                    listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL); //화면 원상복귀
                    break;
                case MESSAGE_READ: //채팅을 받을때
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    chatMessages.add("상대방" + ":  " + readMessage);
                    listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL); //채팅을 받을때 화면 내려감
                    chatAdapter.notifyDataSetChanged(); //채팅 데이터 업데이트
                    listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL); //화면 원상복귀
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    private void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);

        bluetoothAdapter.startDiscovery(); //블루투스 검색 시작

        //찾은 블루투스 기기, 페어링된 기기 표시를 위한 어뎁터 설정
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) { //리스트뷰 텍스트 색상지정
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);
                return view;
            }
        };

        discoveredDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);
                return view;
            }
        };

        //기기들을 출력할 리스트뷰 설정
        ListView listView = dialog.findViewById(R.id.pairedDeviceList);
        ListView listView2 = dialog.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);


        // 디바이스 찾았을 시 방송수신자 등록
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //블루투스 객체선언
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices(); //페어링된 기기의 목록 추가

        if (pairedDevices.size() > 0) { //기기의 페어링된 다른 기기 수
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        dialog.findViewById(R.id.bluetooth_btn).setOnClickListener(new View.OnClickListener() { //자신의 기기 검색버튼을 누를 시
            @Override
            public void onClick(View v) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }
        });

        //Handling listview item click event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { //페어링된 기기 저장 리스트

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                if (pairedDevicesAdapter.getItem(position).equals("none_paired")) {//검색된 기기 없을때 버튼 누를시 튕김 방지
                    Toast.makeText(Gochat.this, "페어링된 기기가 없습니다 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    String info = ((TextView) view).getText().toString(); //부모에게서 값을 받아옴
                    String address = info.substring(info.length() - 17); //문자열 자르기
                    connectToDevice(address);
                }
                dialog.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() { //검색된 기기 저장 리스트
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                if (discoveredDevicesAdapter.getItem(i).equals("none_found")) { //검색된 아이템 없을때 튕김 방지
                    Toast.makeText(Gochat.this, "검색된 기기가 없습니다 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    String info = ((TextView) view).getText().toString(); //부모에게서 값을 받아옴
                    String address = info.substring(info.length() - 17); //문자열 자르기
                    connectToDevice(address);
                }
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothAdapter.cancelDiscovery();
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false); //뒤로가기 X
        dialog.show();
    }

    private void connectToDevice(String deviceAddress) { //블루투스 주소로 연결
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress); //주소 객체를 얻는다
        chatController.connect(device);
    }

    private void setStatus(String s) {
        status.setText(s);
    } //현재 상태 설정


    private void findViewsByIds() {
        status = findViewById(R.id.status);
        btnConnect = findViewById(R.id.btn_connect);
        listView = findViewById(R.id.list);
        inputLayout = findViewById(R.id.input_layout);
        View btnSend = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Objects.requireNonNull(inputLayout.getEditText()).getText().toString().equals("")) { //채팅창이 공백일때
                    Toast.makeText(Gochat.this, "문자를 넣어 주세요", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO: here
                    sendMessage(inputLayout.getEditText().getText().toString());
                    inputLayout.getEditText().setText("");
                }
            }
        });
    }

    private void sendMessage(String message) {
        if (chatController.getState() != ChatController.STATE_CONNECTED) {
            Toast.makeText(this, "전송 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatController.write(send); //채팅 보내기
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            chatController = new ChatController(this, handler);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatController != null)
            chatController.stop();
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { //기기 검색
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action) ) { //기기 발견 시
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED && device.getName() != null) { //페어링된 기기 아닐 시
                    if (!dis_device.contains(device.getName() + "\n" + device.getAddress())) { //동일한 기기 검색 안되도록 설정
                        dis_device.add(device.getName() + "\n" + device.getAddress());
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { //검색종료시 찾은 기기가 없다면
                if (dis_device.size() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
                else{
                    for (String wrt_device : dis_device){
                        discoveredDevicesAdapter.add(wrt_device);
                    }
                }
            }
        }

    };
}