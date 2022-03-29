package com.alphawallet.app.widget;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alphawallet.app.R;
import com.google.android.material.button.MaterialButton;

public class CopyTextView extends LinearLayout {

    public static final String KEY_ADDRESS = "key_address";

    private final Context context;
    private MaterialButton button;

    private int textResId;
    private int gravity;
    private boolean showToast;
    private boolean boldFont;
    private boolean removePadding;
    private String rawAddress;
    private float marginRight;

    public CopyTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;

        inflate(context, R.layout.item_copy_textview, this);

        getAttrs(context, attrs);

        bindViews();
    }

    private void getAttrs(Context context, AttributeSet attrs)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CopyTextView,
                0, 0
        );

        try
        {
            textResId = a.getResourceId(R.styleable.CopyTextView_text, R.string.action_add_wallet);
            gravity = a.getInt(R.styleable.CopyTextView_android_gravity, Gravity.NO_GRAVITY);
            showToast = a.getBoolean(R.styleable.CopyTextView_showToast, true);
            boldFont = a.getBoolean(R.styleable.CopyTextView_bold, false);
            removePadding = a.getBoolean(R.styleable.CopyTextView_removePadding, false);
            marginRight = a.getDimension(R.styleable.CopyTextView_marginRight, 0.0f);
        } finally
        {
            a.recycle();
        }
    }

    private void bindViews()
    {
        button = findViewById(R.id.button);
        button.setText(textResId);
//        text.setText(textResId);
//        text.setTextColor(textColor);
//        text.setGravity(gravity);

//        LayoutParams layoutParams = (LayoutParams) text.getLayoutParams();
//        layoutParams.rightMargin = (int) marginRight;
//        text.setLayoutParams(layoutParams);

//        if(boldFont)
//        {
//            text.setTypeface(text.getTypeface(), Typeface.BOLD);
//        }
//
//        if(removePadding)
//        {
//            copy.setPadding(0, 0, 0, 0);
//        }

        button.setOnClickListener(v -> copyToClipboard());
//        copy.setOnClickListener(v -> copyToClipboard());
    }

    public String getText()
    {
        return rawAddress;
    }

    public void setText(CharSequence text)
    {
//        rawAddress = text.toString();
//        String breakAddr = rawAddress;
//        if ((gravity & Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL && WalletUtils.isValidAddress(breakAddr)) //split string across two lines
//        {
//            breakAddr = breakAddr.substring(0, 22) + " " + breakAddr.substring(22);
//        }
//
//        this.button.setText(breakAddr);
        this.button.setText(text.toString());
    }

    private void copyToClipboard()
    {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(KEY_ADDRESS, rawAddress);
        if (clipboard != null)
        {
            clipboard.setPrimaryClip(clip);
        }

        if (showToast)
            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }
}
