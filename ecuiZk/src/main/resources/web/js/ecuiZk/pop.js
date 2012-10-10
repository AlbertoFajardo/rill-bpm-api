(function () {
    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,
        string = core.string,

        $fastCreate = core.$fastCreate,
        setFocused = core.setFocused,
        createDom = dom.create,
        children = dom.children,
        setStyle = dom.setStyle,
        addClass = dom.addClass,
        moveElements = dom.moveElements,
        getPosition  = dom.getPosition,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,
        getView = util.getView,
        connect = core.$connect,
        blank = util.blan,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_BUTTON = ui.Button,
        UI_BUTTON_CLASS = UI_BUTTON.prototype,
        UI_CUSTOM_CHECKBOXS = ui.CustomCheckboxs,
        UI_CUSTOM_CHECKBOXS_CLASS = UI_CUSTOM_CHECKBOXS.prototype;


    var UI_POP = ui.Pop = 
        inheritsControl(
            UI_CONTROL,
            'ui-pop',
            null,
            function (el, options) {
                var childEle = children(el);
                if (childEle[0].tagName.toLowerCase() == "span") {
                    var parEle = createDom();
                    moveElements(el, parEle, true);
                    el.appendChild(parEle);
                    this._uCheckPanel = $fastCreate(UI_CUSTOM_CHECKBOXS, parEle, this, {});
                }

                var type = this.getTypes()[0],
                    o = createDom(), els;

                el.style.position = 'absolute';

                if (options.noButton !== true) {
                    o.innerHTML = '<div class="'+ type +'-buttons"><div class="ui-button ui-button-g">确定</div><div class="ui-button">取消</div></div>';
                    els = children(o.firstChild);
                    this._uSubmitBtn = $fastCreate(this.Button, els[0], this, {command: 'submit', primary:'ui-button-g'});
                    this._uCancelBtn = $fastCreate(this.Button, els[1], this, {command: 'cancel'});
                    moveElements(o, el, true);
                }
            }
        ),

        UI_POP_CLASS = UI_POP.prototype,

        UI_POP_BTN = UI_POP_CLASS.Button = 
        inheritsControl(
            UI_BUTTON,
            null,
            function (el, options) {
                this._sCommand = options.command;
            }
        ),

        UI_POP_BTN_CLASS = UI_POP_BTN.prototype;

    UI_POP_CLASS.show = function (con, align) {
        var view = getView(),
            h, w,
            pos = getPosition(con.getOuter());

        UI_CONTROL_CLASS.show.call(this);
        this.resize();
        w = this.getWidth();
        h = con.getHeight() + pos.top;
        if (!align && align == 'left') {
            if (pos.left + w > view.right) {
                w = pos.left + con.getWidth() - w;
            }
            else {
                w = pos.left;
            }
        }
        else {
            if (pos.left + con.getWidth() - w < 0) {
                w = pos.left;
            }
            else {
                w = pos.left + con.getWidth() - w;
            }
        }

        if (h + this.getHeight() > view.bottom) {
            h = view.bottom - this.getHeight();
        }

        this.setPosition(w, h);
        setFocused(this);
    };

    UI_POP_CLASS.$resize = function () {
         var el = this._eMain,
            currStyle = el.style;

        currStyle.width = this._sWidth;
        currStyle.height = this._sHeight;
        this.repaint();
    }

    UI_POP_CLASS.init = function () {
        UI_CONTROL_CLASS.init.call(this);
        this.$hide();
    };

    UI_POP_CLASS.$blur = function () {
        this.hide();
        triggerEvent(this, 'cancel');
    };

    UI_POP_CLASS.render = function (data) {
        if (!this._uCheckPanel) {
        	var parEle = createDom();
            moveElements(this.getMain(), parEle, true);
            this.getMain().appendChild(parEle);
            this._uCheckPanel = $fastCreate(UI_CUSTOM_CHECKBOXS, parEle, this, {});
        }
        var items = [];
        for (var i = 0, o; o = data[i++]; ) {
            var item = createDom("", "", "span");
            var optStr = "value:" + o[0] + ";";
            optStr += "checked:" + ((o[2] === "true" || o[2] === true) ? "true" : "false") + ";";
            optStr += "def-checked:" + ((o[3] === "true" || o[3] === true) ? "true" : "false");
            item.setAttribute("ecui", optStr);
            item.innerHTML = o[1];
            items.push(item);
        }
        this._uCheckPanel.setData(items);
        UI_POP_CLASS.$setValue.call(this);
    };

    UI_POP_CLASS.$setValue = function () {
    	if (!this._uCheckPanel) {
        	var parEle = createDom();
            moveElements(this.getMain(), parEle, true);
            this.getMain().appendChild(parEle);
            this._uCheckPanel = $fastCreate(UI_CUSTOM_CHECKBOXS, parEle, this, {});
        }
        var text = this._uCheckPanel.getText(),
            value = this._uCheckPanel.getValue();
        if (value == this._uControl._eInput.value) {
            return ;
        }
        this._uControl._uText.setContent(text.join());
        this._uControl._eInput.value = value.join();
        triggerEvent(this._uControl, "change");
    };

    UI_POP_BTN_CLASS.$click = function () {
        var par = this.getParent();
        UI_BUTTON_CLASS.$click.call(this);
        if (triggerEvent(par, this._sCommand)) {
            par.$blur = blank;
            par.hide();
            delete par.$blur;
        }
        if (par._uCheckPanel && (this._sCommand == "submit")) {
            UI_POP_CLASS.$setValue.call(par);
        }
    };

    var UI_POP_BUTTON = ui.PopButton = 
        inheritsControl(
            UI_BUTTON,
            'ui-pop-button',
            function (el, options) {
                var type = this.getTypes()[0],
                    o = createDom(type + '-icon', 'position:absolute');

                this._sAlign = options.align;
                el.appendChild(o);
                this._sTargetId = options.target;

                if (options.mode == 'text') {
                    this.setClass(this.getPrimary() + '-text');
                }
            }
        ),

        UI_POP_BUTTON_CLASS = UI_POP_BUTTON.prototype;

    UI_POP_BUTTON_CLASS.$click = function () {
        var con;
        UI_BUTTON_CLASS.$click.call(this);
        if (this._sTargetId) {
            con = core.get(this._sTargetId);
            con.show(this, this._sAlign);
        }
    };

    var UI_POP_SELECT = ui.PopSelect =
        inheritsControl(
            UI_CONTROL,
            "ui-pop-select",
            function (el, options) {
                var type = this.getTypes()[0],
                    o = createDom(type + "-text", "overflow:hidden;", "span");
                el.appendChild(o);
                o = createDom(type + "-button", "position:absolute", "span");
                el.appendChild(o);
                o = createDom("", "border:0px none;", "input");
                o.setAttribute("type", "hidden");
                options.name && (o.setAttribute("name", options.name));
                el.appendChild(o);
                setStyle(el, "position", "relative");
                setStyle(el, "display", "inline-block");
                return el;
            },
            function (el, options) {
                el = children(el);
                addClass(el[1], "ui-button");
                this._sTargetId = options.target;
                if (this._sTargetId) {
                    connect(this, function(target) {
                        target._uControl = this;
                        UI_POP_CLASS.$setValue.call(target);
                    }, this._sTargetId);
                }
                this._uText = $fastCreate(UI_CONTROL, el[0], this, {capturable:false});
                this._uButton = $fastCreate(UI_BUTTON, el[1], this, {capturable:false});
                this._eInput = el[2];
            }
        ),
        UI_POP_SELECT_CLASS = UI_POP_SELECT.prototype;

        UI_POP_SELECT_CLASS.$cache = function (style, cacheSize) {
            UI_CONTROL_CLASS.$cache.call(this, style, cacheSize);
            this._uText.cache(false, true);
            this._uButton.cache(false, true);
        };

        UI_POP_SELECT_CLASS.$setSize = function(width, height) {
            UI_CONTROL_CLASS.$setSize.call(this, width, height);
            this.$locate();
            height = this.getBodyHeight();

            this._uText.$setSize(width = this.getBodyWidth() - height, height);

            this._uButton.$setSize(height, height);
            this._uButton.setPosition(width, 0);
        };

        UI_POP_SELECT_CLASS.$click = function () {
            var con;
            UI_BUTTON_CLASS.$click.call(this);
            if (this._sTargetId) {
                con = core.get(this._sTargetId);
                con.show(this, this._sAlign);
            }
        };

        UI_POP_SELECT_CLASS.getValue = function () {
            var con;
            if (this._sTargetId) {
                con = core.get(this._sTargetId);
                return con._uCheckPanel.getValue();
            }
            else return this._eInput.value.split(",");
        };

        UI_POP_SELECT_CLASS.setValue = function (value) {
            var con;
            if (this._sTargetId) {
                con = core.get(this._sTargetId);
                con._uCheckPanel.setValue(value);
                UI_POP_CLASS.$setValue.call(con);
            }
        };
})();
