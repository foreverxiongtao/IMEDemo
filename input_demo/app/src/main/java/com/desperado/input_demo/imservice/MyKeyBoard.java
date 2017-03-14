package com.desperado.input_demo.imservice;

import android.content.Context;
import android.inputmethodservice.Keyboard;


/*
 *
 *
 * 版 权 :@Copyright desperado版权所有
 *
 * 作 者 :desperado
 *
 * 版 本 :1.0
 *
 * 创建日期 :2017/3/10       18:17
 *
 * 描 述 :自定义的输入键盘
 *
 * 修订日期 :
 */
public class MyKeyBoard extends Keyboard {
    public MyKeyBoard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    public MyKeyBoard(Context context, int xmlLayoutResId, int modeId, int width, int height) {
        super(context, xmlLayoutResId, modeId, width, height);
    }

    public MyKeyBoard(Context context, int xmlLayoutResId, int modeId) {
        super(context, xmlLayoutResId, modeId);
    }
}
