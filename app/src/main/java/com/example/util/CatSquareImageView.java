
package com.example.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CatSquareImageView extends ImageView
{

    public CatSquareImageView(Context context)
    {
        super(context);
    }

    public CatSquareImageView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
    }

    public CatSquareImageView(Context context, AttributeSet attributeset, int i)
    {
        super(context, attributeset, i);
    }

    protected void onMeasure(int i, int j)
    {
        super.onMeasure(i, j);
        setMeasuredDimension(getMeasuredWidth(), (int) ((int) (getMeasuredWidth())/1.1));
    }
}
