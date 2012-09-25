<%--
	Here you could do any styling job you want , all CSS stuff.
--%>
<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>

.z-select{
     color:black;
}

.ui-button {display:inline-block;font-size:12px;line-height:18px;padding:0px 12px;margin-right:6px;overflow:hidden;cursor:pointer;text-decoration:none;vertical-align:middle;-moz-user-select:none;-webkit-user-select: none;border-radius:2px;-webkit-border-radius:2px;-o-border-radius:2px;-moz-border-radius:2px;-ms-border-radius:2px;*display:inline;*zoom:1}
.ui-button {background:url(../../../js/ecuiZk/img/ecui-bg.png) 0px -114px repeat-x;border:1px solid #ACB9C1;color:#010000}.ui-button-hover {background-position:0px -132px;color:#010000}
.ui-button-active {background-position:0px -150px;color:#010000}

.ui-button-g {background:url(../../../js/ecuiZk/img/ecui-bg.png) 0px -60px repeat-x;border:1px solid #68A500;color:#FFF}
.ui-button-g-hover {background-position:0px -78px;color:#FFF}
.ui-button-g-active {background-position:0px -96px;color:#FFF}

.ui-button-disabled {background:#EFF3F6;color:#9CA9AF;border-color:#D2D6D9;cursor:default}

input.ui-button:hover {background-position:0px -132px;color:#010000}
input.ui-button:active {background-position:0px -150px;color:#010000}
input.ui-button-g:hover {background-position:0px -78px;color:#FFF}
input.ui-button-g:active {background-position:0px -96px;color:#FFF}

.ui-label {cursor:default;color:#000}

.ui-input {position:relative;height:20px;width:160px;border:1px solid #A9ADB6;background-color:#FFF;display:inline-block;vertical-align:middle;overflow:hidden;*display:inline;*zoom:1}
.ui-input input, .ui-input textarea {width:100%;height:100%;border:0px;border-top:1px solid #F0F0F0;line-height:19px;font-size:12px;resize:none}
.ui-textarea {height:5em;width:200px}
.ui-input textarea {*position:absolute} /* Hack IE6/7下 textarea高度设置100%...原因未知... */
.ui-input-tip, .ui-textarea-tip{position:absolute;top:1px;left:3px;line-height:18px;color:#CCC}

.ui-vscrollbar {width:16px;background:#F0F0F0;padding:16px 0px}
.ui-vscrollbar .ui-button {padding:0px;margin:0px;cursor:default;border-radius:0px;-webkit-border-radius:0px;-o-border-radius:0px;-moz-border-radius:0px;-ms-border-radius:0px}
.ui-vscrollbar .ui-button-hover {padding:0px;margin:0px;cursor:default;border-radius:0px;-webkit-border-radius:0px;-o-border-radius:0px;-moz-border-radius:0px;-ms-border-radius:0px}
.ui-vscrollbar-prev {width:16px;height:16px;border:0px;background:url(../../../js/ecuiZk/img/ecui.png) 0px -222px no-repeat}
.ui-vscrollbar-next {width:16px;height:16px;border:0px;background:url(../../../js/ecuiZk/img/ecui.png) 0px -202px no-repeat}
.ui-vscrollbar .ui-scrollbar-thumb {width:16px;background:#EDF0F6}

.ui-hscrollbar {height:15px;background:#F0F0F0;padding:0px 15px}
.ui-hscrollbar .ui-button {padding:0px;margin:0px;cursor:default;border-radius:0px;-webkit-border-radius:0px;-o-border-radius:0px;-moz-border-radius:0px;-ms-border-radius:0px}
.ui-hscrollbar .ui-button-hover {padding:0px;margin:0px;cursor:default;border-radius:0px;-webkit-border-radius:0px;-o-border-radius:0px;-moz-border-radius:0px;-ms-border-radius:0px}
.ui-hscrollbar-prev {width:15px;height:15px;border:0px;background:url(../../../js/ecuiZk/img/ecui.png) -2px -281px no-repeat}
.ui-hscrollbar-next {width:15px;height:15px;border:0px;background:url(../../../js/ecuiZk/img/ecui.png) -22px -282px no-repeat}
.ui-hscrollbar .ui-scrollbar-thumb {height:15px;background:#EDF0F6}

.ui-select {height:20px;display:inline-block;vertical-align:middle;border:1px solid #A8ADB6;background:#FFF;width:100px;*display:inline;zoom:1}
.ui-select-disabled {color:#CCC}
.ui-select-text {display:block;padding-left:4px;line-height:18px;border-top:1px solid #F0F0F0;cursor:default}
.ui-select-button {background:url(../../../js/ecuiZk/img/ecui.png) 4px -200px no-repeat;padding:0px;margin:0px;border:0px;border-radius:0px;-webkit-border-radius:0px;-o-border-radius:0px;-moz-border-radius:0px;-ms-border-radius:0px;}
.ui-select-options {border:1px solid #A8ADB6;background:#FFF}
.ui-select-item {padding-left:4px;cursor:default;-moz-user-select:none;-webkit-user-select:none;}
.ui-select-item-hover, .ui-select-item-focus {background:#E4EAFA}

.ui-checkbox {width:13px;height:13px;overflow:hidden;display:inline-block;margin:0px 3px;background:url(../../../js/ecuiZk/img/ecui.png) 0px 0px no-repeat;vertical-align:middle;*display:inline;zoom:1;}
.ui-checkbox-hover {background-position:-17px 0px}
.ui-checkbox-checked {background-position:-34px -0px}
.ui-checkbox-checked-hover {background-position:-51px -0px}
.ui-checkbox-part {background-position:-102px -0px}
.ui-checkbox-part-hover {background-position:-51px -0px}
.ui-checkbox-disabled {background-position:-68px -0px}
.ui-checkbox-checked-disabled {background-position:-85px -0px}

.ui-radio {width:12px;height:12px;overflow:hidden;display:inline-block;margin:0px 0px;background:url(../../../js/ecuiZk/img/ecui.png) 0px -18px no-repeat;vertical-align:middle;*display:inline;zoom:1;}
.ui-radio-hover {background-position:-17px -18px}
.ui-radio-checked {background-position:-34px -18px}
.ui-radio-checked-hover {background-position:-51px -18px}
.ui-radio-disabled {background-position:-68px -18px}
.ui-radio-checked-disabled {background-position:-85px -18px}

.ui-table {border:0px;background:#FFF;border-left:1px solid #E6E6E6}
.ui-table-head, .ui-table-locked-head{background:url(../../../js/ecuiZk/img/ecui-bg.png) 0px 0px repeat-x;background-color:#FFF;overflow:hidden;border-bottom:1px solid #E6E6E6;border-top:1px solid #E6E6E6}
.ui-table-hcell {font-weight:normal;text-align:center;padding:5px 3px;color:#3C75A2;cursor:default;border-right:1px solid #E6E6E6;border-bottom:1px solid #E6E6E6;vertical-align:middle}
.ui-table-fill {padding:0px;border-right:1px solid #E6E6E6}
.ui-table-cell {border-bottom:1px solid #E6E6E6;border-right:1px solid #E6E6E6;line-height:30px;padding:0px 3px;vertical-align:middle}
.ui-table-cell .ui-checkbox {*margin-top:6px}
.ui-table-expend-cell .ui-checkbox {*margin-top:11px}
.ui-table-cell-align-right {text-align:right}
.ui-table-cell-align-center {text-align:center}
.ui-table-row-focus {background:#E6F4FF}
.ui-table-row-hover {background:#F6FAFB}
.ui-table-row-highlight {background:#FEFACA}
.ui-table-head-shadow {height:5px;overflow:hidden;background:#CCC;filter:alpha(opacity=50);opacity:0.5}
.ui-table-hcell-sort {background:url(../../../js/ecuiZk/img/ecui.png) right -240px no-repeat}
.ui-table-hcell-sort-asc {background:url(../../../js/ecuiZk/img/ecui.png) right -300px no-repeat}
.ui-table-hcell-sort-desc {background:url(../../../js/ecuiZk/img/ecui.png) right -360px no-repeat}
.ui-table-hcell-filter {background:url(../../../js/ecuiZk/img/ecui.png) right 0px no-repeat}
.ui-table-hcell-filter-hover {background-position: right -60px}
.ui-table-hcell-filter-active {background-position: right -120px}
.ui-table-panel-empty {height:40px;line-height:40px}
.ui-table-empty {height:40px;line-height:40px;text-align:center;font-weight:bold}
.ui-table-expend-layer {background:#FFF;overflow:auto}

.ui-treeview, .ui-check-tree, .ui-account-tree-view {padding-left:15px;font-size:12px;height:20px;margin-bottom:3px;cursor:default}
.ui-treeview label, .ui-check-tree label, .ui-account-tree-view label {margin:0px;padding:0px}
.ui-treeview-expanded, .ui-check-tree-expanded, .ui-account-tree-view-expanded {background:url(../../../js/ecuiZk/img/ecui.png) 2px -35px no-repeat}
.ui-treeview-collapsed, .ui-check-tree-collapsed, .ui-account-tree-view-collapsed {background:url(../../../js/ecuiZk/img/ecui.png) 2px -95px no-repeat}
.ui-treeview-children, .ui-check-tree-children, .ui-account-tree-view-children {margin-left:15px}
.ui-treeview-selected, .ui-check-tree-selected, .ui-account-tree-view-selected {background-color:#E9E9E9;font-weight:bold}

.ui-calendar {position:relative;width:160px;height:20px;border:1px solid #A9ADB6;background-color:#FFF;display:inline-block;vertical-align:middle;*display:inline;zoom:1}
.ui-calendar-text {display:block;height:19px;border-top:1px solid #F0F0F0;line-height:18px;font-size:12px;padding-left:5px;margin-right:40px}
.ui-calendar-button {width:40px;height:20px;position:absolute;right:0px;top:0px;background:url(../../../js/ecuiZk/img/ecui.png) 0px -160px}
.ui-calendar-cancel {position:absolute;top:1px;right:45px;height:19px;width:8px;background:url(../../../js/ecuiZk/img/ecui.png) -77px -160px}
.ui-calendar-default {color:#979797}
.ui-calendar-panel {border:1px solid #C0CCDC;background:#FFF;width:225px;z-index:65535}
.ui-calendar-panel .ui-button {padding:0px;margin:0px;border:0px;background-image:url(../../../js/ecuiZk/img/ecui.png);border-radius:0px;-webkit-border-radius:0px;-o-border-radius:0px;-moz-border-radius:0px;-ms-border-radius:0px}
.ui-calendar-panel-buttons {height:25px;border:1px solid #F5F6FA;background:url(../../../js/ecuiZk/img/ecui-bg.png) 0px -168px;padding:3px 10px 0px}
.ui-calendar-panel .ui-calendar-panel-btn-prv {width:18px;height:18px;border:1px solid #AAADB6;background-position:-39px -160px;vertical-align:middle}
.ui-calendar-panel .ui-calendar-panel-btn-nxt {width:18px;height:18px;border:1px solid #AAADB6;background-position:-57px -160px;vertical-align:middle}
.ui-calendar-panel-slt-year {width:65px;margin-right:10px;margin-left:15px;vertical-align:middle}
.ui-calendar-panel-slt-month {width:45px;margin-right:20px;vertical-align:middle}
.ui-calendar-panel-month-view {padding:0px 2px 2px;border-top:1px solid #C0CCDC;cursor:default}
.ui-calendar-panel-month-view table {margin:0 auto;border-collapse:separate}
.ui-calendar-panel-month-view table td {width:26px;height:30px;text-align:center;line-height:20px;cursor:default;border:1px solid #FFF;margin:5px}
.ui-calendar-panel-month-view .ui-monthview-title {font-weight:bold}
.ui-calendar-panel-month-view .ui-monthview-item-hover {background-color:#DCEDC3;border-color:#C2D79C}
.ui-calendar-panel-month-view .ui-monthview-item-selected {border-color:#5F9303;background-color:#7BBE0E;color:#FFF;font-weight:bold}
.ui-calendar-panel-month-view .ui-monthview-item-disabled {color:#B9BDC6}

.ui-multi-calendar {position:relative;width:212px;height:20px;border:1px solid #A9ADB6;background-color:#FFF;display:inline-block;vertical-align:middle;*display:inline;zoom}
.ui-multi-calendar ul {list-style:none}
.ui-multi-calendar-text {height:19px;border-top:1px solid #F1F1F1;line-height:18px;font-size:12px;padding-left:5px;margin-right:40px}
.ui-multi-calendar-button {width:40px;height:20px;position:absolute;right:0px;top:0px;background:url(../../../js/ecuiZk/img/ecui.png) 0px -160px}
.ui-multi-calendar-cancel {position:absolute;top:1px;right:45px;height:19px;width:8px;background:url(../../../js/ecuiZk/img/ecui.png) -77px -160px}
.ui-multi-calendar-default {color:#979797}
.ui-multi-calendar-layer {border:1px solid #7D889A;border-top-width:2px;padding:15px 0px 0px 10px;background-color:#FFF}
.ui-multi-calendar-layer-text strong {color:#104A78}
.ui-multi-calendar-layer-cal-area {display:inline-block;margin-right:10px;*display:inline;zoom:1}
.ui-multi-calendar-layer-buttons  {margin-top:10px;padding-bottom:5px}

.ui-pop {position:absolute;border:1px solid #7D889A;border-top-width:5px;background-color:#FFF;z-index:1;padding:10px}
.ui-pop-buttons {padding-top:5px;clear:both}
.ui-pop-button {position:relative;padding-right:20px;}
.ui-pop-button-text {border:0px;background:none}
.ui-pop-button .ui-pop-button-icon {position:absolute;height:18px;width:11px;top:0px;right:4px;background:url(../../../js/ecuiZk/img/ecui.png) -17px -200px}
.ui-pop-button-disabled .ui-pop-button-icon {background-position:-17px -218px}