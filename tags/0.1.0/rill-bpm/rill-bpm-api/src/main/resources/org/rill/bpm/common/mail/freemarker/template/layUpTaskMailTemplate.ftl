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
&nbsp;&nbsp;&nbsp;&nbsp;任务(任务名：${taskName?html}，所属订单编号：${orderCode?html})被暂时搁置。<br>
&nbsp;&nbsp;&nbsp;&nbsp;搁置原因：<#if layUpReason??>${layUpReason?html}<#else>原因未知。</#if> 
<br>
<br>
　　您可登录系统<a href="http://pangu.baidu.com/pg-support/workflow/layUpTaskSearchInit.action?layUpTaskFinder.code=${orderCode?html}">查看</a>详细情况。<br>
<br>
此致 <br>
　　敬礼<br>
--<br>
百度公司-轩辕项目组 <br>

<font style="font-style: italic;">注：此邮件为系统自动发送，请勿回复。</font><br>
</body>
</html>