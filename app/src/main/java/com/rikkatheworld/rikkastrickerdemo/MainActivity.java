package com.rikkatheworld.rikkastrickerdemo;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.rikkatheworld.rikkastrickerdemo.stricker.RikkaStickerLayout;
import com.rikkatheworld.rikkastrickerdemo.stricker.RikkaStickerView;

public class MainActivity extends AppCompatActivity {

    private RikkaStickerLayout stickerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stickerLayout = findViewById(R.id.sticker_layout);

        findViewById(R.id.iv_shield).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RikkaStickerView sticker = new RikkaStickerView(MainActivity.this, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.mipmap.shield));
                stickerLayout.addSticker(sticker);
            }
        });

        findViewById(R.id.iv_files).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RikkaStickerView sticker = new RikkaStickerView(MainActivity.this, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.mipmap.files));
                stickerLayout.addSticker(sticker);
            }
        });
        findViewById(R.id.iv_calendar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RikkaStickerView sticker = new RikkaStickerView(MainActivity.this, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.mipmap.calendar));
                stickerLayout.addSticker(sticker);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stickerLayout.removeAllSticker();
    }
}
