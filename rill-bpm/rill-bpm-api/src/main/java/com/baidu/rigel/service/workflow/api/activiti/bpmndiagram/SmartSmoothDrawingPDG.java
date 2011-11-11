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
package com.baidu.rigel.service.workflow.api.activiti.bpmndiagram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import com.baidu.rigel.service.workflow.api.activiti.bpmndiagram.ProcessMonitorChartInfoHelper.ChartInfo;
/**
 *
 * Draw2D API to draw anchors.
 *
 * @author mengran
 */
public class SmartSmoothDrawingPDG {

  protected static final Map<String, ActivityDrawInstruction> activityDrawInstructions = new HashMap<String, ActivityDrawInstruction>();

  // The instructions on how to draw a certain construct is
  // created statically and stored in a map for performance.
  static {
    // start event
    activityDrawInstructions.put("startEvent", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawStartEvent(activityImpl.getX(), activityImpl.getY(),
                activityImpl.getWidth(), activityImpl.getHeight(), null, activityImpl);
      }
    });

    // start timer event
    activityDrawInstructions.put("startTimerEvent", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawTimerStartEvent(activityImpl.getX(), activityImpl.getY(),
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // end event
    activityDrawInstructions.put("endEvent", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawNoneEndEvent(activityImpl.getX(), activityImpl.getY(),
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // error end event
    activityDrawInstructions.put("errorEndEvent", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawErrorEndEvent(activityImpl.getX(), activityImpl.getY(),
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });


    // task
    activityDrawInstructions.put("task", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // user task
    activityDrawInstructions.put("userTask", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawUserTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight(), activityImpl);
      }
    });

    // script task
    activityDrawInstructions.put("scriptTask", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawScriptTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // service task
    activityDrawInstructions.put("serviceTask", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawServiceTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // receive task
    activityDrawInstructions.put("receiveTask", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawReceiveTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // send task
    activityDrawInstructions.put("sendTask", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawSendTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // manual task
    activityDrawInstructions.put("manualTask", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawManualTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
                activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // exclusive gateway
    activityDrawInstructions.put("exclusiveGateway", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawExclusiveGateway(activityImpl.getId(), activityImpl.getX(), activityImpl.getY(),
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // parallel gateway
    activityDrawInstructions.put("parallelGateway", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawParallelGateway(activityImpl.getId(), activityImpl.getX(), activityImpl.getY(),
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // Boundary timer
    activityDrawInstructions.put("boundaryTimer", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawCatchingTimerEvent(activityImpl.getX(), activityImpl.getY(),
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // Boundary catch error
    activityDrawInstructions.put("boundaryError", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawCatchingErroEvent(activityImpl.getX(), activityImpl.getY(),
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // timer catch event
    activityDrawInstructions.put("intermediateTimer", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawCatchingTimerEvent(activityImpl.getX(), activityImpl.getY(),
                activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

    // subprocess
    activityDrawInstructions.put("subProcess", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        Boolean isExpanded = (Boolean) activityImpl.getProperty(BpmnParse.PROPERTYNAME_ISEXPANDED);
        if (isExpanded != null && isExpanded == false) {
          processDiagramCreator.drawCollapsedSubProcess((String) activityImpl.getProperty("name"),
                  activityImpl.getX(), activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
        } else {
          processDiagramCreator.drawExpandedSubProcess((String) activityImpl.getProperty("name"),
                  activityImpl.getX(), activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
        }
      }
    });

    // call activity
    activityDrawInstructions.put("callActivity", new ActivityDrawInstruction() {
      public void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl) {
        processDiagramCreator.drawCollapsedCallActivity(activityImpl.getId(), (String) activityImpl.getProperty("name"),
                  activityImpl.getX(), activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
      }
    });

  }

  /**
   *  Generates a PNG diagram image of the given process definition,
   *  using the diagram interchange information of the process.
   */
  public static ProcessMonitorChartInfoHelper.ChartInfo generatePngDiagram(ProcessDefinitionEntity processDefinition) {
    return generateDiagram(processDefinition, "png", Collections.<String>emptyList());
  }

  /**
   *  Generates a JPG diagram image of the given process definition,
   *  using the diagram interchange information of the process.
   */
  public static ProcessMonitorChartInfoHelper.ChartInfo generateJpgDiagram(ProcessDefinitionEntity processDefinition) {
    return generateDiagram(processDefinition, "jpg", Collections.<String>emptyList());
  }

  protected static SmartSmoothDrawingPDC generateDiagram(ProcessDefinitionEntity processDefinition, List<String> highLightedActivities, Map<String, String> takedTransitions) {
    SmartSmoothDrawingPDC SmartSmoothDrawingPDC = initSmartSmoothDrawingPDC(processDefinition).setTakeTransitions(takedTransitions);
    for (ActivityImpl activity : processDefinition.getActivities()) {
     drawActivity(SmartSmoothDrawingPDC, activity, highLightedActivities);

    }
    return SmartSmoothDrawingPDC;
  }

  public static ProcessMonitorChartInfoHelper.ChartInfo generateDiagram(ProcessDefinitionEntity processDefinition, String imageType, List<String> highLightedActivities) {

      return generateDiagram(processDefinition, imageType, highLightedActivities, Collections.<String, String>emptyMap());
  }

  public static ProcessMonitorChartInfoHelper.ChartInfo generateDiagram(ProcessDefinitionEntity processDefinition, String imageType, List<String> highLightedActivities, Map<String, String> takedTransitions) {

      SmartSmoothDrawingPDC smartSmoothDrawingPDC = generateDiagram(processDefinition, highLightedActivities, takedTransitions);
      return new ChartInfo().setDiagramBytes(smartSmoothDrawingPDC.generateImageByteArray(imageType))
              .setTaskDefinitionKeyPosition(smartSmoothDrawingPDC.getTaskDefinitionKeyPosition()).setTaskDefinitionKeyType(smartSmoothDrawingPDC.getTaskDefinitionKeyType());
      
  }

  protected static void drawActivity(SmartSmoothDrawingPDC smartSmoothDrawingPDC, ActivityImpl activity, List<String> highLightedActivities) {
    String type = (String) activity.getProperty("type");
    ActivityDrawInstruction drawInstruction = activityDrawInstructions.get(type);
    if (drawInstruction != null) {

      drawInstruction.draw(smartSmoothDrawingPDC, activity);

      // Gather info on the multi instance marker
      boolean multiInstanceSequential = false, multiInstanceParallel = false, collapsed = false;
      String multiInstance = (String) activity.getProperty("multiInstance");
      if (multiInstance != null) {
        if ("sequential".equals(multiInstance)) {
          multiInstanceSequential = true;
        } else {
          multiInstanceParallel = true;
        }
      }

      // Gather info on the collapsed marker
      Boolean expanded = (Boolean) activity.getProperty(BpmnParse.PROPERTYNAME_ISEXPANDED);
      if (expanded != null) {
        collapsed = !expanded;
      }

      // Actually draw the markers
      smartSmoothDrawingPDC.drawActivityMarkers(activity.getX(), activity.getY(), activity.getWidth(),
              activity.getHeight(), multiInstanceSequential, multiInstanceParallel, collapsed);

      // Draw highlighted activities
      if (highLightedActivities.contains(activity.getId())) {
          drawHighLight(smartSmoothDrawingPDC, activity);
      }

      // Reject activity position infos to PDC
      smartSmoothDrawingPDC.addTaskDefinitionKeyPosition(activity.getId(), new Integer[] {activity.getX(), activity.getY(), activity.getWidth(),activity.getHeight()});
      smartSmoothDrawingPDC.addTaskDefinitionKeyType(activity.getId(), type);

    }

    // Outgoing transitions of activity
    for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
      // Draw2D implementation
      List<Integer> waypoints = centerConnection(sequenceFlow);
      for (int i=2; i < waypoints.size(); i+=2) { // waypoints.size() minimally 4: x1, y1, x2, y2
        boolean drawConditionalIndicator = (i == 2)
          && sequenceFlow.getProperty(BpmnParse.PROPERTYNAME_CONDITION) != null
          && !((String) activity.getProperty("type")).toLowerCase().contains("gateway");
        if (i < waypoints.size() - 2) {
          smartSmoothDrawingPDC.drawSequenceflowWithoutArrow(waypoints.get(i-2), waypoints.get(i-1),
                waypoints.get(i), waypoints.get(i+1), drawConditionalIndicator, sequenceFlow);
        } else {
          smartSmoothDrawingPDC.drawSequenceflow(waypoints.get(i-2), waypoints.get(i-1),
                  waypoints.get(i), waypoints.get(i+1), drawConditionalIndicator, sequenceFlow);
        }
      }
    }

    // Nested activities (boundary events)
    for (ActivityImpl nestedActivity : activity.getActivities()) {
      drawActivity(smartSmoothDrawingPDC, nestedActivity, highLightedActivities);
    }
  }

    private static List<Integer> centerConnection(PvmTransition sequenceFlow) {

        List<Integer> waypoints = ((TransitionImpl) sequenceFlow).getWaypoints();

        int x1, y1, x2, y2, w1, h1, w2, h2;
//        ActivityImpl source = (ActivityImpl) sequenceFlow.getSource();
//        ActivityImpl dest = (ActivityImpl) sequenceFlow.getDestination();
        x1 = ((ActivityImpl) sequenceFlow.getSource()).getX();
        x2 = ((ActivityImpl) sequenceFlow.getDestination()).getX();
        y1 = ((ActivityImpl) sequenceFlow.getSource()).getY();
        y2 = ((ActivityImpl) sequenceFlow.getDestination()).getY();
        w1 = ((ActivityImpl) sequenceFlow.getSource()).getWidth();
        h1 = ((ActivityImpl) sequenceFlow.getSource()).getHeight();
        w2 = ((ActivityImpl) sequenceFlow.getDestination()).getWidth();
        h2 = ((ActivityImpl) sequenceFlow.getDestination()).getHeight();
        Point sourceCenter = new Point(x1 + w1/2, y1 + h1/2);
        Point destCenter = new Point(x2 + w2/2, y2 + h2/2);

        List<Integer> centerConnectWaypoints = new ArrayList<Integer>(4);
        // x as center, y's position
        if (waypoints.size() == 4) {
            Ellipse ellipse = new Ellipse();
            ellipse.setBounds(new Rectangle(x1, y1, w1, h1));
            ChopboxAnchor anchor = new ChopboxAnchor(ellipse);
            Point sourceAnchorPoint = anchor.getLocation(destCenter);
            centerConnectWaypoints.add(sourceAnchorPoint.x);
            centerConnectWaypoints.add(sourceAnchorPoint.y);
            ellipse.setBounds(new Rectangle(x2, y2, w2, h2));
            Point destAnchorPoint = anchor.getLocation(sourceCenter);
            centerConnectWaypoints.add(destAnchorPoint.x);
            centerConnectWaypoints.add(destAnchorPoint.y);
            return centerConnectWaypoints;
        } else {
            // Turning point
            int xt = waypoints.get(2);
            int yt = waypoints.get(3);
            Point turningPoint = new Point(xt, yt);
            Ellipse ellipse = new Ellipse();
            ellipse.setBounds(new Rectangle(x1, y1, w1, h1));
            ChopboxAnchor anchor = new ChopboxAnchor(ellipse);
            Point sourceAnchorPoint = anchor.getLocation(turningPoint);
            centerConnectWaypoints.add(sourceAnchorPoint.x);
            centerConnectWaypoints.add(sourceAnchorPoint.y);
            Point lastTurningPoint = null;
            for (int i = 2; i < waypoints.size() - 2; i += 2) {
                centerConnectWaypoints.add(waypoints.get(i));
                centerConnectWaypoints.add(waypoints.get(i + 1));
                lastTurningPoint = new Point(waypoints.get(i).intValue(), waypoints.get(i + 1).intValue());
            }
            ellipse.setBounds(new Rectangle(x2, y2, w2, h2));
            Point destAnchorPoint = anchor.getLocation(lastTurningPoint);
            centerConnectWaypoints.add(destAnchorPoint.x);
            centerConnectWaypoints.add(destAnchorPoint.y);
            return centerConnectWaypoints;
        }
  }

  private static void drawHighLight(SmartSmoothDrawingPDC SmartSmoothDrawingPDC, ActivityImpl activity) {
      SmartSmoothDrawingPDC.drawHighLight(activity.getX(), activity.getY(), activity.getWidth(), activity.getHeight());

  }

    protected static SmartSmoothDrawingPDC initSmartSmoothDrawingPDC(ProcessDefinitionEntity processDefinition) {
    int minX = Integer.MAX_VALUE;
    int maxX = 0;
    int minY = Integer.MAX_VALUE;
    int maxY = 0;

    for (ActivityImpl activity : processDefinition.getActivities()) {

      // width
      if (activity.getX() + activity.getWidth() > maxX) {
        maxX = activity.getX() + activity.getWidth();
      }
      if (activity.getX() < minX) {
        minX = activity.getX();
      }
      // height
      if (activity.getY() + activity.getHeight() > maxY) {
        maxY = activity.getY() + activity.getHeight();
      }
      if (activity.getY() < minY) {
        minY = activity.getY();
      }

      for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
        List<Integer> waypoints = ((TransitionImpl) sequenceFlow).getWaypoints();
        for (int i=0; i < waypoints.size(); i+=2) {
          // width
          if (waypoints.get(i) > maxX) {
            maxX = waypoints.get(i);
          }
          if (waypoints.get(i) < minX) {
            minX = waypoints.get(i);
          }
          // height
          if (waypoints.get(i+1) > maxY) {
            maxY = waypoints.get(i+1);
          }
          if (waypoints.get(i+1) < minY) {
            minY = waypoints.get(i+1);
          }
        }
      }
    }
    return new SmartSmoothDrawingPDC(maxX + 10, maxY + 10, minX, minY);
  }

  protected interface ActivityDrawInstruction {

    void draw(SmartSmoothDrawingPDC processDiagramCreator, ActivityImpl activityImpl);

  }

}

