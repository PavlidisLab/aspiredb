/**
 * @author Gregor Biering
 * @author  (http://kalmatrongeorgia.ge/dwrExtjs4Store/DwrProxy.js) first runnable version
 * @author Sencha
 * @class Ext.ux.DwrProxy
 * @extends Ext.data.proxy.Proxy
 *
 * CRUD-Proxy for DWR 3.0. Coded based on the idea of http://kalmatrongeorgia.ge/dwrExtjs4Store/DwrProxy.js
 * and the @class Ext.data.proxy.Server, Ext.data.proxy.AjaxProxy,Ext.data.proxy.RestProxy
 *
 */
Ext.define('Ext.ux.DwrProxy', {
    requires : [ 'Ext.util.MixedCollection' , 'Ext.data.Request' ],
    extend : 'Ext.data.proxy.Proxy',
    alias : 'proxy.dwr',
    alternateClassName : [ 'Ext.data.dwrProxy' ],
    /**
     * @cfg {Boolean} passDwrStoreParams
     * Send store parameter like "limit","start","page" to Dwr
     */
    passDwrStoreParams : false,
    appendId: true,
    timeout : undefined,
    constructor : function(config) {
        var me = this;

        config = config || {};

        this.addEvents('exception');
        me.callParent([config]);

        /**
         * @cfg {Object} extraParams
         * Extra parameters that will be included on every request. Individual requests with params of the same name
         * will override these params when they are in conflict.
         */
        me.extraParams = config.extraParams || {};
    },
    create : function (){
        return this.doRequest.apply(this, arguments);
    },
    read : function (){
        return this.doRequest.apply(this, arguments);
    },
    update : function (){
        return this.doRequest.apply(this, arguments);
    },
    destroy : function (){
        return this.doRequest.apply(this, arguments);
    },
    doRequest : function(operation, callback, scope) {
        var me = this, writer = this.getWriter();
        request = this.buildRequest(operation, callback, scope);

        if (operation.allowWrite()) {
            request = writer.write(request);
        }

        // creating param list that is going to be sent to Dwr.
        // and adding the request.param as the first parameter
        if (this.passDwrStoreParams)
            var dwrParams = [ request.params ];
        else if(operation.action !== 'read'){
            if(Ext.isArray(request.jsonData))
                var dwrParams = [ request.jsonData ];
            else
                var dwrParams = [ [ request.jsonData ] ];
        } else
            var dwrParams = []

        // adding parameters if there are defined any in proxy
        // configuration
        if (typeof (me.dwrParams) === 'object') {
            dwrParams = dwrParams.concat(me.dwrParams);
        }

        dwrParams.push({
            callback : function(x) {
                me.processResponse(true, operation, request, x , callback, scope);
            },
            scope : scope,
            timeout : this.timeout,
            errorHandler : function (message) {
                if(console != undefined) {
                    console.log(message);
                } else if(windows != undefined) {
                    if(windows.console != undefined)
                        windows.console.log(message);
                }

                me.processResponse(false, operation, request, message , callback, scope);
            }
        });

        // /making a call
        if(Ext.isFunction(me.dwrFunction)) {
            me.dwrFunction.apply(null, dwrParams);
        } else {
            switch (operation.action) {
                case 'read':
                    me.dwrFunction.read.apply(null, dwrParams0);
                    break;
                case 'create':
                    me.dwrFunction.create.apply(null, dwrParams);
                    break;
                case 'update':
                    me.dwrFunction.create.apply(null, dwrParams);
                    break;
                case 'destroy':
                    me.dwrFunction.delete.apply(null, dwrParams);
                    break;
                default:
                    me.processResponse(false, operation, request, 'operation "' +  operation.action + '" not available', callback, scope);
            }
        }

        return request;
    },
    processResponse : function(success, operation, request, response, callback, scope) {
        var me = this, reader, result = {}, records, length, mc, record;
        console.log('request');
        if (success === true) {
            reader = me.getReader();
            var data = {};
            // there's currently no total count in dwr response
            // awaiting currently all available results
            response = response || [];
            data[reader.totalProperty] = response.length;
            // there's to hierarchy in dwr response response == data
            data[reader.root] = response;

            result = reader.readRecords(data);

            if (result.success !== false) {
                // see comment in buildRequest for why we include the response
                // object here
                Ext.apply(operation, {
                    response : response,
                    resultSet : result
                });

                operation.commitRecords(result.records);
                operation.setCompleted();
                operation.setSuccessful();
            } else {
                operation.setException(result.message);
                me.fireEvent('exception', this, response, operation);
            }
        } else {
            me.setException(operation, response);
            me.fireEvent('exception', this, response, operation);
        }

        // this callback is the one that was passed to the 'read' or 'write'
        // function above
        if (typeof callback == 'function') {
            callback.call(scope || me, operation);
        }

        me.afterRequest(request, success);
    },
    /**
     * Creates and returns an Ext.data.Request object based on the options passed by the {@link Ext.data.Store Store}
     * that this Proxy is attached to.
     * @param {Ext.data.Operation} operation The {@link Ext.data.Operation Operation} object to execute
     * @return {Ext.data.Request} The request object
     */
    buildRequest: function(operation) {
        var params = Ext.applyIf(operation.params || {}, this.extraParams || {}), request;

        //copy any sorters, filters etc into the params so they can be sent over the wire
        params = Ext.applyIf(params, this.getParams(operation));

        if (operation.id && !params.id) {
            params.id = operation.id;
        }

        request = Ext.create('Ext.data.Request', {
            params   : params,
            records  : operation.records,
            operation : operation
        });

        /*
         * Save the request on the Operation. Operations don't usually care about Request and Response data, but in the
         * ServerProxy and any of its subclasses we add both request and response as they may be useful for further processing
         */
        operation.request = request;

        return request;
    },
    /**
     * @private
     * Copy any sorters, filters etc into the params so they can be sent over the wire
     */
    getParams: function(operation) {
        var me             = this,
            params         = {},
            isDef          = Ext.isDefined,
            groupers       = operation.groupers,
            sorters        = operation.sorters,
            filters        = operation.filters;

        Ext.apply(params, {
            page : operation.page,
            start : operation.start,
            limit : operation.limit
        });

        if (groupers && groupers.length > 0) {
            Ext.apply(params , {
                groupers : me.encodeSorters(groupers)
            });
        }

        if (sorters && sorters.length > 0) {
            Ext.apply(params , {
                sorters : me.encodeSorters(sorters)
            });
        }

        if (filters && filters.length > 0) {
            Ext.apply(params , {
                filters : me.encodeFilters(filters)
            });
        }

        return params;
    },

    /**
     * Encodes the array of {@link Ext.util.Sorter} objects into a string to be sent in the request url. By default,
     * this simply JSON-encodes the sorter data
     * @param {Ext.util.Sorter[]} sorters The array of {@link Ext.util.Sorter Sorter} objects
     * @return {String} The encoded sorters
     */
    encodeSorters: function(sorters) {
        var min = [],
            length = sorters.length,
            i = 0;

        for (; i < length; i++) {
            min[i] = {
                property : sorters[i].property,
                direction: sorters[i].direction
            };
        }
        return this.applyEncoding(min);

    },
    /**
     * Encode any values being sent to the server. Can be overridden in subclasses.
     * @private
     * @param {Array} An array of sorters/filters.
     * @return {Object} The encoded value
     */
    applyEncoding: function(value){
        return Ext.encode(value);
    },
    /**
     * Encodes the array of {@link Ext.util.Filter} objects into a string to be sent in the request url. By default,
     * this simply JSON-encodes the filter data
     * @param {Ext.util.Filter[]} filters The array of {@link Ext.util.Filter Filter} objects
     * @return {String} The encoded filters
     */
    encodeFilters: function(filters) {
        var min = [],
            length = filters.length,
            i = 0;

        for (; i < length; i++) {
            min[i] = {
                property: filters[i].property,
                value   : filters[i].value
            };
        }
        return this.applyEncoding(min);
    },
    /**
     * Sets up an exception on the operation
     * @private
     * @param {Ext.data.Operation} operation The operation
     * @param {Object} response The response
     */
    setException: function(operation, response){
        operation.setException({
            status: response.status,
            statusText: response.statusText
        });
    },
    /**
     * Optional callback function which can be used to clean up after a request has been completed.
     * @param {Ext.data.Request} request The Request object
     * @param {Boolean} success True if the request was successful
     * @method
     */
    afterRequest: Ext.emptyFn
}, function() {
    // backwards compatibility, remove in Ext JS 5.0
    Ext.data.HttpProxy = this;
});