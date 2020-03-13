package com.yb.lib.pinyin;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PinYinTextView extends View {

    public static enum PIN_YIN_TYPE {
        TYPE_PLAIN_TEXT, TYPE_PINYIN_AND_TEXT, TYPE_PINYIN
    }

    private static final PIN_YIN_TYPE DEFAULT_DRAW_TYPE = PIN_YIN_TYPE.TYPE_PLAIN_TEXT;
    private static final float DEFAULT_PINYIN_TEST_SIZE_RADIO = 0.5f;

    private PIN_YIN_TYPE mDrawType = DEFAULT_DRAW_TYPE;

    //汉字大小
    private float mTextSize;
    //拼音大小
    private float mPinyinTextSize;
    //拼音大小是汉字的多少倍,除非用户手动设置大小,否则按此比率设置拼音大小
    private float mPinyinTextRadio;
    private int mTextColor;
    private int mPinyinColor;

    //横向间隔
    private int mHorizontalSpacing;
    //竖向间隔
    private int mVerticalSpacing;
    //汉字与拼音间隔
    private int mPinyinTextSpacing;
    private Spanned mDrawSpanned;
    //是否显示下划线
    private boolean mUnderline = false;

    private int mUnderlineColor;
    private int mUnderlineWidth;
    private int mUnderlineSpacing;
    private boolean mUnderlineSolid;
    private Paint mUnderlinePaint = new Paint();

    //首行缩进
    private int mTextIndent;
    private int mTextIndentWidth;
    //数据
    private List<PinyinCompat> mDataCompat = new ArrayList<>();
    private List<PinyinBean> mData = new ArrayList<>();
    private String mText;
    private String mPinyin;
    private String mColorText;
    private String mColorPinyin;
    private TextPaint mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);

    //汉字高度
    private int mTextHeight;
    //拼音高度
    private int mPinyinHeight;

    private Rect mBounds = new Rect();

    private StaticLayout mStaticLayout;

    private boolean debugDraw = false; //  for debug, set false when release
    private Paint mDebugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PinYinTextView(Context context) {
        this(context, null);
    }

    public PinYinTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinYinTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefault();
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PinyinTextView);
        if (a.hasValue(R.styleable.PinyinTextView_textSize)) {
            mTextSize = a.getDimensionPixelSize(R.styleable.PinyinTextView_textSize, (int) mTextSize);
        }
        if (a.hasValue(R.styleable.PinyinTextView_pinyinTextRatio)) {
            mPinyinTextRadio = a.getFloat(R.styleable.PinyinTextView_pinyinTextRatio, mPinyinTextRadio);
        }
        if (a.hasValue(R.styleable.PinyinTextView_pinyinTextSize)) {
            mPinyinTextSize = a.getDimensionPixelSize(R.styleable.PinyinTextView_pinyinTextSize, (int) mPinyinTextSize);
        } else {
            mPinyinTextSize = mTextSize * mPinyinTextRadio;
        }
        if (a.hasValue(R.styleable.PinyinTextView_textColor)) {
            mTextColor = a.getColor(R.styleable.PinyinTextView_textColor, mTextColor);
        }
        if (a.hasValue(R.styleable.PinyinTextView_pinyinColor)) {
            mPinyinColor = a.getColor(R.styleable.PinyinTextView_pinyinColor, mPinyinColor);
        } else {
            mPinyinColor = mTextColor;
        }
        if (a.hasValue(R.styleable.PinyinTextView_horizontalSpace)) {
            mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.PinyinTextView_horizontalSpace, mHorizontalSpacing);
        }
        if (a.hasValue(R.styleable.PinyinTextView_verticalSpace)) {
            mVerticalSpacing = a.getDimensionPixelSize(R.styleable.PinyinTextView_verticalSpace, mVerticalSpacing);
        }
        if (a.hasValue(R.styleable.PinyinTextView_pinyinTextSpace)) {
            mPinyinTextSpacing = a.getDimensionPixelSize(R.styleable.PinyinTextView_pinyinTextSpace, mPinyinTextSpacing);
        }
        if (a.hasValue(R.styleable.PinyinTextView_underLine)) {
            mUnderline = a.getBoolean(R.styleable.PinyinTextView_underLine, mUnderline);
        }
        if (a.hasValue(R.styleable.PinyinTextView_underLineColor)) {
            mUnderlineColor = a.getColor(R.styleable.PinyinTextView_underLineColor, mUnderlineColor);
            mUnderlinePaint.setColor(mUnderlineColor);
        }
        if (a.hasValue(R.styleable.PinyinTextView_underLineWidth)) {
            mUnderlineWidth = a.getDimensionPixelSize(R.styleable.PinyinTextView_underLineWidth, mUnderlineWidth);
            mUnderlinePaint.setStrokeWidth(mUnderlineWidth);
        }
        if (a.hasValue(R.styleable.PinyinTextView_underLineSolid)) {
            mUnderlineSolid = a.getBoolean(R.styleable.PinyinTextView_underLineSolid, mUnderlineSolid);
            if (mUnderlineSolid) {
                mUnderlinePaint.setPathEffect(null);
            } else {
                mUnderlinePaint.setPathEffect(new DashPathEffect(new float[]{mUnderlineWidth, mUnderlineWidth, mUnderlineWidth, mUnderlineWidth}, 0));
            }
        }
        if (a.hasValue(R.styleable.PinyinTextView_underLineVerticalSpace)) {
            mUnderlineSpacing = a.getDimensionPixelSize(R.styleable.PinyinTextView_underLineVerticalSpace, mUnderlineSpacing);
        }
        if (a.hasValue(R.styleable.PinyinTextView_textIndent)) {
            mTextIndent = a.getInt(R.styleable.PinyinTextView_textIndent, mTextIndent);
        }
        a.recycle();
    }

    private void initDefault() {
        Context c = getContext();
        Resources r;

        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }

        DisplayMetrics dm = r.getDisplayMetrics();

        // Text size default 14sp
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, dm);
        mPinyinTextRadio = DEFAULT_PINYIN_TEST_SIZE_RADIO;
        mPinyinTextSize = mTextSize * mPinyinTextRadio;

        // spacing
        mHorizontalSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm);
        mVerticalSpacing = mHorizontalSpacing * 2;
        mPinyinTextSpacing = mHorizontalSpacing / 2;

        // set default text color
        mTextColor = 0xff333333;
        mPinyinColor = 0xff333333;

        mPaint.setStyle(Paint.Style.FILL);

        mDebugPaint.setStyle(Paint.Style.STROKE);

        mUnderline = false;
        mUnderlineColor = 0xff333333;
        mUnderlinePaint.setColor(mUnderlineColor);
        mUnderlinePaint.setStyle(Paint.Style.STROKE);
        mUnderlineSolid = true;
//        mUnderlinePaint.setPathEffect(new DashPathEffect(new float[]{2, 2, 2, 2}, 0));
        mUnderlineSpacing = mHorizontalSpacing;
        mUnderlineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm);
        mUnderlinePaint.setStrokeWidth(mUnderlineWidth);
        mTextIndent = 0;
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    //region 公开api

    public PIN_YIN_TYPE getDrawType() {
        return mDrawType;
    }

    public PinYinTextView setDrawType(PIN_YIN_TYPE mDrawType) {
        if (this.mDrawType == mDrawType) {
            return this;
        }
        this.mDrawType = mDrawType;
        requestLayout();
        invalidate();
        return this;
    }

    //region textSize
    public float getTextSize() {
        return mTextSize;
    }

    public PinYinTextView setTextSize(float mTextSize) {
        if (this.mTextSize == mTextSize || mTextSize <= 0) {
            return this;
        }
        this.mTextSize = mTextSize;
        setPinyinTextSize(mTextSize * mPinyinTextRadio);
        return this;
    }

    public float getPinyinTextSize() {
        return mPinyinTextSize;
    }

    public PinYinTextView setPinyinTextSize(float mPinyinTextSize) {
        if (this.mPinyinTextSize == mPinyinTextSize || mPinyinTextSize <= 0) {
            return this;
        }
        this.mPinyinTextSize = mPinyinTextSize;
        calTextHeight();
        requestLayout();
        invalidate();
        return this;
    }

    public float getPinyinTextRadio() {
        return mPinyinTextRadio;
    }

    public PinYinTextView setPinyinTextRadio(float mPinyinTextRadio) {
        if (this.mPinyinTextRadio == mPinyinTextRadio || mPinyinTextRadio <= 0) {
            return this;
        }
        this.mPinyinTextRadio = mPinyinTextRadio;
        setPinyinTextSize(mTextSize * mPinyinTextRadio);
        return this;
    }
    //endregion

    //region textColor
    public int getTextColor() {
        return mTextColor;
    }

    public PinYinTextView setTextColor(int mTextColor) {
        if (this.mTextColor != mTextColor) {
            this.mTextColor = mTextColor;
            setData(mData);
        }
        return this;
    }

    public PinYinTextView setTextColor(int position, int mTextColor) {
        if (mData.size() > 0 && mData.size() > position) {
            if (mData.get(position).textColor == mTextColor) {
                return this;
            }
            PinyinBean word = mData.get(position);
            word.textColor = mTextColor;
            setData(mData);
        }
        return this;
    }

    public PinYinTextView setAllTextColor(int mTextColor) {
        for (int i = 0; i < mData.size(); i++) {
            PinyinBean word = mData.get(i);
            word.textColor = 0;
        }
        this.mTextColor = mTextColor;
        setData(mData);
        return this;
    }

    //endregion

    //region pinyinColor
    public int getPinyinColor() {
        return mPinyinColor;
    }

    public PinYinTextView setPinyinColor(int mPinyinColor) {
        if (this.mPinyinColor != mPinyinColor) {
            this.mPinyinColor = mPinyinColor;
            setData(mData);
        }
        return this;
    }

    public PinYinTextView setPinyinColor(int position, int mPinyinColor) {
        if (mData.size() > 0 && mData.size() > position) {
            if (mData.get(position).pinyinColor == mPinyinColor) {
                return this;
            }
            PinyinBean word = mData.get(position);
            word.pinyinColor = mPinyinColor;
            setData(mData);
        }
        return this;
    }

    public PinYinTextView setAllPinyinColor(int mPinyinColor) {
        for (int i = 0; i < mData.size(); i++) {
            PinyinBean word = mData.get(i);
            word.pinyinColor = 0;
        }
        this.mPinyinColor = mPinyinColor;
        setData(mData);
        return this;
    }
    //endregion

    //region 间距
    public int getHorizontalSpacing() {
        return mHorizontalSpacing;
    }

    public PinYinTextView setHorizontalSpacing(int mHorizontalSpacing) {
        if (this.mHorizontalSpacing == mHorizontalSpacing || mHorizontalSpacing < 0) {
            return this;
        }
        this.mHorizontalSpacing = mHorizontalSpacing;
        this.mPinyinTextSpacing = mHorizontalSpacing / 2;
        requestLayout();
        invalidate();
        return this;
    }

    public int getVerticalSpacing() {
        return mVerticalSpacing;
    }

    public PinYinTextView setVerticalSpacing(int mVerticalSpacing) {
        if (this.mVerticalSpacing == mVerticalSpacing || mVerticalSpacing < 0) {
            return this;
        }
        this.mVerticalSpacing = mVerticalSpacing;
        requestLayout();
        invalidate();
        return this;
    }

    public int getPinyinTextSpacing() {
        return mPinyinTextSpacing;
    }

    public PinYinTextView setPinyinTextSpacing(int mPinyinTextSpacing) {
        if (this.mPinyinTextSpacing == mPinyinTextSpacing || mPinyinTextSpacing < 0) {
            return this;
        }
        this.mPinyinTextSpacing = mPinyinTextSpacing;
        requestLayout();
        invalidate();
        return this;
    }

    //endregion

    //region 首行缩进
    public int getTextIndent() {
        return mTextIndent;
    }

    public PinYinTextView setTextIndent(int mTextIndent) {
        if (this.mTextIndent == mTextIndent || mTextIndent < 0) {
            return this;
        }
        this.mTextIndent = mTextIndent;
        setData(mData);
        return this;
    }
    //endregion

    //region 设置数据
    public List<PinyinBean> getData() {
        return mData;
    }

    public void setData(List<PinyinBean> data) {
        mDataCompat.clear();
        mData = new ArrayList<>();
        mText = null;
        mPinyin = null;
        mColorText = null;
        mColorPinyin = null;
        if (data != null && data.size() > 0) {
            StringBuilder mSbColorText = new StringBuilder();
            StringBuilder mSbColorPinyin = new StringBuilder();
            StringBuilder mSbText = new StringBuilder();
            StringBuilder mSbPinyin = new StringBuilder();
            for (int i = 0; i < mTextIndent; i++) {
                mSbColorText.append("<font>\u3000</font>");
                mSbColorPinyin.append("<font>\u3000</font>");
                mDataCompat.add(new PinyinCompat(PinyinBean.create("\u3000", "\u3000")));
            }
            for (int i = 0; i < data.size(); i++) {
                PinyinBean word = data.get(i);
                if (word != null) {
                    mSbText.append(word.text);
                    mSbPinyin.append(word.pinyin);
                    mSbPinyin.append(" ");
                    if ("\n".equals(word.text)) {
                        word.text = "<br>";
                        word.pinyin = "<br>";
                    }
                    mData.add(word);
                    if (word.textColor == 0) {
                        mSbColorText.append(word.text);
                    } else {
                        mSbColorText.append(convertTokenToHtml(word.text, word.textColor));
                    }
                    if (word.pinyinColor == 0) {
                        mSbColorPinyin.append(word.pinyin);
                    } else {
                        mSbColorPinyin.append(convertTokenToHtml(word.pinyin, word.pinyinColor));
                    }
                    mSbColorPinyin.append(" ");
                    PinyinCompat pinyinCompat = new PinyinCompat(word);
                    pinyinCompat.textColor = pinyinCompat.textColor == 0 ? mTextColor : pinyinCompat.textColor;
                    pinyinCompat.pinyinColor = pinyinCompat.pinyinColor == 0 ? mPinyinColor : pinyinCompat.pinyinColor;
                    mDataCompat.add(pinyinCompat);
                    if ("<br>".equals(word.text)) {
                        for (int j = 0; j < mTextIndent; j++) {
                            mSbColorText.append("<font>\u3000</font>");
                            mSbColorPinyin.append("<font>\u3000</font>");
                            mDataCompat.add(new PinyinCompat(PinyinBean.create("\u3000", "\u3000")));
                        }
                    }
                }
            }
            if (mDataCompat.size() > 0) {
                mText = mSbText.toString();
                mColorText = mSbColorText.toString();
                mPinyin = mSbPinyin.substring(0, mSbPinyin.length() - 1);
                mColorPinyin = mSbColorPinyin.substring(0, mSbColorPinyin.length() - 1);
            }
        }

        calTextHeight();
        requestLayout();
        invalidate();
    }

    public void setData(String hanzi, String pinyin) {
        if (hanzi != null && pinyin != null) {
            String[] splitPinyin = pinyin.split(" ");
//            Log.e("test", hanzi.length() + " : " + splitPinyin.length);
            int count = hanzi.length() > splitPinyin.length ? splitPinyin.length : hanzi.length();
            List<PinyinBean> beans = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                beans.add(PinyinBean.create(hanzi.charAt(i) + "", splitPinyin[i]));
            }
            setData(beans);
        }
    }

    private String convertTokenToHtml(String text, int color) {
        String textColor = String.format("#%06X", 0xFFFFFF & color);
        return String.format("<font color=\"%s\">%s</font>", textColor, text);
    }
    //endregion

    public String getText() {
        return mText == null ? "" : mText;
    }

    public String getPinyin() {
        return mPinyin == null ? "" : mPinyin;
    }

    //region 下划线
    public boolean isUnderline() {
        return mUnderline;
    }

    public PinYinTextView setUnderline(boolean mUnderline) {
        if (this.mUnderline == mUnderline) {
            return this;
        }
        this.mUnderline = mUnderline;
        requestLayout();
        invalidate();
        return this;
    }

    public int getUnderlineColor() {
        return mUnderlineColor;
    }

    public PinYinTextView setUnderlineColor(int mUnderlineColor) {
        if (this.mUnderlineColor == mUnderlineColor) {
            return this;
        }
        this.mUnderlineColor = mUnderlineColor;
        mUnderlinePaint.setColor(mUnderlineColor);
        invalidate();
        return this;
    }

    public int getUnderlineWidth() {
        return mUnderlineWidth;
    }

    public PinYinTextView setUnderlineWidth(int mUnderlineWidth) {
        if (mUnderlineWidth <= 0 || this.mUnderlineWidth == mUnderlineWidth) {
            return this;
        }
        this.mUnderlineWidth = mUnderlineWidth;
        mUnderlinePaint.setStrokeWidth(mUnderlineWidth);
        if (mUnderlineSolid) {
            mUnderlinePaint.setPathEffect(null);
        } else {
            mUnderlinePaint.setPathEffect(new DashPathEffect(new float[]{mUnderlineWidth, mUnderlineWidth, mUnderlineWidth, mUnderlineWidth}, 0));
        }
        invalidate();
        return this;
    }

    public boolean isUnderlineSolid() {
        return mUnderlineSolid;
    }

    public PinYinTextView setUnderlineSolid(boolean mUnderlineSolid) {
        if (this.mUnderlineSolid == mUnderlineSolid) {
            return this;
        }
        this.mUnderlineSolid = mUnderlineSolid;
        if (mUnderlineSolid) {
            mUnderlinePaint.setPathEffect(null);
        } else {
            mUnderlinePaint.setPathEffect(new DashPathEffect(new float[]{mUnderlineWidth, mUnderlineWidth, mUnderlineWidth, mUnderlineWidth}, 0));
        }
        invalidate();
        return this;
    }

    public int getUnderlineSpacing() {
        return mUnderlineSpacing;
    }

    public PinYinTextView setUnderlineSpacing(int mUnderlineSpacing) {
        if (this.mUnderlineSpacing == mUnderlineSpacing || mUnderlineSpacing < 0) {
            return this;
        }
        this.mUnderlineSpacing = mUnderlineSpacing;
        requestLayout();
        invalidate();
        return this;
    }
    //endregion

    //endregion

    private void calTextHeight() {
        // calculate text height
        String chinese = "你好";
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds(chinese, 0, chinese.length(), mBounds);
        mTextHeight = mBounds.height();
        // calculate pinyin height
        String pinyin = "āáǎàaHhJjPpYyGg";
        if (mDrawType == PIN_YIN_TYPE.TYPE_PINYIN) {
            mPaint.setTextSize(mTextSize);
        } else {
            mPaint.setTextSize(mPinyinTextSize);
        }
        mPaint.getTextBounds(pinyin, 0, pinyin.length() - 1, mBounds);
        mPinyinHeight = mBounds.height();
    }

    //region 测量view大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (PIN_YIN_TYPE.TYPE_PINYIN_AND_TEXT == mDrawType && mDataCompat.size() > 0) {
            measurePinyinText(widthMeasureSpec, heightMeasureSpec);
        } else if (PIN_YIN_TYPE.TYPE_PINYIN == mDrawType && !TextUtils.isEmpty(mColorPinyin)) {
            measurePinyin(widthMeasureSpec, heightMeasureSpec);
        } else if (PIN_YIN_TYPE.TYPE_PLAIN_TEXT == mDrawType && !TextUtils.isEmpty(mColorText)) {
            measurePlainText(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureDefault(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void measurePinyinText(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeft = this.getPaddingLeft();
        int paddingRight = this.getPaddingRight();
        int paddingTop = this.getPaddingTop();
        int paddingBottom = this.getPaddingBottom();

        // max allowed width or height
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight;
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom;

        // mode
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // measured width and height
        int measuredWidth = modeWidth == MeasureSpec.EXACTLY ? sizeWidth : 0;
        int measuredHeight = modeHeight == MeasureSpec.EXACTLY ? sizeHeight : 0;

        int line = 0;
        int col = 0;
        int lineLength = 0;
        int baseLine = 0; // top of pinyin
        boolean newLine = false;

        for (PinYinTextView.PinyinCompat compat : mDataCompat) {
            if ("<br>".equals(compat.text)) {
                newLine = false;
                line = 0;
                col = 0;
                lineLength = 0;
                baseLine += mTextHeight + mPinyinHeight + mPinyinTextSpacing + mVerticalSpacing;
                continue;
            }

            int textWidth = getTextWidth(compat.text, mTextSize);
            int pinyinWidth = getTextWidth(compat.pinyin, mPinyinTextSize);

            int maxWidth = Math.max(textWidth, pinyinWidth);

            if (newLine) {
                line++;
                col = 0;
                newLine = false;
            }

            if (lineLength + maxWidth + (col == 0 ? 0 : mHorizontalSpacing) > sizeWidth) {
                //需要换行
                lineLength = maxWidth;

                baseLine += mTextHeight + mPinyinHeight + mPinyinTextSpacing + mVerticalSpacing;
                // TODO: add the underline vertical space if show underline

                if (modeWidth != MeasureSpec.EXACTLY) {
                    measuredWidth = sizeWidth;
                }

                newLine = true;
            } else {
                if (col != 0 || line != 0) { // not the first item of first row
                    lineLength += mHorizontalSpacing;
                }
                lineLength += maxWidth;

                if (modeWidth != MeasureSpec.EXACTLY && measuredWidth < lineLength) {
                    measuredWidth = lineLength;
                    if (measuredWidth > sizeWidth) {
                        measuredWidth = sizeWidth;
                    }
                }
                col++;
            }

            // Center the pinyin/text
            int pinyinBias = 0;
            int textBias = 0;
            if (pinyinWidth < textWidth) {
                pinyinBias = (textWidth - pinyinWidth) / 2;
            } else {
                textBias = (pinyinWidth - textWidth) / 2;
            }

            compat.pinyinRect.left = lineLength - maxWidth + pinyinBias;
            compat.pinyinRect.right = compat.pinyinRect.left + pinyinWidth;
            compat.pinyinRect.top = baseLine;
            compat.pinyinRect.bottom = compat.pinyinRect.top + mPinyinHeight;

            compat.textRect.left = lineLength - maxWidth + textBias;
            compat.textRect.right = compat.textRect.left + textWidth;
            compat.textRect.top = compat.pinyinRect.bottom + mPinyinTextSpacing;
            compat.textRect.bottom = compat.textRect.top + mTextHeight;

            compat.pinyinTextRect.left = Math.min(compat.pinyinRect.left, compat.textRect.left);
            compat.pinyinTextRect.right = Math.max(compat.pinyinRect.right, compat.textRect.right);
            compat.pinyinTextRect.top = compat.pinyinRect.top;
            compat.pinyinTextRect.bottom = compat.textRect.bottom;

        }

        if (modeHeight != MeasureSpec.EXACTLY) {
            measuredHeight = baseLine + mPinyinHeight + mPinyinTextSpacing + mTextHeight + mTextHeight / 4;
        }

        setMeasuredDimension(measuredWidth + paddingLeft + paddingRight, measuredHeight + paddingTop + paddingBottom);
    }

    private int getTextWidth(String text, float textSize) {
        mPaint.setTextSize(textSize);
        return (int) Math.ceil(Layout.getDesiredWidth(text, mPaint));
    }

    private void measurePlainText(int widthMeasureSpec, int heightMeasureSpec) {
        measureText(widthMeasureSpec, heightMeasureSpec, mColorText, mTextSize);
    }

    private void measurePinyin(int widthMeasureSpec, int heightMeasureSpec) {
        measureText(widthMeasureSpec, heightMeasureSpec, mColorPinyin, mPinyinTextSize);
    }

    private void measureText(int widthMeasureSpec, int heightMeasureSpec, String text, float textSize) {
        mTextIndentWidth = getTextWidth("\u3000", textSize);

        int paddingLeft = this.getPaddingLeft();
        int paddingRight = this.getPaddingRight();
        int paddingTop = this.getPaddingTop();
        int paddingBottom = this.getPaddingBottom();

        // max allowed width or height
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight;
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom;

        // mode
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // calculate text width and height
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(textSize);

        mDrawSpanned = fromHtml(text);

        mStaticLayout = new StaticLayout(mDrawSpanned, mPaint, sizeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, mVerticalSpacing, false);
        // measured width and height
        int measuredWidth =
                modeWidth == MeasureSpec.EXACTLY
                        ? sizeWidth
                        : Math.min(sizeWidth, (int) Math.ceil(Layout.getDesiredWidth(mDrawSpanned, mPaint)));
        int measuredHeight =
                modeHeight == MeasureSpec.EXACTLY
                        ? sizeHeight
                        : mStaticLayout.getHeight();

        if (isUnderline()) {
            measuredHeight += mUnderlineSpacing;
        }

        setMeasuredDimension(measuredWidth + paddingLeft + paddingRight, measuredHeight + paddingTop + paddingBottom);
    }

    private Spanned fromHtml(String html) {
        if (html == null) {
            html = "";
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    private void measureDefault(int widthMeasureSpec, int heightMeasureSpec) {
        // max allowed width or height
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        // mode
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // measured width and height
        int measuredWidth =
                modeWidth == MeasureSpec.EXACTLY ? sizeWidth : getPaddingLeft() + getPaddingRight();
        int measuredHeight =
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    //endregion

    //region draw

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (PIN_YIN_TYPE.TYPE_PINYIN_AND_TEXT == mDrawType && mDataCompat.size() > 0) {
            drawPinyinAndText(canvas);
        } else if (PIN_YIN_TYPE.TYPE_PINYIN == mDrawType && !TextUtils.isEmpty(mColorPinyin)) {
            drawOnlyPinyin(canvas);
        } else if (PIN_YIN_TYPE.TYPE_PLAIN_TEXT == mDrawType && !TextUtils.isEmpty(mColorText)) {
            drawOnlyText(canvas);
        } else {

        }
    }

    private void drawPinyinAndText(Canvas canvas) {
        int paddingLeft = this.getPaddingLeft();
        int paddingTop = this.getPaddingTop();

        for (int i = 0; i < mDataCompat.size(); i++) {
            PinyinCompat compat = mDataCompat.get(i);

            if ("<br>".equals(compat.text)) {
                continue;
            }

            // draw pinyin
            mPaint.setColor(compat.pinyinColor);
            mPaint.setTextSize(mPinyinTextSize);
            compat.pinyinRect.offset(paddingLeft, paddingTop);
            // If the draw mode is TYPE_PINYIN_AND_TEXT, don't draw the pinyin if it's punctuation
            if (compat.pinyin != null && !compat.pinyin.equals(compat.text)) {
                canvas.drawText(compat.pinyin, compat.pinyinRect.left, compat.pinyinRect.bottom, mPaint);
            }

            // draw text
            mPaint.setColor(compat.textColor);
            mPaint.setTextSize(mTextSize);
            compat.textRect.offset(paddingLeft, paddingTop);
            canvas.drawText(compat.text, compat.textRect.left, compat.textRect.bottom, mPaint);


            compat.pinyinTextRect.offset(paddingLeft, paddingTop);
            if (mUnderline && !"\u3000".equals(compat.text)) {
                canvas.drawLine(
                        compat.pinyinTextRect.left,
                        compat.pinyinTextRect.bottom + mUnderlineSpacing,
                        compat.pinyinTextRect.right + mHorizontalSpacing,
                        compat.pinyinTextRect.bottom + mUnderlineSpacing,
                        mUnderlinePaint);
            }

            if (debugDraw) {
                mDebugPaint.setColor(mTextColor);
                canvas.drawRect(compat.textRect, mDebugPaint);
            }

            if (debugDraw) {
                mDebugPaint.setColor(mTextColor);
                canvas.drawRect(compat.pinyinRect, mDebugPaint);
            }

            if (debugDraw) {
                mDebugPaint.setColor(mTextColor);
                canvas.drawRect(compat.pinyinTextRect, mDebugPaint);
            }

            compat.pinyinRect.offset(-paddingLeft, -paddingTop);
            compat.textRect.offset(-paddingLeft, -paddingTop);
            compat.pinyinTextRect.offset(-paddingLeft, -paddingTop);
        }
    }

    private void drawOnlyPinyin(Canvas canvas) {
        drawText(canvas, mPinyinColor);
    }

    private void drawOnlyText(Canvas canvas) {
        drawText(canvas, mTextColor);
    }

    private void drawText(Canvas canvas, int color) {
        if (mStaticLayout != null && mDrawSpanned != null) {
            mPaint.setColor(color);
            int paddingLeft = this.getPaddingLeft();
            int paddingTop = this.getPaddingTop();
            canvas.translate(paddingLeft, paddingTop);
            mStaticLayout.draw(canvas);
            if (mUnderline) {
                for (int i = 0; i < mStaticLayout.getLineCount(); i++) {
                    int drawOffset = 0;
                    try {
                        int startPosition = mStaticLayout.getLineStart(i);
                        int endPosition = mStaticLayout.getLineEnd(i);
                        String lineContent = "" + mDrawSpanned.subSequence(startPosition, endPosition);
                        drawOffset = createDrawOffset(lineContent);
                    } catch (Throwable t) {
                    }
                    int y = i == mStaticLayout.getLineCount() - 1 ? mStaticLayout.getLineBottom(i) : mStaticLayout.getLineBottom(i) - mVerticalSpacing;
                    y += mUnderlineSpacing;
                    canvas.drawLine(
                            mStaticLayout.getLineLeft(i) + drawOffset,
                            y,
                            mStaticLayout.getLineRight(i),
                            y,
                            mUnderlinePaint);
                }
            }
        }
    }

    private int createDrawOffset(String lineContent) {
        if (lineContent != null) {
            int result = 0;
            int i = 0;
            while (i < lineContent.length() && lineContent.charAt(i) == '\u3000') {
                result += mTextIndentWidth;
                i++;
            }
            return result;
        }
        return 0;
    }

    //endregion

    //region 实体类
    public static class PinyinBean {

        String text;

        int textColor;
        String pinyin;

        int pinyinColor;

        private PinyinBean() {
        }

        public static PinyinBean create(String text, String pinyin) {
            PinyinBean pinyinBean = new PinyinBean();
            pinyinBean.text = text;
            pinyinBean.pinyin = pinyin;
            return pinyinBean;
        }

        public static PinyinBean create(String text, int textColor, String pinyin, int pinyinColor) {
            PinyinBean pinyinBean = create(text, pinyin);
            pinyinBean.textColor = textColor;
            pinyinBean.pinyinColor = pinyinColor;
            return pinyinBean;
        }
    }


    private static class PinyinCompat {
        String text;

        int textColor;
        String pinyin;

        int pinyinColor;
        Rect pinyinTextRect = new Rect();
        Rect textRect = new Rect();
        Rect pinyinRect = new Rect();

        public PinyinCompat(PinyinBean pinyinBean) {
            text = pinyinBean.text;
            textColor = pinyinBean.textColor;
            pinyin = pinyinBean.pinyin;
            pinyinColor = pinyinBean.pinyinColor;
        }
    }
    //endregion

}
