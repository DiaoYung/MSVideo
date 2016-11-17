package com.example.lastvideo;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

public class VideoList extends Activity {
	public final List<String> itemList=new ArrayList<String>();
	public final List<String> itemList1=new ArrayList<String>();
	public static int currentItem=0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		ImageButton open=(ImageButton)findViewById(R.id.open);
		open.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//启动文件浏览界面
				Intent intent1=new Intent(VideoList.this,SDFileExplorer.class);
				startActivity(intent1);
			}
		});
		videoList();
	}
	public void videoList()
	{
		getFiles(Environment.getExternalStorageDirectory().getPath());
		ArrayAdapter<String>adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,itemList1);
		ListView listview=(ListView)findViewById(R.id.list);
		listview.setAdapter(adapter);
		//对选中项做出反应
		listview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?>listView,View view,int position,long id)
			{
				currentItem=position;
				//创建Intent对象
				Intent intent=new Intent(VideoList.this,PlayVideo.class);
				//创建bundle对象，将数据传输到播放界面中
				Bundle bundle=new Bundle();
				bundle.putCharSequence("Path",itemList.get(currentItem));
				bundle.putInt("key", currentItem);
				bundle.putSerializable("List1", (Serializable) itemList);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	//设置相关格式
	private static String[] imageFormatSet=new String[]{"mp4","MP4"};
	//判定文件是否为视频文件
	private static boolean isMovieFile(String path)
	{
		for(String format:imageFormatSet)
		{
			if(path.contains(format))
			{
				return true;
			}
		}
		return false;
	}
	//生成列表
	private void getFiles(String url)
	{
		File files=new File(url);
		File[] file=files.listFiles();
		try{
			for(File f:file)
			{
				if(f.isDirectory())
				{
					getFiles(f.getAbsolutePath());
				}
				else
				{
					if(isMovieFile(f.getPath()))
					{
						itemList.add(f.getPath());
						itemList1.add(f.getName());
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

}

