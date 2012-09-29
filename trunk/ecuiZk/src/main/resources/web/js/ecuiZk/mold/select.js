/**
* Here's the mold file , a mold means a HTML struct that the widget really presented.
* yep, we build html in Javascript , that make it more clear and powerful.
*/
function (out) {

	//Here you call the "this" means the widget instance. (@see Select.js)

	var zcls = this.getZclass(),
		uuid = this.uuid;
	
	this._ecuiId = "ecuiZk-" + uuid;
	
	//The this.domAttrs_() means it will prepare some dom attributes,
	//like the pseudo code below
	/*
		class="${zcls} ${this.getSclass()}" id="${uuid}"
	*/
	/*
	out.push('<span ', this.domAttrs_(), '>');
	out.push(this._text);
	out.push('</span>');
	*/
	out.push("<select ecuiZk='id:", this._ecuiId, ";", this.optToStr_(), "' style='");
	if (this._options["width"]) {
		out.push("width:", this._options["width"], "; ");
	}
	if (this._options["height"]) {
		out.push("height", this._options["height"], "; ");
	}
	out.push("'>")
	for (var i = 0; i < this._items.length; i++) {
		var item = this._items[i];
		out.push("<option value='", item.value, "'");
		if (item.selected) {
			out.push(" selected='" + item.selected + "'");
		}
		out.push(">", item.text, "</option>");
	}
	out.push("</select>");
}