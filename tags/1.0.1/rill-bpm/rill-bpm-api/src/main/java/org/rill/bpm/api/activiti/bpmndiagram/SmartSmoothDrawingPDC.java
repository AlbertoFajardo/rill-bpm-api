/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rill.bpm.api.activiti.bpmndiagram;

import java.awt.BasicStroke;
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
 * 
 * @author mengran
 */
public class SmartSmoothDrawingPDC extends ProcessDiagramCanvas {

	protected static Color TASK_COLOR_COMPLETED = new Color(250, 208, 11);
	protected static Color TASK_COLOR = new Color(186, 205, 227);
	protected static Color HIGHLIGHT_COLOR = new Color(234, 81, 69);
	
	protected static Stroke CALLACTIVITY_TASK_BORDER_STROKE = new BasicStroke(3.2f);

	private List<String[]> takeTransitions = null;
	Map<String, List<Integer>> taskDefinitionKeyPosition = new HashMap<String, List<Integer>>();
	Map<String, String> taskDefinitionKeyType = new HashMap<String, String>();

	private List<String[]> getTakeTransitions() {
		return takeTransitions;
	}

	public SmartSmoothDrawingPDC setTakeTransitions(
			List<String[]> takeTransitions) {
		this.takeTransitions = takeTransitions;
		return this;
	}

	/**
	 * Fill when drawing for improve performance.
	 * 
	 * @return task definition key & position map
	 */
	public Map<String, List<Integer>> getTaskDefinitionKeyPosition() {
		return taskDefinitionKeyPosition;
	}

	void addTaskDefinitionKeyPosition(String taskDefinitionKey,
			Integer[] position) {

		taskDefinitionKeyPosition.put(taskDefinitionKey,
				Arrays.asList(position));
	}
	
	void addTaskDefinitionKeyType(String taskDefinitionKey, String type) {
		taskDefinitionKeyType.put(taskDefinitionKey, type);
	}

	public final Map<String, String> getTaskDefinitionKeyType() {
		return taskDefinitionKeyType;
	}

	public SmartSmoothDrawingPDC(int width, int height, int minX, int minY) {
		super(width, height, minX, minY);

		// Add by MENGRAN for
		g.setFont(new Font("宋体", Font.PLAIN, 12));
		g.setPaint(TASK_COLOR);
		this.fontMetrics = g.getFontMetrics();
	}

	public SmartSmoothDrawingPDC(int width, int height) {
		super(width, height);

		// Add by MENGRAN for
		g.setFont(new Font("宋体", Font.PLAIN, 12));
		this.fontMetrics = g.getFontMetrics();
	}

	private enum EXECUTE_STATUS {

		NO, ING, ED
	}

	private EXECUTE_STATUS haveExecuted(String nodeId) {

		nodeId = "(" + nodeId + ")";
		EXECUTE_STATUS status = EXECUTE_STATUS.NO;
		for (String[] tansitionIdName : getTakeTransitions()) {
			if (tansitionIdName[1].startsWith(nodeId)) {
				status = EXECUTE_STATUS.ED;
			} else if (tansitionIdName[1].endsWith(nodeId)) {
				status = EXECUTE_STATUS.ING;
			}
		}

		return status;
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
		
		for (String[] takeTransitionIdName : getTakeTransitions()) {
			if (takeTransitionIdName[0].equals(transition.getId())) {
				return true;
			}
		}

		return false;
	}

	public void drawStartEvent(int x, int y, int width, int height,
			Image image, ActivityImpl activity) {
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

	public void drawNoneEndEvent(int x, int y, int width, int height,
			ActivityImpl activity) {
		if (haveExecuted(activity) || EXECUTE_STATUS.ING.equals(haveExecuted(activity.getId()))) {
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

	public void drawUserTask(String name, int x, int y, int width, int height,
			ActivityImpl activity) {
		drawTask(name, x, y, width, height, activity);
		g.drawImage(USERTASK_IMAGE, x + 7, y + 7, ICON_SIZE, ICON_SIZE, null);
	}

	public void drawTask(String name, int x, int y, int width, int height,
			ActivityImpl activity) {
		drawTask(name, x, y, width, height, false, activity);
	}

	protected void drawTask(String name, int x, int y, int width, int height,
			boolean thickBorder, ActivityImpl activity) {
		Paint originalPaint = g.getPaint();
		if (haveExecuted(activity)) {
			g.setPaint(TASK_COLOR_COMPLETED);
		} else {
			g.setPaint(TASK_COLOR);
		}

		// shape
		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width,
				height, 20, 20);
		g.fill(rect);
		g.setPaint(originalPaint);

		g.setPaint(Color.GRAY);
		if (thickBorder) {
			Stroke originalStroke = g.getStroke();
			g.setStroke(THICK_TASK_BORDER_STROKE);
			g.draw(rect);
			g.setStroke(originalStroke);
		} else {
			g.draw(rect);
		}
		g.setPaint(originalPaint);

		// text
		if (name != null) {
			String text = fitTextToWidth(name, width);
			int textX = x + ((width - fontMetrics.stringWidth(text)) / 2);
			int textY = y + ((height - fontMetrics.getHeight()) / 2)
					+ fontMetrics.getHeight();
			
			Paint originalPaint1 = g.getPaint();
			g.setPaint(Color.BLACK);
			g.drawString(text, textX, textY);
			g.setPaint(originalPaint1);
		}
	}

	public void drawSequenceflow(int srcX, int srcY, int targetX, int targetY,
			boolean conditional, PvmTransition sequenceFlow) {
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

	public void drawSequenceflowWithoutArrow(int srcX, int srcY, int targetX,
			int targetY, boolean conditional, PvmTransition sequenceFlow) {
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

	public void drawConditionalSequenceFlowIndicator(Line2D.Double line,
			PvmTransition sequenceFlow) {
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

	public void drawParallelGateway(String id, int x, int y, int width,
			int height) {
		// rhombus
		drawGateway(id, x, y, width, height);

		// plus inside rhombus
		Stroke orginalStroke = g.getStroke();
		g.setStroke(GATEWAY_TYPE_STROKE);
		Paint originalPaint = g.getPaint();
		if (haveExecuted(id).equals(EXECUTE_STATUS.ED)) {
			g.setPaint(TASK_COLOR_COMPLETED);
		}
		Line2D.Double line = new Line2D.Double(x + 10, y + height / 2, x
				+ width - 10, y + height / 2); // horizontal
		g.draw(line);
		line = new Line2D.Double(x + width / 2, y + height - 10, x + width / 2,
				y + 10); // vertical
		g.draw(line);
		g.setStroke(orginalStroke);
		g.setPaint(originalPaint);
	}

	public void drawExclusiveGateway(String id, int x, int y, int width,
			int height) {
		// rhombus
		drawGateway(id, x, y, width, height);

		int quarterWidth = width / 4;
		int quarterHeight = height / 4;

		// X inside rhombus
		Stroke orginalStroke = g.getStroke();
		g.setStroke(GATEWAY_TYPE_STROKE);
		Paint originalPaint = g.getPaint();
		if (haveExecuted(id).equals(EXECUTE_STATUS.ED)) {
			g.setPaint(TASK_COLOR_COMPLETED);
		}
		Line2D.Double line = new Line2D.Double(x + quarterWidth + 3, y
				+ quarterHeight + 3, x + 3 * quarterWidth - 3, y + 3
				* quarterHeight - 3);
		g.draw(line);
		line = new Line2D.Double(x + quarterWidth + 3, y + 3 * quarterHeight
				- 3, x + 3 * quarterWidth - 3, y + quarterHeight + 3);
		g.draw(line);

		g.setStroke(orginalStroke);
		g.setPaint(originalPaint);
	}

	public void drawGateway(String id, int x, int y, int width, int height) {
		Polygon rhombus = new Polygon();
		rhombus.addPoint(x, y + (height / 2));
		rhombus.addPoint(x + (width / 2), y + height);
		rhombus.addPoint(x + width, y + (height / 2));
		rhombus.addPoint(x + (width / 2), y);
		Paint originalPaint = g.getPaint();
		if (haveExecuted(id).equals(EXECUTE_STATUS.ED)) {
			g.setPaint(TASK_COLOR_COMPLETED);
			g.draw(rhombus);
			g.setPaint(originalPaint);
		} else {
			g.draw(rhombus);
		}
		g.setPaint(originalPaint);
	}

	public void drawCollapsedCallActivity(String id, String name, int x, int y,
			int width, int height) {

		Paint originalPaint = g.getPaint();
		if (haveExecuted(id).equals(EXECUTE_STATUS.ING)) {
			g.setPaint(HIGHLIGHT_COLOR);
		} else if (haveExecuted(id).equals(EXECUTE_STATUS.ED)) {
			g.setPaint(TASK_COLOR_COMPLETED);
		} else {
			g.setPaint(TASK_COLOR);
		}
		
		// shape
		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width,
				height, 20, 20);
		g.fill(rect);
		
		g.setPaint(TASK_COLOR);
		Stroke originalStroke = g.getStroke();
	    g.setStroke(CALLACTIVITY_TASK_BORDER_STROKE);
		g.draw(rect);
		g.setStroke(originalStroke);
		g.setPaint(originalPaint);

		// text
		if (name != null) {
			String text = fitTextToWidth(name, width);
			int textX = x + ((width - fontMetrics.stringWidth(text)) / 2);
			int textY = y + ((height - fontMetrics.getHeight()) / 2)
					+ fontMetrics.getHeight();
			Paint originalPaint1 = g.getPaint();
			g.setPaint(Color.BLACK);
			g.drawString(text, textX, textY);
			g.setPaint(originalPaint1);
		}
		
		// FIXME: Change icon
		g.drawImage(SERVICETASK_IMAGE, x + 7, y + 7, ICON_SIZE, ICON_SIZE, null);

	}

	/**
	 * Generates an image of what currently is drawn on the canvas.
	 * 
	 * Throws an {@link ActivitiException} when {@link #close()} is already
	 * called.
	 */
	public byte[] generateImageByteArray(String imageType) {
		if (closed) {
			throw new ActivitiException(
					"ProcessDiagramGenerator already closed");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			// // Try to remove white space
			// minX = (minX <= 5) ? 5 : minX;
			// minY = (minY <= 5) ? 5 : minY;
			BufferedImage imageToSerialize = processDiagram;
			// if (minX >= 0 && minY >= 0) {
			// imageToSerialize = processDiagram.getSubimage(minX - 5, minY - 5,
			// canvasWidth - minX + 5, canvasHeight - minY + 5);
			// }
			ImageIO.write(imageToSerialize, imageType, out);
		} catch (IOException e) {
			throw new ActivitiException("Error while generating process image",
					e);
		} finally {
			IoUtil.closeSilently(out);
		}
		return out.toByteArray();
	}
}
