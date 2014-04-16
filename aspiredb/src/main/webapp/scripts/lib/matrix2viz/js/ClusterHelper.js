h2m = {};

h2m.ClusterHelper = {};

h2m.ClusterHelper.produceClusteredOrder = function (data) {
    var rowClustering = clusterfck.hcluster(data);
    var columnClustering = clusterfck.hcluster(data.transpose());

    var inOrderTraversal = function (node, output) {
        if (node.left) {
            inOrderTraversal(node.left, output);
        }

        if (typeof node.originalIndex !== 'undefined') {
            node.finalIndex = output.length;
            output.push(node.originalIndex);
        }

        if (node.right) {
            inOrderTraversal(node.right, output);
        }
    };

    var colOrder = [];
    var rowOrder = [];

    inOrderTraversal(columnClustering[0], colOrder);
    inOrderTraversal(rowClustering[0], rowOrder);

    return {
        columnOrder: colOrder,
        rowOrder: rowOrder,
        rowClustering: rowClustering[0],
        columnClustering: columnClustering[0]
    }
};
