
var Mark = Backbone.Model.extend({
    initialize: function(args, options) {
        // Keep reference to query
        this.query = options.query;
    },
    
    parse: function(response) {
        // this.query.workspace.trigger('query:result', {
            // workspace: this.query.workspace,
            // data: response
        // });
        
        if (!response || !response.ok) {
            Saiku.ui.block(response.result);
            setTimeout( function() {
                Saiku.ui.unblock();
            }, 5000);
        } else { 
            // Show the UI if hidden
            Saiku.ui.unblock();
        }
    },
    
    url: function() {
        return encodeURI(this.query.url() + "/mark");
    }
});
