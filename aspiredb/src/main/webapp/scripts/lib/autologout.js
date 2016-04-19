
/**
 * @class Ext.ux.ActivityMonitor
 * @author Arthur Kay (http://www.akawebdesign.com)
 * @modifications Manuel Belmadani (manuel.belmadani@ubc.ca)
 * @singleton
 * @version 1.0
 *
 * GitHub Project: https://github.com/arthurakay/ExtJS-Activity-Monitor
 *
 * The MIT License (MIT)
 * 
 * Copyright (c) <2011> Arthur Kay
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

Ext.define('Ext.ux.ActivityMonitor', {
    singleton   : true,

    ui          : null,
    runner      : null,
    task        : null,
    lastActive  : null,
    
    ready       : false,
    verbose     : false,
    interval    : (1000 * 30 * 1),  //30 seconds
    maxInactive : (1000 * 60 * 60), //1 hour
    
    init : function(config) {
        if (!config) { config = {}; }
        
        Ext.apply(this, config, {
            runner     : new Ext.util.TaskRunner(),
            ui         : Ext.getBody(),
            task       : {
                run      : this.monitorUI,
                interval : config.interval || this.interval,
                scope    : this
            }
        });
        
        this.ready = true;
    },
    
    isReady : function() {
        return this.ready;
    },
    
    isActive   : Ext.emptyFn,
    isInactive : Ext.emptyFn,
    
    start : function() {
        if (!this.isReady()) {
            this.log('Please run ActivityMonitor.init()');
            return false;
        }
        
        this.ui.on('mousemove', this.captureActivity, this);
        this.ui.on('keydown', this.captureActivity, this);
        
        this.lastActive = new Date();
        this.log('ActivityMonitor has been started.');
        
        this.runner.start(this.task);
    },
    
    stop : function() {
        if (!this.isReady()) {
            this.log('Please run ActivityMonitor.init()');
            return false;
        }
        
        this.runner.stop(this.task);
        this.lastActive = null;
        
        this.ui.un('mousemove', this.captureActivity);
        this.ui.un('keydown', this.captureActivity);
        
        this.log('ActivityMonitor has been stopped.');
    },
    
    captureActivity : function(eventObj, el, eventOptions) {
        this.lastActive = new Date();
    },
    
    monitorUI : function() {
        var now      = new Date(),
            inactive = (now - this.lastActive);
        
        if (inactive >= this.maxInactive) {
            this.log('MAXIMUM INACTIVE TIME HAS BEEN REACHED');
            this.stop(); //remove event listeners
            
            this.isInactive();
            window.location.href = 'j_spring_security_logout';
        }
        else if (inactive >= this.maxInactive-this.interval*2) {
           var myForm = new Ext.Window({
              title: 'You are about to be logged out.',
              width: 500,
              autoHeight:true,
              layout: 'form',
              plain:true,
              bodyStyle:'padding:5px;',
              buttonAlign:'center',
              items: [{
                 xtype:'label',
                 text:'Close this window to remain logged in, or click the logout button.',
                 id:'mylabel',
                 height:50,
                 border:false

                 }],
                 buttons: [{
                  text: 'Logout',
                  href: 'j_spring_security_logout'               
              }]
          });
          myForm.show();
        }        
        else {
            this.log('CURRENTLY INACTIVE FOR ' + inactive + ' (ms)');
            this.isActive();
        }
    },
    
    log : function(msg) {
        if (this.verbose) {
            window.console.log(msg);
        }
    }
    
});

Ext.ux.ActivityMonitor.init({ verbose : true });
Ext.ux.ActivityMonitor.start();