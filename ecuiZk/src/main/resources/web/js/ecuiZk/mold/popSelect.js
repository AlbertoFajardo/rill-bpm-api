/**
* Here's the mold file , a mold means a HTML struct that the widget really presented.
* yep, we build html in Javascript , that make it more clear and powerful.
*/
function (out) {

	//Here you call the "this" means the widget instance. (@see Select.js)

	var zcls = this.getZclass(),
		uuid = this.uuid;
	
	this._ecuiId = "ecuiZk-" + uuid;
	this._popButId = this._ecuiId + "-but";
	this._popPanelId = this._ecuiId + "-panel";
	
	var html;
	for (var i = 0, o; o = this._items[i++]; ){
		var optStr = "value:" + o[0] + ";";
        optStr += "checked:" + ((o[2] === "true" || o[2] === true) ? "true" : "false") + ";";
        optStr += "def-checked:" + ((o[3] === "true" || o[3] === true) ? "true" : "false");
        html += "<span ecui='" + optStr + "'>" + o[1] + "</span>";
	}
	
	var pop = document.createElement("div");
	pop.innerHTML = html;
	pop.setAttribute("ecui", "type:pop-select; id:" + this._popPanelId);
	pop.setAttribute("style", "width:" + this._options.panelWidth);
	
	document.body.appendChild(pop);

	out.push("<span ecui='type:pop-select;id:", this._popButId, ";target:", this._popPanelId, "'");
	this._options.butWidth && (out.push(" style='width:", this._options.butWidth, "'"));
	out.push("></span>");
}