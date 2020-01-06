package com.mass.javacvffmpopencv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainObservableActivity extends AppCompatActivity {

    private final String TAG = "MainObservableActivity";
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_observable);
        tv=findViewById(R.id.tvtext);

        Observable observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "Observable =========================currentThread name: " + Thread.currentThread().getName());
                emitter.onNext(1);
                Thread.sleep(10000);
                emitter.onNext(2);
                Thread.sleep(10000);
                emitter.onNext(3);
                Thread.sleep(10000);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());

        Observer<Integer> observer=new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "observer========================= currentThread name: " + Thread.currentThread().getName());
            }

            @Override
            public void onNext(Integer integer) {
                tv.setText(integer+"");
                Log.d(TAG, "======================onNext " + integer);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "======================onError"+e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "======================onComplete");
            }


        };
        observable.subscribe(observer);
    }
}
