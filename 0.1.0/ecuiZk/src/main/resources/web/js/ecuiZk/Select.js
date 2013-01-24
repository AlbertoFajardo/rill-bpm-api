/**
 *
 * Base naming rule:
 * The stuff start with "_" means private , end with "_" means protect ,
 * others mean public.
 *
 * All the member field should be private.
 *
 * Life cycle: (It's very important to know when we bind the event)
 * A widget will do this by order :
 * 1. $init
 * 2. set attributes (setters)
 * 3. rendering mold (@see mold/ecuiZk.js )
 * 4. call bind_ to bind the event to dom .
 *
 * this.deskop will be assigned after super bind_ is called,
 * so we use it to determine whether we need to update view
 * manually in setter or not.
 * If this.desktop exist , means it's after mold rendering.
 *
 */
ecuiZk.Select = zk.$extends(zul.Widget, {
	_text:'', //default value for text attribute
	_ecuiId : "", //ecui控件使用的id
	_options : {}, //ecui控件的options配置
	_items : {},
	_value : "",
	//_selected : 
	
	/**
	 * Don't use array/object as a member field, it's a restriction for ZK object,
	 * it will work like a static , share with all the same Widget class instance.
	 *
	 * if you really need this , assign it in bind_ method to prevent any trouble.
	 *
	 * TODO:check array or object , must be one of them ...I forgot. -_- by Tony
	 */
	
	$define: {
		/**
		 * The member in $define means that it has its own setter/getter.
		 * (It's a coding sugar.)
		 *
		 * If you don't get this ,
		 * you could see the comment below for another way to do this.
		 *
		 * It's more clear.
		 *
		 */
		text: function() { //this function will be called after setText() .
			if(this.desktop) {
				this.$n().innerHTML = this._text;
			}
		}
	},
	/**
	 * If you don't like the way in $define ,
	 * you could do the setter/getter by yourself here.
	 *
	 * Like the example below, they are the same as we mentioned in $define section.
	 */
	/*
	getText:function(){ return this._text; },
	setText:function(val){
		this._text = val;
		if(this.desktop){
		//update the UI here.
		}
	},
	*/
	
	getOptions : function () {
		return this._options;
	},
	
	setOptions : function (val) {
        this._options = val;
    },
    
    getValue : function () {
    	this._value = ecui.get(this._ecuiId).getValue();
    	return this._value;
    },
    
    setValue : function (val) {
    	this._value = val;
    	ecui.get(this._ecuiId).setValue(this._value);
    },
    
    getItems : function () {
    	return this._items;
    },
    
    setItems : function (val) {
    	this._items = val;
    	if (this._ecuiId) {
    		for (var i = 0, list = ecui.get(this._ecuiId).getItems(), o; o = list[i++]; ) {
    			ecui.get(this._ecuiId).remove(o);
            }
    		
    		for (var i = 0; i < this._items.length; i++) {
    			var item = this._items[i];
    			ecui.get(this._ecuiId).add(item.text, item.index, {value : item.value});
    			if (item.selected == "selected") {
    				ecui.get(this._ecuiId).setValue(item.value);
    			}
    		}
    	}
    },
    
    optToStr_ : function () {
    	var options = this._options;
    	var optStr = "type:select;";
    	var item;
    	for (item in options) {
    		optStr += item + ":" + options[item] + ";"
    	}
    	optStr.substring(0, optStr.length - 1);
    	return optStr;
    },
	
	bind_: function () {

		this.$supers(ecuiZk.Select,'bind_', arguments);
		ecui.init(document.body);
		ecui.get(this._ecuiId).onchange = this.doChange_(this);
		
	},

	unbind_: function () {
	
		// A example for domUnlisten_ , should be paired with bind_
		// this.domUnlisten_(this.$n("cave"), "onClick", "_doItemsClick");
		
		/*
		* For widget lifecycle , the super unbind_ should be called
		* as LAST STATEMENT in the function.
		*/
		this.$supers(ecuiZk.Select,'unbind_', arguments);
		ecui.get(this._ecuiId).$dispose();
	},
	
	doChange_ : function (me) {
		return function (evt) {
			//me.$super("doChange_", evt, true);
			me._value = ecui.get(me._ecuiId).getValue();
			me.fire("onChange", {value : me._value});
		};
	},
	
	getZclass: function () {
		return this._zclass != null ? this._zclass: "z-select";
	}
});
