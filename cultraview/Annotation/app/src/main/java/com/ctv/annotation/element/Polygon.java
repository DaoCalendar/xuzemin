package com.ctv.annotation.element;

import android.graphics.Rect;
import android.util.Log;

import com.ctv.annotation.action.FrameBufferEraserState;

import java.util.ArrayList;

public class Polygon {
    private ArrayList<Rect> mPolyRect;
    private Rect mRect = new Rect();
    private ObjPool<Rect> mRectPool = new ObjPool(16, 16, 1024);
    private int mScreenHeiht;
    private int mScreenWidth;

    private int id = 0;
    Point mPoint = new Point(0.0f,0.0f);
    public boolean QuadrilateralTrajectory(float mPrevX,float mPrevY,float x,float y, float m){
        float radian = 0.0f;
        Point point1 = new Point(mPrevX,mPrevY);
        Point point2 = new Point(x,y);

        float angle1 = calAngle(mPoint,point1);
        float angle2 = calAngle(mPoint,point2);

        double distance = Math.sqrt((point2.mX - mPoint.mX)*(point2.mX - mPoint.mX) + (point2.mY - mPoint.mY)*(point2.mY - mPoint.mY));


        radian = Math.abs(Math.abs(angle2) - Math.abs(angle1));

 /*       BaseUtils.dbg(TAG, "QuadrilateralTrajectory mPoint.x=" + mPoint.mX + " mPoint.y=" + mPoint.mY);

        BaseUtils.dbg(TAG, "QuadrilateralTrajectory angle1=" + angle1 + " angle2=" + angle2);
        BaseUtils.dbg(TAG, "QuadrilateralTrajectory radian=" + radian);
        BaseUtils.dbg(TAG, "QuadrilateralTrajectory distance=" + distance);*/
        if (distance - m > 0) {
            mPoint = point1;
            return true;
        }else if(radian - 0.3f > 0) {
            mPoint = point1;
            return true;
        }
        return false;
    }
    public static float calAngle(Point p1, Point p2) {
        double radian = Math.atan2((p2.mY - p1.mY), (p2.mX - p1.mX));
        double angle = radian * (180 / Math.PI);
        //角度
        return (float) angle;
    }
    public void addRect(int left, int top, int right, int bottom) {
        mRect.left = Math.min(mRect.left, left);
        mRect.top = Math.min(mRect.top, top);
        mRect.right = Math.max(mRect.right, right);
        mRect.bottom = Math.max(mRect.bottom, bottom);

        Rect rect = (Rect) mRectPool.take();
        if (rect == null) {
            rect = new Rect(left, top, right, bottom);
        } else {
            rect.set(left, top, right, bottom);
        }
        mPolyRect.add(rect);
    }

    public Polygon(int screenWidth, int screenHeight) {
        Log.d("hhh", "----Polygon---");
        id = FrameBufferEraserState.PolygonId;
        FrameBufferEraserState.PolygonId ++;
        mScreenHeiht = screenHeight;
        mScreenWidth = screenWidth;
        mPolyRect = new ArrayList();
        mRect.left = mScreenWidth;
        mRect.right = 0;
        mRect.top = mScreenHeiht;
        mRect.bottom = 0;
    }

    public void addRect(Rect pRect) {
        mRect.left = Math.min(mRect.left, pRect.left);
        mRect.right = Math.max(mRect.right, pRect.right);
        mRect.top = Math.min(mRect.top, pRect.top);
        mRect.bottom = Math.max(mRect.bottom, pRect.bottom);
        mPolyRect.add(pRect);
    }

    public boolean isCross(Rect pRect) {
        int size = mPolyRect.size();
        for (int i = 0; i < size; i++) {
            if (((Rect) mPolyRect.get(i)).intersect(pRect)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(int pX, int pY) {
        int size = mPolyRect.size();
        for (int i = 0; i < size; i++) {
            if (pointInRect((Rect) mPolyRect.get(i), pX, pY)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(int pX, int pY, int rectIndex) {
        if (rectIndex == -1) {
            return contains(pX, pY);
        }
        int size = mPolyRect.size();
        for (int i = rectIndex; i < size; i++) {
            if (pointInRect((Rect) mPolyRect.get(i), pX, pY)) {
                return true;
            }
        }
        return false;
    }

    public int containsInRect(int pX, int pY) {
        int size = mPolyRect.size();
        for (int i = 0; i < size; i = (i + 1) + 1) {
            if (pointInRect((Rect) mPolyRect.get(i), pX, pY)) {
                return i;
            }
        }
        return -1;
    }

    public boolean pointInRect(Rect rect, int x, int y) {
        return x >= rect.left && x < rect.right && y >= rect.top && y < rect.bottom;
    }

    public void clear() {
        int size = mPolyRect.size();
        int i = 0;
        while (i < size && mRectPool.put(mPolyRect.get(i))) {
            i++;
        }
        mPolyRect.clear();
        mRect.left = mScreenWidth;
        mRect.right = 0;
        mRect.top = mScreenHeiht;
        mRect.bottom = 0;
    }

    public Rect getRect() {
        return mRect;
    }

}
