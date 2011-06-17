/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.api.activiti.bpmndiagram;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramCanvas;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.util.IoUtil;
import org.springframework.util.CollectionUtils;

/**
 * Keep png file size of Eclipse-Designer' generated.
 * @author mengran
 */
public class SmartSmoothDrawingPDC extends ProcessDiagramCanvas {

    protected static Color TASK_COLOR_COMPLETED = new Color(230, 230, 230);
    List<String> takeTransitions = null;
    Map<String, List<Integer>> taskDefinitionKeyPosition = new HashMap<String, List<Integer>>();

    public List<String> getTakeTransitions() {
        return takeTransitions;
    }

    public SmartSmoothDrawingPDC setTakeTransitions(List<String> takeTransitions) {
        this.takeTransitions = takeTransitions;
        return this;
    }

    /**
     * Fill when drawing for improve performance.
     * @return task definition key & position map
     */
    public Map<String, List<Integer>> getTaskDefinitionKeyPosition() {
        return taskDefinitionKeyPosition;
    }

    void addTaskDefinitionKeyPosition(String taskDefinitionKey, Integer[] position) {

        taskDefinitionKeyPosition.put(taskDefinitionKey, Arrays.asList(position));
    }

    public SmartSmoothDrawingPDC(int width, int height, int minX, int minY) {
        super(width, height, minX, minY);

        // Add by MENGRAN for
        g.setFont(new Font("宋体", Font.PLAIN, 12));
        this.fontMetrics = g.getFontMetrics();
    }

    public SmartSmoothDrawingPDC(int width, int height) {
        super(width, height);

        // Add by MENGRAN for
        g.setFont(new Font("宋体", Font.PLAIN, 12));
        this.fontMetrics = g.getFontMetrics();
    }

    private boolean haveExecuted(ActivityImpl activity) {

        if (CollectionUtils.isEmpty(getTakeTransitions())) {
            return false;
        }

        for (PvmTransition transition : activity.getOutgoingTransitions()) {
            if (haveExecuted(transition)) {
                return true;
            }
        }

        return false;
    }

    private boolean haveExecuted(PvmTransition transition) {

        if (CollectionUtils.isEmpty(getTakeTransitions())) {
            return false;
        }

        if (getTakeTransitions().contains(transition.getId())) {
            return true;
        }

        return false;
    }

    public void drawStartEvent(int x, int y, int width, int height, Image image, ActivityImpl activity) {
        if (haveExecuted(activity)) {
            Paint originalPaint = g.getPaint();
            g.setPaint(TASK_COLOR_COMPLETED);
            g.draw(new Ellipse2D.Double(x, y, width, height));
            g.setPaint(originalPaint);
        } else {
            g.draw(new Ellipse2D.Double(x, y, width, height));
        }
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        }
    }

    public void drawNoneEndEvent(int x, int y, int width, int height, ActivityImpl activity) {
        if (haveExecuted(activity)) {
            Stroke originalStroke = g.getStroke();
            g.setStroke(END_EVENT_STROKE);
            Paint originalPaint = g.getPaint();
            g.setPaint(TASK_COLOR_COMPLETED);
            g.draw(new Ellipse2D.Double(x, y, width, height));
            g.setPaint(originalPaint);
            g.setStroke(originalStroke);
        } else {
            Stroke originalStroke = g.getStroke();
            g.setStroke(END_EVENT_STROKE);
            g.draw(new Ellipse2D.Double(x, y, width, height));
            g.setStroke(originalStroke);
        }
    }

    public void drawUserTask(String name, int x, int y, int width, int height, ActivityImpl activity) {
        drawTask(name, x, y, width, height, activity);
        g.drawImage(USERTASK_IMAGE, x + 7, y + 7, ICON_SIZE, ICON_SIZE, null);
    }

    public void drawTask(String name, int x, int y, int width, int height, ActivityImpl activity) {
        drawTask(name, x, y, width, height, false, activity);
    }

    protected void drawTask(String name, int x, int y, int width, int height, boolean thickBorder, ActivityImpl activity) {
        Paint originalPaint = g.getPaint();
        if (haveExecuted(activity)) {
            g.setPaint(TASK_COLOR_COMPLETED);
        } else {
            g.setPaint(TASK_COLOR);
        }

        // shape
        RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 20, 20);
        g.fill(rect);
        g.setPaint(originalPaint);

        if (thickBorder) {
            Stroke originalStroke = g.getStroke();
            g.setStroke(THICK_TASK_BORDER_STROKE);
            g.draw(rect);
            g.setStroke(originalStroke);
        } else {
            g.draw(rect);
        }

        // text
        if (name != null) {
            String text = fitTextToWidth(name, width);
            int textX = x + ((width - fontMetrics.stringWidth(text)) / 2);
            int textY = y + ((height - fontMetrics.getHeight()) / 2) + fontMetrics.getHeight();
            g.drawString(text, textX, textY);
        }
    }

    public void drawSequenceflow(int srcX, int srcY, int targetX, int targetY, boolean conditional, PvmTransition sequenceFlow) {
        Line2D.Double line = new Line2D.Double(srcX, srcY, targetX, targetY);
        Paint originalPaint = g.getPaint();
        if (haveExecuted(sequenceFlow)) {
            g.setPaint(TASK_COLOR_COMPLETED);
            g.draw(line);
            g.setPaint(originalPaint);
        } else {
            g.draw(line);
        }
        drawArrowHead(line, sequenceFlow);

        if (conditional) {
            drawConditionalSequenceFlowIndicator(line, sequenceFlow);
        }
    }

    public void drawSequenceflowWithoutArrow(int srcX, int srcY, int targetX, int targetY, boolean conditional, PvmTransition sequenceFlow) {
        Line2D.Double line = new Line2D.Double(srcX, srcY, targetX, targetY);
        Paint originalPaint = g.getPaint();
        if (haveExecuted(sequenceFlow)) {
            g.setPaint(TASK_COLOR_COMPLETED);
            g.draw(line);
            g.setPaint(originalPaint);
        } else {
            g.draw(line);
        }
        if (conditional) {
            drawConditionalSequenceFlowIndicator(line, sequenceFlow);
        }
    }

    public void drawArrowHead(Line2D.Double line, PvmTransition sequenceFlow) {
        int doubleArrowWidth = 2 * ARROW_WIDTH;
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, 0);
        arrowHead.addPoint(-ARROW_WIDTH, -doubleArrowWidth);
        arrowHead.addPoint(ARROW_WIDTH, -doubleArrowWidth);

        AffineTransform transformation = new AffineTransform();
        transformation.setToIdentity();
        double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
        transformation.translate(line.x2, line.y2);
        transformation.rotate((angle - Math.PI / 2d));

        AffineTransform originalTransformation = g.getTransform();
        g.setTransform(transformation);
        Paint originalPaint = g.getPaint();
        if (haveExecuted(sequenceFlow)) {
            g.setPaint(TASK_COLOR_COMPLETED);
            g.fill(arrowHead);
            g.setPaint(originalPaint);
        } else {
            g.fill(arrowHead);
        }
        g.setTransform(originalTransformation);
    }

    public void drawConditionalSequenceFlowIndicator(Line2D.Double line, PvmTransition sequenceFlow) {
        int horizontal = (int) (CONDITIONAL_INDICATOR_WIDTH * 0.7);
        int halfOfHorizontal = horizontal / 2;
        int halfOfVertical = CONDITIONAL_INDICATOR_WIDTH / 2;

        Polygon conditionalIndicator = new Polygon();
        conditionalIndicator.addPoint(0, 0);
        conditionalIndicator.addPoint(-halfOfHorizontal, halfOfVertical);
        conditionalIndicator.addPoint(0, CONDITIONAL_INDICATOR_WIDTH);
        conditionalIndicator.addPoint(halfOfHorizontal, halfOfVertical);

        AffineTransform transformation = new AffineTransform();
        transformation.setToIdentity();
        double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
        transformation.translate(line.x1, line.y1);
        transformation.rotate((angle - Math.PI / 2d));

        AffineTransform originalTransformation = g.getTransform();
        g.setTransform(transformation);
        g.draw(conditionalIndicator);

        Paint originalPaint = g.getPaint();
        if (haveExecuted(sequenceFlow)) {
            g.setPaint(TASK_COLOR_COMPLETED);
        } else {
            g.setPaint(CONDITIONAL_INDICATOR_COLOR);
        }
        g.fill(conditionalIndicator);

        g.setPaint(originalPaint);
        g.setTransform(originalTransformation);
    }

    /**
     * Generates an image of what currently is drawn on the canvas.
     *
     * Throws an {@link ActivitiException} when {@link #close()} is already called.
     */
    public byte[] generateImageByteArray(String imageType) {
        if (closed) {
            throw new ActivitiException("ProcessDiagramGenerator already closed");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
//            // Try to remove white space
//            minX = (minX <= 5) ? 5 : minX;
//            minY = (minY <= 5) ? 5 : minY;
            BufferedImage imageToSerialize = processDiagram;
//            if (minX >= 0 && minY >= 0) {
//                imageToSerialize = processDiagram.getSubimage(minX - 5, minY - 5,
//                        canvasWidth - minX + 5, canvasHeight - minY + 5);
//            }
            ImageIO.write(imageToSerialize, imageType, out);
        } catch (IOException e) {
            throw new ActivitiException("Error while generating process image", e);
        } finally {
            IoUtil.closeSilently(out);
        }
        return out.toByteArray();
    }
}
