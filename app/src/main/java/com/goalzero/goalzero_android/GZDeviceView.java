package com.goalzero.goalzero_android;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


/**
 * TODO: document your custom view class.
 */
public class GZDeviceView extends View
{
	private int mBattteryPercent;
	private float mVoltage;
	private int mTemperature;

	private TextPaint mBatteryTextPaint;
	private float mBatteryTextWidth;
	private float mBatteryTextHeight;

	private TextPaint mVoltageTextPaint;
	private float mVoltageTextWidth;
	private float mVoltageTextHeight;

	private TextPaint mTempuratureTextPaint;
	private float mTempuratureTextWidth;
	private float mTempuratureTextHeight;

	public GZDeviceView(Context context)
	{
		super(context);
		init(null, 0);
	}

	public GZDeviceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
	}

	public GZDeviceView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle)
	{
		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.GZDeviceView, defStyle, 0);

		mBattteryPercent = a.getInt(R.styleable.GZDeviceView_BatteryPercent, mBattteryPercent);
		mVoltage = a.getFloat(R.styleable.GZDeviceView_Voltage, mVoltage);
		mTemperature = a.getInt(R.styleable.GZDeviceView_Temperature, mTemperature);

		a.recycle();

		// Set up a default TextPaint object
		mBatteryTextPaint = new TextPaint();
		mBatteryTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mBatteryTextPaint.setTextAlign(Paint.Align.LEFT);

		mVoltageTextPaint = new TextPaint();
		mVoltageTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mVoltageTextPaint.setTextAlign(Paint.Align.LEFT);

		mTempuratureTextPaint = new TextPaint();
		mTempuratureTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTempuratureTextPaint.setTextAlign(Paint.Align.LEFT);

		// Update TextPaint and text measurements from attributes
		invalidateTextPaintAndMeasurements();
	}

	private void invalidateTextPaintAndMeasurements()
	{
		mBatteryTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics()));
		mBatteryTextPaint.setColor(Color.WHITE);
		mBatteryTextWidth = mBatteryTextPaint.measureText("" + mBattteryPercent  + "%");

		mVoltageTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
		mVoltageTextPaint.setColor(Color.WHITE);
		mVoltageTextWidth = mVoltageTextPaint.measureText("" + mVoltage  + "v");

		mTempuratureTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
		mTempuratureTextPaint.setColor(Color.WHITE);
		mTempuratureTextWidth = mTempuratureTextPaint.measureText("" + mTemperature + "°F");


		Paint.FontMetrics fontMetrics = mBatteryTextPaint.getFontMetrics();
		mBatteryTextHeight = fontMetrics.bottom;

		fontMetrics = mVoltageTextPaint.getFontMetrics();
		mVoltageTextHeight = fontMetrics.bottom;

		fontMetrics = mTempuratureTextPaint.getFontMetrics();
		mTempuratureTextHeight = fontMetrics.bottom;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		// TODO: consider storing these as member variables to reduce
		// allocations per draw cycle.
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		int paddingRight = getPaddingRight();
		int paddingBottom = getPaddingBottom();

		int contentWidth = getWidth() - paddingLeft - paddingRight;
		int contentHeight = getHeight() - paddingTop - paddingBottom;

		// Draw the text.
		canvas.drawText("" + mBattteryPercent + "%",
				paddingLeft,
				paddingTop + contentHeight + mBatteryTextHeight,
				mBatteryTextPaint);

		canvas.drawText("" + mVoltage  + "v",
				paddingLeft,
				paddingTop + contentHeight - (mVoltageTextHeight * 6)- 10,
				mVoltageTextPaint);

		canvas.drawText("" + mTemperature + "°F",
				paddingLeft + 225,
				paddingTop + contentHeight - (mTempuratureTextHeight * 6) - 10,
				mTempuratureTextPaint);
	}

	public int getBatteryPecrent()
	{
		return mBattteryPercent;
	}

	public float getVoltage()
	{
		return mVoltage;
	}

	public int getTemperature()
	{
		return mTemperature;
	}

	public void setBatteryPercent(int value)
	{
		mBattteryPercent = value;
		invalidate();
		requestLayout();
	}

	public void setVoltage(float value)
	{
		mVoltage = value;
		invalidate();
		requestLayout();
	}

	public void setTemperature(int value)
	{
		mTemperature = value;
		invalidate();
		requestLayout();
	}
}
