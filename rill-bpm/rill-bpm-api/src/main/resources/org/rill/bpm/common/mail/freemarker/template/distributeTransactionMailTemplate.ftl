<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
body {margin:0 auto;padding:0;border:0;font-size:12px;line-height:150%;text-align:left;font-family:Arial,'宋体',Verdana,sans-serif}
</style>
</head>
<body>
您好！<br>
<br>
&nbsp;&nbsp;&nbsp;&nbsp;HOST INFO：<#if hostInfo??>${hostInfo?html}<#else>未知</#if><br>
<br>
<br>
&nbsp;&nbsp;&nbsp;&nbsp;产生有分布式事务异常，相关信息如下<br>
<br>
&nbsp;&nbsp;&nbsp;&nbsp;HOST INFO：<#if hostInfo??>${hostInfo?html}<#else>未知</#if><br>
<br>
&nbsp;&nbsp;&nbsp;&nbsp;操作人：<#if operator??>${operator?html}<#else>未知</#if><br>
&nbsp;&nbsp;&nbsp;&nbsp;工作流流程ID：<#if processInstanceId??>${processInstanceId?html}<#else>未知</#if><br>
&nbsp;&nbsp;&nbsp;&nbsp;工作流任务ID：<#if taskInstanceId??>${taskInstanceId?html}<#else>未知</#if><br>
&nbsp;&nbsp;&nbsp;&nbsp;操作类型：<#if workflowOperation??>${workflowOperation?html}<#else>未知</#if><br>
&nbsp;&nbsp;&nbsp;&nbsp;业务对象ID：<#if boId??>${boId?html}<#else>未知</#if><br>
&nbsp;&nbsp;&nbsp;&nbsp;异常Stack：<br>
<#if exception??>${exception?html}<#else>未知</#if><br>
<br>
<br>
此致 <br>
　　敬礼<br>
--<br>
百度公司-盘古项目组 <br>

<font style="font-style: italic;">注：此邮件为系统自动发送，请勿回复。</font><br>
</body>
</html>