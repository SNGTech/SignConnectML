package com.sngtech.signconnect.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sngtech.signconnect.R;

import org.tensorflow.lite.task.gms.vision.detector.Detection;

import java.util.LinkedList;
import java.util.List;

public class DetectionBoxView extends View {

    private final int BOUNDING_RECT_TEXT_PADDING = 8;

    private List<Detection> results = new LinkedList<Detection>();
    private Paint boxPaint = new Paint();
    private Paint textBackgroundPaint = new Paint();
    private Paint textPaint = new Paint();

    private float scaleFactor = 1f;

    private Rect bounds = new Rect();

    public DetectionBoxView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void clear() {
        boxPaint.reset();
        textPaint.reset();
        textBackgroundPaint.reset();
        invalidate();
        initPaint();
    }

    public void initPaint() {
        textBackgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.detection_box_color));
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setTextSize(50f);

        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50f);

        boxPaint.setColor(ContextCompat.getColor(getContext(), R.color.detection_box_color));
        boxPaint.setStrokeWidth(8f);
        boxPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        for (Detection result : results) {
            RectF boundingBox = result.getBoundingBox();

            float top = boundingBox.top * scaleFactor;
            float bottom = boundingBox.bottom * scaleFactor;
            float left = boundingBox.left * scaleFactor;
            float right = boundingBox.right * scaleFactor;

            // Draw bounding box around detected objects
            RectF drawableRect = new RectF(left, top, right, bottom);
            canvas.drawRect(drawableRect, boxPaint);

            // Create text to display alongside detected objects
            String drawableText = "Letter: " +
                    result.getCategories().get(0).getLabel() + " " +
                            String.format("%.2f", result.getCategories().get(0).getScore());

            // Draw rect behind display text
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length(), bounds);
            float textWidth = bounds.width();
            float textHeight = bounds.height();
            canvas.drawRect(
                    left,
                    top,
                    left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                    top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                    textBackgroundPaint
            );

            // Draw text for detected object
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint);
        }
    }

    public void setResults(List<Detection> results, int imgWidth, int imgHeight) {
        this.results = results;

        // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
        // the size that the captured images will be displayed.
        scaleFactor = Math.max(getWidth() * 1f / imgWidth, getHeight() * 1f / imgHeight);
    }
}
