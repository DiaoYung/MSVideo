package com.example.lastvideo;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayVideo extends Activity implements OnClickListener
{
	SurfaceView surfaceView;
	ImageButton pause,stop,forward,rewind;
	TextView vtime1,vtime2;
	MediaPlayer moviePlayer=new MediaPlayer();
	SeekBar seekbar;
	int position,movieLength,max,currentItem;
	//子线程停止标志
	private boolean flag;
	private Handler handler;
	public List<String>movieList=new ArrayList<String>();
	String path;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//悬浮标题栏
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.playvideo);
		Intent intent=getIntent();
		Bundle bundle=intent.getExtras();
		path=bundle.getString("Path");
		currentItem=bundle.getInt("key");
		movieList=(ArrayList<String>)bundle.getSerializable("List1");
		handler=new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case 0:
						int a=msg.getData().getInt("1");
						int b=msg.getData().getInt("2");
						set(b,a);
				}
			}
			private void set(int progress,int max)
			{
				vtime1.setText(toTime(progress));
				vtime2.setText(toTime(max));
			}
			private String toTime(int time)
			{
				//时间转换
				StringBuffer stringBuffer=new StringBuffer();
				int s=(time/1000)%60;
				int m=time/60000;
				stringBuffer.append(m).append(":");
				if(s<10)
				{
					stringBuffer.append(0);
				}
				stringBuffer.append(s);
				return stringBuffer.toString();
			}
		};
		//获得按钮对象
		pause=(ImageButton)findViewById(R.id.pause);
		stop=(ImageButton)findViewById(R.id.stop);
		forward=(ImageButton)findViewById(R.id.forward);
		rewind=(ImageButton)findViewById(R.id.rewind);
		vtime1=(TextView)findViewById(R.id.vtime1);
		vtime2=(TextView)findViewById(R.id.vtime2);
		//获取SurfaceView对象
		surfaceView=(SurfaceView)this.findViewById(R.id.surfaceView);
		surfaceView.getHolder().setKeepScreenOn(true);
		//添加回调函数，获取控制SurfaceView对象控制接口
		surfaceView.getHolder().addCallback(new SurfaceListener());
		//为按钮绑定事件监听器
		pause.setOnClickListener(this);
		stop.setOnClickListener(this);
		forward.setOnClickListener(this);
		rewind.setOnClickListener(this);
		//获取进度条对象
		seekbar=(SeekBar)findViewById(R.id.seekBar);
		//为设置监听器
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				if(moviePlayer!=null)
				{
					movieLength=seekBar.getProgress();
					moviePlayer.seekTo(movieLength);
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			//当进度条滑块位置发生改变时触发该方法
			@Override
			public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser)
			{
				seekbar.setProgress(progress);
				movieLength=seekBar.getProgress();
			}
		});
	}
	@Override
	public void onClick(View source)
	{
		switch(source.getId())
		{
			case R.id.pause:
				if(moviePlayer.isPlaying())
				{
					pause.setImageResource(R.drawable.play);
					moviePlayer.pause();
				}else{
					pause.setImageResource(R.drawable.pause);
					moviePlayer.start();
				}
				break;
			case R.id.stop:
				if(moviePlayer.isPlaying())
					moviePlayer.stop();
				break;
			case R.id.forward:
				forwardMovie();
				break;
			case R.id.rewind:
				rewindMovie();
		}
	}
	private void play(final int currentPosition)throws IOException
	{
		//强制播放时横屏
		if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		moviePlayer.reset();
		moviePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		//设置播放视频
		moviePlayer.setDataSource(path);
		setTitle(getFileName(path));
		//把视频画面输出到SurfaceView
		moviePlayer.setDisplay(surfaceView.getHolder());
		moviePlayer.prepare();
		//获取窗口管理器
		WindowManager wManager = getWindowManager();
		DisplayMetrics metrics = new DisplayMetrics();
		//获取屏幕大小
		wManager.getDefaultDisplay().getMetrics(metrics);
		//设置屏幕占满整个屏幕
		surfaceView.setLayoutParams(new LayoutParams(metrics.widthPixels
				, moviePlayer.getVideoHeight() * metrics.widthPixels
				/ moviePlayer.getVideoWidth()));
		moviePlayer.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp)
			{
				moviePlayer.start();
			}
		});
		//将子线程写进准备监听里面，方便在播放时同步显示相关内容
		moviePlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp)
			{

				moviePlayer.start();
				max = moviePlayer.getDuration();
				seekbar.setMax(max);
				moviePlayer.seekTo(currentPosition);
				MyThread myThread=new MyThread();
				myThread.start();
			}
		});
	}
	//获取文件名
	public String getFileName(String pathandname){
		int start=pathandname.lastIndexOf("/");
		int end=pathandname.lastIndexOf(".");
		if (start!=-1 && end!=-1) {
			return pathandname.substring(start+1, end);
		}
		else {
			return null;
		}
	}
	//下一曲操作方法
	public void forwardMovie()
	{
		if(++currentItem>=movieList.size())
		{
			currentItem=0;
		}
		path=movieList.get(currentItem);
		try{
			play(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//上一曲操作方法
	public void rewindMovie()
	{
		if(--currentItem<0)
		{
			currentItem=movieList.size()-1;
		}
		path=movieList.get(currentItem);
		try{
			play(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//通过重写SurfaceHolder.callback回调函数实现控制
	private class SurfaceListener implements SurfaceHolder.Callback
	{
		@Override
		public void surfaceChanged(SurfaceHolder holder,int farmat,int width,int height)
		{
		}
		@Override
		public void surfaceCreated(SurfaceHolder holder)
		{

			try
			{
				play(0);
				moviePlayer.seekTo(position);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
		{
		}
	}
	//重写onPause方法
	@Override
	protected void onPause()
	{
		if(moviePlayer.isPlaying())
		{
			position=moviePlayer.getCurrentPosition();
			moviePlayer.pause();
		}
		super.onPause();
	}
	//重写onDestroy方法
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(moviePlayer.isPlaying())
			moviePlayer.stop();
		flag=false;
		try{
			moviePlayer.release();
			moviePlayer = null;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	class MyThread extends Thread
	{

		@Override
		public void run()
		{
			flag = true;
			while (flag)
			{
				int progress = moviePlayer.getCurrentPosition();
				seekbar.setProgress(progress);
				Message message = new Message();

				Bundle bundle=new Bundle();
				message.setData(bundle);
				bundle.putInt("1", max);
				bundle.putInt("2", progress);
				message.what = 0;
				handler.sendMessage(message);

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

