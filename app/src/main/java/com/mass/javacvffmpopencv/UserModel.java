package com.mass.javacvffmpopencv;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserModel extends ViewModel {
    public final MutableLiveData<User> mUserLiveData=new MutableLiveData<>();

    public UserModel() {
        mUserLiveData.postValue(new User(23,"jam"));
    }
    //模拟 进行一些数据骚操作
    public void doSomething() {
        User user = mUserLiveData.getValue();
        if (user != null) {
            user.age = 15;
            user.name = "name15";
            mUserLiveData.setValue(user);
        }
    }
}
