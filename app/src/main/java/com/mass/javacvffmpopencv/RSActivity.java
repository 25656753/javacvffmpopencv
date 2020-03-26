package com.mass.javacvffmpopencv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;


public class RSActivity extends AppCompatActivity {


private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rs);
        lv=findViewById(R.id.rslist);
      Intent intent= getIntent();
        List<segdata> data=intent.getParcelableArrayListExtra("data");
        Log.i("fff",data.size()+"");
      /*  ArrayAdapter<segdata>   adapter = new ArrayAdapter<segdata>(this, R.layout.listlayout,
                data);
*/
        UserAdapter adapter=new UserAdapter(this, R.layout.listlayout,
                data);


        lv.setAdapter(adapter);
    }


    class UserAdapter extends ArrayAdapter<segdata> {

        private int mResourceId;

        public UserAdapter(Context context, int textViewResourceId,
                           List<segdata> datas ) {
            super(context, textViewResourceId, datas);
            this.mResourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            segdata data = getItem(position);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(mResourceId, null);

            TextView text = (TextView) view.findViewById(R.id.listtv);
            text.setText(data.toString());
            return view;
        }
    }
}
