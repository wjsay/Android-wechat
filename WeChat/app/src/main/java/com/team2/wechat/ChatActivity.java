package com.team2.wechat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.team2.wechat.bean.ChatMessage;
import com.team2.wechat.bean.ChatMessage.Type;
import com.team2.wechat.util.NetUtils;

public class ChatActivity extends Activity {

	private TextView tv_header;
	private ListView lv_body;
	private LinearLayout ll_footer;
	private EditText et_msg;
	private Button btn_send;
	private ServerSocket serverSocket;
	private List<ChatMessage> msgList = new ArrayList<ChatMessage>();
	private BodyAdapter bodyAdapter;
	private Thread serverThread;

	// 10.0.2.15
	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		findView();
		initInterface();

		bodyAdapter = new BodyAdapter();
		lv_body.setAdapter(bodyAdapter);
		et_msg.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				if (s.length() == 0) {
					btn_send.setBackgroundResource(R.drawable.send_btn_normal);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
				btn_send.setBackgroundResource(R.drawable.send_btn_pressed);
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		//TODO
		//System.out.println(NetUtils.getIPAddress(this));

		serverThread = new Thread() {

			public void run() {
				try {

					serverSocket = new ServerSocket(6000);
					while (true) {

						Socket sServer = serverSocket.accept();
						ObjectInputStream ois = new ObjectInputStream(
								sServer.getInputStream());
						String receiveMsgContent = (String) ois.readObject();
						final ChatMessage receiveMessage = new ChatMessage();
						receiveMessage.setContent(receiveMsgContent);
						receiveMessage.setType(Type.RECEIVE);
						receiveMessage.setDate(new Date());
						sServer.close();
						runOnUiThread(new Runnable() {
							public void run() {
								bodyAdapter.add(receiveMessage);
								lv_body.setSelection(bodyAdapter.getCount() - 1);
							}
						});
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		serverThread.setDaemon(true);
		serverThread.start();

	}

	@Override
	protected void onDestroy() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
	}

	/**
	 * 点击发送按钮触发的事件
	 *
	 * @param v
	 */
	public void onSendClick(View v) {

		final String sendMsgContent = et_msg.getText().toString();
		if (TextUtils.isEmpty(sendMsgContent)) {
			return;
		}

		ChatMessage sendMessage = new ChatMessage();
		sendMessage.setType(Type.SEND);
		sendMessage.setDate(new Date());
		sendMessage.setContent(sendMsgContent);
		bodyAdapter.add(sendMessage);
		lv_body.setSelection(bodyAdapter.getCount() - 1);
		et_msg.setText("");

		new Thread() {
			public void run() {
				try {
					Socket cServer = new Socket("172.16.0.186", 6000);
					ObjectOutputStream oos = new ObjectOutputStream(
							cServer.getOutputStream());

					oos.writeObject(sendMsgContent);
					oos.flush();
					cServer.close();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}.start();

	}

	private void findView() {

		tv_header = (TextView) findViewById(R.id.tv_header);
		lv_body = (ListView) findViewById(R.id.lv_body);
		ll_footer = (LinearLayout) findViewById(R.id.ll_footer);
		et_msg = (EditText) findViewById(R.id.et_msg);
		btn_send = (Button) findViewById(R.id.btn_send);
	}

	private void initInterface() {
		Intent intent = getIntent();

		// 设置联系人名字
		String contactName = intent.getStringExtra("contactName");
		tv_header.setText(contactName);

		// TODO
		// 设置联系人头像

		// TODO
		// 初始化已有消息

		//TODO
		//得到对方的IP地址，注：需要在打开主页面的时候，开启一个服务，然后

	}

	public class BodyAdapter extends BaseAdapter {

		public void add(ChatMessage msg) {
			msgList.add(msg);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return msgList.size();
		}

		@Override
		public ChatMessage getItem(int position) {
			return msgList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			if (msgList.get(position).getType() == Type.SEND) {
				return 1;
			}
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ChatMessage chatMessage = getItem(position);
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();
				if (getItemViewType(position) == 1) {
					convertView = View.inflate(ChatActivity.this,
							R.layout.item_send_msg, null);
					holder.date = (TextView) convertView
							.findViewById(R.id.tv_send_msg_date);
					holder.text = (TextView) convertView
							.findViewById(R.id.tv_send_msg_text);
				} else {
					convertView = View.inflate(ChatActivity.this,
							R.layout.item_receive_msg, null);
					holder.date = (TextView) convertView
							.findViewById(R.id.tv_receive_msg_date);
					holder.text = (TextView) convertView
							.findViewById(R.id.tv_receive_msg_text);
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.CHINA);
			// 设置图标
			// TODO
			holder.date.setText(df.format(chatMessage.getDate()));
			holder.text.setText(chatMessage.getContent());

			return convertView;
		}

		private final class ViewHolder {
			TextView date;
			TextView text;
		}

	}

}
