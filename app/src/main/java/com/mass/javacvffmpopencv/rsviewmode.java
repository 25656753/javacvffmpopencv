package com.mass.javacvffmpopencv;

import java.util.Vector;

import androidx.lifecycle.ViewModel;

public class rsviewmode extends ViewModel {
    public Vector<Vector<Integer>> getMrs() {
        return mrs;
    }

    public void setMrs(Vector<Vector<Integer>> mrs) {
        this.mrs = mrs;
    }

    private  Vector<Vector<Integer>>  mrs;
}
