package org.pytorch.helloworld;

import org.opencv.core.Mat;

import java.io.Serializable;
import java.util.Vector;

public class mMat implements Serializable {
    private Vector<Mat> matVector;

    public Vector<Mat> getMatVector() {
        return matVector;
    }

    public void setMatVector(Vector<Mat> matVector) {
        this.matVector = matVector;
    }
}
