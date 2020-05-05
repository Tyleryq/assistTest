package com.example.assisttest;

import android.content.Context;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import java.io.Serializable;

public class MyImageView extends AppCompatImageView implements Serializable {
    public MyImageView(Context context) {
        super(context);
    }
}
