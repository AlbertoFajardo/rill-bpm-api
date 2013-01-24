/**
 * query-tab
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * path:    custom-checkboxs.js
 * desc:    自定义多选组合
 * author:  cxl(chenxinle@baidu.com)
 * date:    2012/06/29
 */
(function () {
    var core = ecui,
        ui = core.ui,
        dom = core.dom,
        string = core.string,
        util = core.util,

        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,
        getOptions = core.getOptions,
        disposeControl = core.dispose,
        children = dom.children,
        createDom = dom.create,
        moveElements = dom.moveElements,
        trim = string.trim,
        encodeHTML = string.encodeHTML,
        blank = util.blank,
        attachEvent = util.attachEvent,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype;

    function buildTitles(type) {
        var html = ['<div class="'+ type +'-title">'],
            uid = (new Date()).getTime();

        html.push('<input type="radio" name="opt'+ uid +'" value="def" id="optDef'+ uid +'" /><label for="optDef'+ uid +'">默认</label>');
        html.push('<input type="radio" name="opt'+ uid +'" value="all" id="optAll'+ uid +'" /><label for="optAll'+ uid +'">全部</label>');
        html.push('<input type="radio" name="opt'+ uid +'" value="custom" id="optCustom'+ uid +'" /><label for="optCustom'+ uid +'">自定义</label>');
        html.push('</div>');

        return html.join('');
    }

    function attachTitleHandler(items, control) {
        for (var i = 0, item; item = items[i]; i++) {
            attachEvent(item, 'click', (function (type) {
                return function () {
                    control.$changeType(type);
                }
            })(item.value));
            control._aTypeRadio[item.value] = item;
        }
    }

    function attachCheckedHanlder(item, control) {
        attachEvent(item, 'click', function () {
            triggerEvent(control.getParent(), 'itemclick');
        });
    }

    function validDefChecked(control) {
        var res = true, i, item;

        for (i = 0; item = control._aItems[i]; i++) {
            if (item.isDefChecked() != item.isChecked()) {
                res = false;
                break;
            }
        }

        return res;
    }

    var UI_CUSTOM_CHECKBOXS = ui.CustomCheckboxs = inheritsControl(
            UI_CONTROL,
            'ui-custom-checkboxs',
            function (el, options) {
                var o = createDom(),
                    type = this.getTypes()[0],
                    html = [buildTitles(type)];

                html.push('<div class="'+ type +'-items"></div>');
                moveElements(el, o, true);
                el.innerHTML = html.join('');
                moveElements(o, el.lastChild, true);
                options.resizeable = false;
            },
            function (el, options) {
                var childs = children(el),
                    items;

                items = childs[0].getElementsByTagName('input');
                this._aTypeRadio = {};
                attachTitleHandler(items, this);
                this.$setBody(childs[1]);
                this.$initItems(options.data);
            }
        ),
        UI_CUSTOM_CHECKBOXS_CLASS = UI_CUSTOM_CHECKBOXS.prototype,
        UI_CUSTOM_CHECKBOXS_ITEM = UI_CUSTOM_CHECKBOXS_CLASS.Item = inheritsControl(
            UI_CONTROL,
            'ui-custom-checkboxs-item',
            function (el, options) {
                var html = [];

                options.name = options.name || trim(el.innerHTML);
                options.resizeable = false;
                html.push('<input type="checkbox" value="'+ options.value +'" id="opt'+ options.value +'" />');
                html.push('<label for="opt'+  options.value +'">' + options.name + '</label>');
                el.innerHTML = html.join('');
            },
            function (el, options) {
                this._eInput = children(el)[0];
                this._sName = options.name; 
                this._sValue = options.value;
                this._bDefChecked = options.defChecked === true;
                if (options.checked) {
                    this._eInput.checked = true;
                }
                attachCheckedHanlder(this._eInput, this);
            }
        ),
        UI_CUSTOM_CHECKBOXS_ITEM_CLASS = UI_CUSTOM_CHECKBOXS_ITEM.prototype;

    UI_CUSTOM_CHECKBOXS_CLASS.$setSize = blank;

    UI_CUSTOM_CHECKBOXS_CLASS.init = function () {
        UI_CONTROL_CLASS.init.call(this);
        this.$itemclick();
    };

    UI_CUSTOM_CHECKBOXS_CLASS.$changeType = function (type) {
        var i, item;

        if (type == this._sValueType) {
            return;
        }
        this._sValueType = type;

        if (type == 'custom') {
            return;
        }

        for (i = 0; item = this._aItems[i]; i++) {
            if (type == 'all') {
                item.setChecked(true);
            }
            else if (type == 'def') {
                item.setChecked(item.isDefChecked());
            }
        }
    };

    UI_CUSTOM_CHECKBOXS_CLASS.$initItems = function (items) {
        var data = items || children(this.getBody()),
            i, item;

        this._aItems = [];
        for (i = 0; item = data[i]; i++) {
            this.add(item);
        }
    };

    UI_CUSTOM_CHECKBOXS_CLASS.add = function (item) {
        var type = this.getTypes()[0], options;
        
        if (item.nodeName && item.nodeType == 1) {
            item.className = type + '-item';
            options = getOptions(item);
        }
        else {
            options = item;
            item = createDom(type + '-item', '', 'span');
        }
        this.getBody().appendChild(item);
        this._aItems.push($fastCreate(this.Item, item, this, options));
    };

    UI_CUSTOM_CHECKBOXS_CLASS.setData = function (data) {
        var i;
        if (this._aItems && this._aItems.length > 0) {
            i = this._aItems.length - 1;
            while (i >= 0) {
                disposeControl(this._aItems[i--]);
            }
        }
        this.setContent('');
        this.$initItems(data);
        this.$itemclick();
    };

    UI_CUSTOM_CHECKBOXS_CLASS.getValue = function () {
        var i, item, res = [];

        for (i = 0; item = this._aItems[i]; i++) {
            if (item.isChecked()) {
                res.push(item.getValue());
            }
        }

        return res;
    };

    UI_CUSTOM_CHECKBOXS_CLASS.getText = function () {
        var i, item, res = [];

        for (i = 0; item = this._aItems[i]; i++) {
            if (item.isChecked()) {
                res.push(item._sName);
            }
        }

        return res;
    };

    UI_CUSTOM_CHECKBOXS_CLASS.setValue = function (value) {
        var i, item, map = {};

        for (i = 0; item = value[i]; i++) {
            map[item] = true;
        }

        for (i = 0; item = this._aItems[i]; i++) {
            if (map[item.getValue()]) {
                item.setChecked(true);
            }
            else {
                item.setChecked(false);
            }
        }

        this.$itemclick();
    };

    UI_CUSTOM_CHECKBOXS_CLASS.setValueType = function (type) {
        this.$changeType(type);
    };

    UI_CUSTOM_CHECKBOXS_CLASS.getValueType = function () {
        return this._sValueType;
    };

    UI_CUSTOM_CHECKBOXS_CLASS.$itemclick = function () {
        var value = this.getValue();

        if (value.length == this._aItems.length) {
            this._aTypeRadio.all.checked = true;
            this._sValueType = 'all';
        }
        else if (validDefChecked(this)) {
            this._aTypeRadio.def.checked = true;
            this._sValueType = 'def';
        }
        else {
            this._aTypeRadio.custom.checked = true;
            this._sValueType = 'custom';
        }
    };

    UI_CUSTOM_CHECKBOXS_ITEM_CLASS.$setSize = blank;

    UI_CUSTOM_CHECKBOXS_ITEM_CLASS.getValue = function () {
        return this._sValue;
    };

    UI_CUSTOM_CHECKBOXS_ITEM_CLASS.setChecked = function (checked) {
        this._eInput.checked = checked;
    };

    UI_CUSTOM_CHECKBOXS_ITEM_CLASS.isChecked = function () {
        return this._eInput.checked;
    };

    UI_CUSTOM_CHECKBOXS_ITEM_CLASS.isDefChecked = function () {
        return this._bDefChecked;
    };
})();
