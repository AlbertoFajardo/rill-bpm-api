/**
 * Dialog for member selections
 */
var DrillthroughModal = Modal.extend({
    type: "drillthrough",
    
    buttons: [
        { text: "Ok", method: "ok" },
        { text: "Cancel", method: "close" }
    ],

    events: {
        'click .collapsed': 'select',
        'click .expand': 'select',
        'click .folder_collapsed': 'select',
        'click .folder_expanded': 'select',
        
        'click .dialog_footer a:' : 'call',
        'click .parent_dimension input' : 'select_dimension',
        'click .measure_tree input' : 'select_measure'
    },

    
    initialize: function(args) {
        // Initialize properties
        _.extend(this, args);
        this.options.title = args.title;
        this.query = args.workspace.query;
        
        this.position = args.position;
        this.action = args.action;
        Saiku.ui.unblock();
        _.bindAll(this, "ok", "drilled");

        // Resize when rendered
        this.bind('open', this.post_render);
        this.render();
               // Load template
       $(this.el).find('.dialog_body')
          .html(_.template($("#template-drillthrough").html())(this));
        // Show dialog
        $(this.el).find('.maxrows').val(this.maxrows);
                    
        var schema = this.query.get('schema');
        var key = this.query.get('connection') + "/" + 
                this.query.get('catalog') + "/"
                + ((schema == "" || schema == null) ? "null" : schema) 
                + "/" + encodeURIComponent(this.query.get('cube'));

        var dimensions = Saiku.session.sessionworkspace.dimensions[key].get('data');
        var measures = Saiku.session.sessionworkspace.measures[key].get('data');

        var container = $("#template-drillthrough-list").html();
        var templ_dim =_.template($("#template-drillthrough-dimensions").html())({dimensions: dimensions});
        var templ_measure =_.template($("#template-drillthrough-measures").html())({measures: measures});

        $(container).appendTo($(this.el).find('.dialog_body'));
        $(this.el).find('.sidebar').height(($("body").height() / 2) + ($("body").height() / 6) );
        $(this.el).find('.sidebar').width(380);

        $(this.el).find('.dimension_tree').html('').append($(templ_dim));
        $(this.el).find('.measure_tree').html('').append($(templ_measure));
        
    },
    
    select: function(event) {
        var $target = $(event.target).hasClass('root')
            ? $(event.target) : $(event.target).parent().find('span');
        if ($target.hasClass('root')) {
            $target.find('a').toggleClass('folder_collapsed').toggleClass('folder_expand');
            $target.toggleClass('collapsed').toggleClass('expand');
            $target.parents('li').find('ul').children('li').toggle();
        }
        
        return false;
    },

    select_dimension: function(event) {
        var $target = $(event.target);
        var checked = $target.is(':checked');
        $target.parent().find('input').attr('checked', checked);
    },

    select_measure: function(event) {
        var $target = $(event.target);
        var checked = $target.is(':checked');
        if(checked) {
            $target.parent().siblings().find('input').attr('checked', false);
        }
    },


    post_render: function(args) {
        $(args.modal.el).parents('.ui-dialog').css({ width: "150px" });
    },
    
    ok: function() {
        // Notify user that updates are in progress
        var $loading = $("<div>Drilling through...</div>");
        $(this.el).find('.dialog_body').children().hide();
        $(this.el).find('.dialog_body').prepend($loading);
        var selections = ""
        $(this.el).find('.check_level:checked').each( function(index) { 
            if (index > 0) {
                selections += ", ";
            }
            selections += $(this).val();
        });

        var maxrows = $(this.el).find('.maxrows').val();
        var params = "?maxrows=" + maxrows;
        params = params + (typeof this.position !== "undefined" ? "&position=" + this.position : "" );
        params += "&returns=" + selections;
        if (this.action == "export") {
            var location = Settings.REST_URL +
                Saiku.session.username + "/query/" + 
                this.query.id + "/drillthrough/export/csv" + params;
            this.close();
            window.open(location);
        } else if (this.action == "table") {
            Saiku.ui.block("Executing drillthrough...");
            this.query.action.get("/drillthrough", { data: { position: this.position, maxrows: maxrows , returns: selections}, success: this.drilled } );
            this.close();
        }
        
        return false;
    },

    drilled: function(model, response) {
        var table = new Table({ workspace: this.workspace });
        table.render({ data: response }, true);

        Saiku.ui.unblock();
        var html = '<div id="fancy_results" class="workspace_results" style="overflow:visible"><table>' + $(table.el).html() + '</table></div>';
        this.remove();
        table.remove();
        $.fancybox(html
            ,
            {
            'autoDimensions'    : false,
            'autoScale'         : false,
            'height'            :  ($("body").height() - 100),
            'width'             :  ($("body").width() - 100),
            'transitionIn'      : 'none',
            'transitionOut'     : 'none'
            }
        );

    },
    
    finished: function() {
        $(this.el).dialog('destroy').remove();
        this.query.run();
    }
});