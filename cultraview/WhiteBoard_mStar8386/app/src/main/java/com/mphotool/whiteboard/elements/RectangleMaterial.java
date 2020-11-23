package com.mphotool.whiteboard.elements;

import com.mphotool.whiteboard.utils.BaseUtils;
import com.mphotool.whiteboard.utils.MathUtils;
import com.mphotool.whiteboard.utils.PathUtils;
import com.mphotool.whiteboard.view.PanelManager;

import java.io.Serializable;

/**
 * @Description: 矩形
 * @Author: wanghang
 * @CreateDate: 2019/10/14 11:34
 * @UpdateUser: 更新者：
 * @UpdateDate: 2019/10/14 11:34
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class RectangleMaterial extends ShapeMaterial implements Serializable {
    private static final String TAG = RectangleMaterial.class.getSimpleName();

    public RectangleMaterial() {
        super();
    }

    public RectangleMaterial(PanelManager manager, int mBrushColor, float mBrushThickness) {
        super(manager, mBrushColor, mBrushThickness);
    }

    /**
     * 初始化顶点坐标
     * @param color
     * @param thickness
     */
    @Override
    protected void initVector(int color, float thickness) {
        mVector = new Ink();
        mVector.mInkColor = color;
        mVector.mThickness = thickness;
        mVector.mPoints = new Point[4];
        mVector.mPointCount = 4;

        for (int i = 0; i < mVector.mPointCount; i ++){
            mVector.mPoints[i] = new Point();
        }
    }

    @Override
    protected void handleInks() {
        super.handleInks();
        float distance = MathUtils.getDistance(startX, startY, midX, midY);

        if (Float.compare(distance, 4F) < 0){
            return;
        }
        float distance0 = MathUtils.getDistance(startX, startY, startX, midY);
        float distance1 = MathUtils.getDistance(startX, midY, midX, midY);
        insertInksPoint(startX, startY, startX, midY,distance0,false,true);
        insertInksPoint(startX, midY, midX, midY,distance1,false,false);
        insertInksPoint(midX, midY, midX, startY,distance0,false,false);
        insertInksPoint(midX, startY, startX, startY,distance1,true,false);
    }

    @Override
    protected void handleVector() {
        super.handleVector();
        PathUtils.addPoint(mVector.mPoints[0], startX, startY);
        PathUtils.addPoint(mVector.mPoints[1],startX, midY);
        PathUtils.addPoint(mVector.mPoints[2], midX, midY);
        PathUtils.addPoint(mVector.mPoints[3], midX, startY);
    }

    @Override
    protected void handlePath() {
        super.handlePath();

        this.mPath.reset();
        this.mPath.moveTo(startX, startY);
        this.mPath.lineTo(startX, midY);
        this.mPath.lineTo(midX, midY);
        this.mPath.lineTo(midX, startY);
        this.mPath.close();
    }

    @Override
    public boolean isCross(float x1, float y1, float x2, float y2) {
        return PathUtils.isCrossInk(mInk, x1, y1, x2, y2);
    }
}