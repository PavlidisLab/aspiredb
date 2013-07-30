/**
 * @struct
 * @constructor
 */
var Property = function() {
    this.name = null,
    this.displayName = null,
    this.dataType = null,
    this.exampleValues = [],
    this.operators = []
};

var VariantService = {};

VariantService.suggestLabels = function (suggestionContext, /*List<LabelValueObject>*/ callback) {
};

VariantService.suggestProperties = function (/*VariantType*/ variantType, /*AsyncCallback<Collection<Property>>*/ callback) {
    var result = [
        {
            name: 'A',
            displayName: 'property A'
        },{
            name: 'B',
            displayName: 'property A'
        }
    ];
    callback.success(result);
};

VariantService.suggestValues = function(/*Property*/ property, /*SuggestionContext*/ suggestionContext, /*AsyncCallback<Collection<PropertyValue>>*/ async) {
};

VariantService.suggestVariantLocationProperties = function (/*AsyncCallback<Collection<Property>>*/ async) {
};

VariantService.suggestVariantLocationValues = function (/*Property*/ property, /*SuggestionContext*/ suggestionContext, /*AsyncCallback<Collection<PropertyValue>>*/ async) {
};

VariantService.suggestProperties = function (/*AsyncCallback<Collection<Property>>*/ async) {
};
