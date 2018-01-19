package com.exmaple.lensview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.util.DisplayMetrics;

@SuppressWarnings({"NumericCastThatLosesPrecision", "MethodWithTooManyParameters"})
public class LensCalculator {

    // Algorithm for calculating equispaced grid
    static Grid calculateGrid(Context context, int screenWidth, int screenHeight, int
            itemCount, float iconSize) {
        Grid grid = new Grid();
        grid.setItemCount(itemCount);
        int itemCountHorizontal, itemCountVertical;
        if (itemCount == 0 || itemCount == 1) {
            itemCountHorizontal = itemCount;
            itemCountVertical = itemCount;
        } else {
            double optimalSquareSize = calculateOptimalSquareSize(screenWidth, screenHeight,
                    itemCount);
            itemCountHorizontal = (int) Math.ceil(screenWidth / optimalSquareSize);
            itemCountVertical = (int) Math.ceil((double) itemCount / (double) itemCountHorizontal);
        }
        grid.setItemCountHorizontal(itemCountHorizontal);
        grid.setItemCountVertical(itemCountVertical);
        float itemSize = LensCalculator.convertDpToPixel(iconSize, context);
        grid.setItemSize(itemSize);
        float spacingHorizontal = ((float) screenWidth - ((float) itemCountHorizontal *
                itemSize)) / (float) (itemCountHorizontal + 1);
        grid.setSpacingHorizontal(spacingHorizontal);
        float spacingVertical = ((float) screenHeight - ((float) itemCountVertical * itemSize))
                / (float) (itemCountVertical + 1);
        grid.setSpacingVertical(spacingVertical);
        return grid;
    }

    // Algorithm for calculating optimal square side length given width, height and number of items
    private static double calculateOptimalSquareSize(int screenWidth, int screenHeight, int
            itemCount) {
        // Source: http://math.stackexchange
        // .com/questions/466198/algorithm-to-get-the-maximum-size-of-n-squares-that-fit-into-a
        // -rectangle-with-a
        double x = (double) screenWidth, y = (double) screenHeight, n = (double) itemCount;
        double px = Math.ceil(Math.sqrt(n * x / y));
        double sx, sy;
        if (Math.floor(px * y / x) * px < n) {
            sx = y / Math.ceil(px * y / x);
        } else {
            sx = x / px;
        }
        double py = Math.ceil(Math.sqrt(n * y / x));
        if (Math.floor(py * x / y) * py < n) {
            sy = x / Math.ceil(x * py / y);
        } else {
            sy = y / py;
        }
        return Math.max(sx, sy);
    }

    // Algorithm for circular distance
    public static double calculateDistance(float x1, float x2, float y1, float y2) {
        return Math.sqrt(Math.pow((double) (x2 - x1), 2) + Math.pow((double) (y2 - y1), 2));
    }

    // Algorithm for determining whether a rect is within a given lens (centered at touchX, touchY)
    public static boolean isRectWithinLens(RectF rect, float touchX, float touchY, float
            lensDiameter) {
        if (rect.left >= touchX - lensDiameter / 2.0f &&
                rect.right <= touchX + lensDiameter / 2.0f &&
                rect.top >= touchY - lensDiameter / 2.0f &&
                rect.bottom <= touchY + lensDiameter / 2.0f) {
            return true;
        } else {
            return false;
        }
    }

    // Graphical Fisheye Lens algorithm for shifting
    static float shiftPoint(Context context, float lensPosition, float itemPosition, float
            boundary, float multiplier, double distortionFactor) {
        if (lensPosition < 0) {
            return itemPosition;
        }
        float shiftedPosition;
        float a = Math.abs(lensPosition - itemPosition);
        float b = Math.max(lensPosition, boundary - lensPosition);
        float x = a / b;
        float d = (float) (multiplier * distortionFactor);
        float y = ((1.0f + d) * x) / (1.0f + (d * x));
        float newDistanceFromCenter = b * y;
        if (lensPosition >= itemPosition) {
            shiftedPosition = lensPosition - newDistanceFromCenter;
        } else {
            shiftedPosition = lensPosition + newDistanceFromCenter;
        }
        return shiftedPosition;
    }

    // Graphical Fisheye Lens algorithm for scaling
    static float scalePoint(Context context, float lensPosition, float itemPosition, float
            itemSize, float boundary, float multiplier, double scaleFactor, double
                                    distortionFactor) {
        if (lensPosition < 0) {
            return itemSize;
        }
        float scaleDifference = (float) (scaleFactor - LensView.ScaleFactor.MIN_SCALE_FACTOR);
        float d = LensView.ScaleFactor.MIN_SCALE_FACTOR + scaleDifference * multiplier;
        if (lensPosition >= itemPosition) {
            itemPosition = itemPosition - d * (itemSize / 2.0f);
        } else {
            itemPosition = itemPosition + d * (itemSize / 2.0f);
        }
        return LensCalculator.shiftPoint(context, lensPosition, itemPosition, boundary,
                multiplier, distortionFactor);
    }

    // Graphical Fisheye Lens algorithm for determining final scaled size
    static float calculateSquareScaledSize(float scaledPositionX, float shiftedPositionX,
                                           float scaledPositionY, float shiftedPositionY) {
        return 2.0f * Math.min(Math.abs(scaledPositionX - shiftedPositionX), Math.abs
                (scaledPositionY - shiftedPositionY));
    }

    // Algorithm for calculating new rect
    static RectF calculateRect(float newCenterX, float newCenterY, float newSize) {
        RectF newRect = new RectF(
                newCenterX - newSize / 2.0f,
                newCenterY - newSize / 2.0f,
                newCenterX + newSize / 2.0f,
                newCenterY + newSize / 2.0f);
        return newRect;
    }

    // Algorithm for determining if touch point is within rect
    static boolean isInsideRect(float x, float y, RectF rect) {
        if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
            return true;
        } else {
            return false;
        }
    }

    // Algorithm for converting dp measurements to pixels
    private static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    // Algorithm for converting pixels to dp measurements
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
}
