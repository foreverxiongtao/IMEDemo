package com.desperado.input_demo.imservice;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.desperado.input_demo.R;

import java.util.ArrayList;
import java.util.List;

/*
 *
 *
 * 版 权 :@Copyright desperado版权所有
 *
 * 作 者 :desperado
 *
 * 版 本 :1.0
 *
 * 创建日期 :2017/3/10       14:42
 *
 * 描 述 :自定义的IME组件
 *
 * 修订日期 :
 */
public class CustomIMService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    //注意：輸入法界面（軟鍵盤）並不許要我們自己建立Activity,這個Activity是由系統提供的，
    // 而我們只需要在Activity上顯示的View對象即可，也就是onCreateInputView方法的返回值。
    //自定义的键盘控件
    private MyKeyBoard mKeyBoard;
    //自定义的数字键盘
    private MyKeyBoard mNumberKeyBoard;
    //自定义的shift
    private MyKeyBoard mSymbelKeyBoard;
    //字符串容器
    private StringBuilder mComposing = new StringBuilder();
    //候选区控件
    private CandidateView mCandidateView;
    private boolean mCompletionOn;
    private CompletionInfo[] mCompletions;
    private KeyboardView mkeyboardView;
    private boolean mCapsLock;
    private long mLastShiftTime;


    /***
     * 1.输入法的初始化操作
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /***
     * 2.该方法在onCreate方法后调用，主要做一些ui的初始化
     */
    @Override
    public void onInitializeInterface() {
        super.onInitializeInterface();
        if (mKeyBoard == null) {
            mKeyBoard = new MyKeyBoard(this, R.xml.qwerty);
            mNumberKeyBoard=new MyKeyBoard(this,R.xml.symbols);
            mSymbelKeyBoard=new MyKeyBoard(this,R.xml.symbols_shift);
        }
    }

    /***
     * 3.创建了软键盘布局(输入框)
     *
     * @return
     */
    @Override
    public View onCreateInputView() {
        mkeyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        mkeyboardView.setKeyboard(mKeyBoard);
        mkeyboardView.setOnKeyboardActionListener(this);
        Log.i("root", "CustomIMService has called onCreateInputView");
        return mkeyboardView;
    }

    @Override
    public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        return mCandidateView;
    }

    /***
     * 4.创建候选框
     * 开始输入
     *
     * @param attribute
     * @param restarting
     */

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        mComposing.setLength(0);
        updateCandidates();
        mCompletionOn = false;
        mCompletions = null;
    }

    @Override
    public void onPress(int primaryCode) {

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Log.i("onKey:", (char) primaryCode + "");
        handleCharacter(primaryCode, keyCodes);

    }

    @Override
    public void onText(CharSequence text) {
        Log.i("onText", text + "");
    }

    /***
     * 处理字符
     *
     * @param primaryCode
     * @param keyCodes
     */
    private void handleCharacter(int primaryCode, int[] keyCodes) {
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE: //删除字符
                handleDeleteBehaviour(primaryCode);
                break;
            case Keyboard.KEYCODE_CANCEL:  //键盘消失
                handleClose();
                break;
            case Keyboard.KEYCODE_SHIFT:
                handleShift();
                break;
            case Keyboard.KEYCODE_MODE_CHANGE:
                handleModelChange(primaryCode);
                break;
            default:
                InputConnection currentInputConnection = getCurrentInputConnection();
                if (currentInputConnection != null) {
                    /***如果是输入框打开**/
                    if (isInputViewShown()) {
                        if (mkeyboardView.isShifted()) {//shift键已经打开
                            primaryCode = Character.toUpperCase(primaryCode);
                        }
                    }
                    mComposing.append((char) primaryCode);
                    currentInputConnection.setComposingText(mComposing, 1);
                    setCandidatesViewShown(true);
                }
                updateCandidates();
                break;
        }
    }

    private void handleDeleteBehaviour(int keyEventCode) {
        if (mComposing.length() > 1) {
            mComposing.delete(mComposing.length() - 1, mComposing.length());
            getCurrentInputConnection().setComposingText(mComposing, 1);
        } else if (mComposing.length() > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().setComposingText("", 0);
        } else { //用户点击删除按键
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateCandidates();

    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }


    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }


    @Override
    public void onFinishInput() {
        super.onFinishInput();
        mComposing.setLength(0);
        updateCandidates();

        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);
        Log.i("onFinishInput", "onFinishInput has been called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "onDestroy has been called");
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        Log.i("onStartInputView","onStartInputView");
    }

    /***
     * 更新候选区的词汇
     */
    private void updateCandidates() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }


    /***
     * 设置候选区建议词汇
     *
     * @param suggestions
     * @param completions
     * @param typedWordValid
     */
    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }


    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mkeyboardView != null && mKeyBoard == mkeyboardView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mkeyboardView.setShifted(mCapsLock || caps != 0);
        }
    }


    /***
     * 手动选择候选框中的文字
     *
     * @param index
     */
    public void pickSuggestionManually(int index) {
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {
            // If we were generating candidate suggestions for the current
            // text, we would commit one of them here.  But for this sample,
            // we will just commit the current text.
            commitTyped(getCurrentInputConnection());
        }
    }


    /***
     * 关闭键盘
     */
    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mkeyboardView.closing();
    }


    /**
     * 提交文本给客户端的InputManager
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing,mComposing.length());
            mComposing.delete(0,mComposing.length());
            updateCandidates();
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }

            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < completions.length; i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }


    /***
     * 处理shift键
     */
    private void handleShift() {
        if (mkeyboardView == null) {
            return;
        }

        Keyboard currentKeyboard = mkeyboardView.getKeyboard();
        if (mKeyBoard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mkeyboardView.setShifted(mCapsLock || !mkeyboardView.isShifted());
        }
        else if(currentKeyboard==mNumberKeyBoard){
            currentKeyboard=mSymbelKeyBoard;
            mkeyboardView.setKeyboard(currentKeyboard);
            mkeyboardView.setShifted(true);
        }
        else if(currentKeyboard==mSymbelKeyBoard){
            currentKeyboard=mNumberKeyBoard;
            mkeyboardView.setKeyboard(currentKeyboard);
            mkeyboardView.setShifted(false);
        }
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mCapsLock=!mCapsLock;
            mLastShiftTime = now;
        }
    }


    /**
     * 数字键盘
     */
    private void handleModelChange(int keyCode){
        Keyboard currentKeyboard = mkeyboardView.getKeyboard();
        currentKeyboard.setShifted(false);
        if(currentKeyboard==mNumberKeyBoard){
            currentKeyboard=mKeyBoard;
        }
        else if(currentKeyboard==mKeyBoard){
            currentKeyboard=mNumberKeyBoard;
        }
        mkeyboardView.setKeyboard(currentKeyboard);
    }
}
