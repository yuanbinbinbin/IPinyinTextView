package com.example.pinyindemo;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.yb.lib.pinyin.PinYinTextView;

public class MainActivity extends Activity {

    PinYinTextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = (PinYinTextView) findViewById(R.id.pinyin_tv);
    }

    //region 显示格式
    public void fillWord(View v) {
        mTv.setData("传说，在很久很久以前，有一种叫“年”的怪兽。它长得可凶啦！\n头上长着长长的角，嘴里龇着尖尖的牙，发起怒来，就会张开血盆大口，哇哇大叫。",
                "chuán shuō ， zài hěn jiǔ hěn jiǔ yǐ qián ， yǒu yī zhǒng jiào “ nián ” de guài shòu 。 tā cháng dé kě xiōng lā ！ \n tóu shàng cháng zhuó cháng cháng de jiǎo ， zuǐ lǐ zī zhuó jiān jiān de yá ， fā qǐ nù lái ， jiù huì zhāng kāi xiě pén dà kǒu ， wā wā dà jiào 。");
    }

    public void showHanziPinyin(View v) {
        mTv.setDrawType(PinYinTextView.PIN_YIN_TYPE.TYPE_PINYIN_AND_TEXT);
    }

    public void showHanzi(View view) {
        mTv.setDrawType(PinYinTextView.PIN_YIN_TYPE.TYPE_PLAIN_TEXT);
    }

    public void showPinyin(View view) {
        mTv.setDrawType(PinYinTextView.PIN_YIN_TYPE.TYPE_PINYIN);
    }
    //endregion

    //region 文本大小
    public void hanziAdd(View view) {
        mTv.setTextSize(mTv.getTextSize() + 5);
    }

    public void hanziSub(View view) {
        mTv.setTextSize(mTv.getTextSize() - 5);
    }

    public void pinyinAdd(View view) {
        mTv.setPinyinTextSize(mTv.getPinyinTextSize() + 5);
    }

    public void pinyinSub(View view) {
        mTv.setPinyinTextSize(mTv.getPinyinTextSize() - 5);
    }
    //endregion

    //region 间距
    public void hanziHAdd(View view) {
        mTv.setHorizontalSpacing(mTv.getHorizontalSpacing() + 5);
    }

    public void hanziHSub(View view) {
        mTv.setHorizontalSpacing(mTv.getHorizontalSpacing() - 5);
    }

    public void hanziVAdd(View view) {
        mTv.setVerticalSpacing(mTv.getVerticalSpacing() + 5);
    }

    public void hanziVSub(View view) {
        mTv.setVerticalSpacing(mTv.getVerticalSpacing() - 5);
    }

    public void HanZiPinyinAdd(View v) {
        mTv.setPinyinTextSpacing(mTv.getPinyinTextSpacing() + 5);
    }

    public void HanZiPinyinSub(View v) {
        mTv.setPinyinTextSpacing(mTv.getPinyinTextSpacing() - 5);
    }
    //endregion

    //region下划线
    public void showUnderLine(View v) {
        mTv.setUnderline(true);
    }

    public void hideUnderLine(View view) {
        mTv.setUnderline(false);
    }

    public void underLineSpaceAdd(View view) {
        mTv.setUnderlineSpacing(mTv.getUnderlineSpacing() + 5);
    }

    public void underLineSpaceSub(View view) {
        mTv.setUnderlineSpacing(mTv.getUnderlineSpacing() - 5);
    }

    public void underLineColor(View view) {
        mTv.setUnderlineColor(Color.RED);
    }

    public void underLineWidthAdd(View view) {
        mTv.setUnderlineWidth(mTv.getUnderlineWidth() + 2);
    }

    public void underLineWidthSub(View view) {
        mTv.setUnderlineWidth(mTv.getUnderlineWidth() - 2);
    }

    public void underLineSolid(View view) {
        mTv.setUnderlineSolid(true);
    }

    public void underLineDotted(View view) {
        mTv.setUnderlineSolid(false);
    }
    //endregion

    //region 文本颜色
    public void specialTextColor(View view) {
        mTv.setTextColor(0, Color.GREEN);
    }

    public void specialPinyinColor(View view) {
        mTv.setPinyinColor(0, Color.YELLOW);
    }

    public void normalTextColor(View view) {
        mTv.setTextColor(Color.RED);
    }

    public void normalPinyinColor(View view) {
        mTv.setPinyinColor(Color.MAGENTA);
    }

    public void allTextColor(View view) {
        mTv.setAllTextColor(Color.parseColor("#abce80"));
    }

    public void allPinyinColor(View view) {
        mTv.setAllPinyinColor(Color.parseColor("#c9b2a2"));
    }

    //endregion

    //region 首行缩进
    public void textIndentAdd(View view) {
        mTv.setTextIndent(mTv.getTextIndent() + 1);
    }

    public void textIndentSub(View view) {
        mTv.setTextIndent(mTv.getTextIndent() - 1);
    }
    //endregion
}
