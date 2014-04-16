var data = {
    data: [
        [11, 15, 9, 122, 1, 0, 0, 1, 0, 'm'],
        [1, 15, 8, 12, 1, 1, 1, 1, 0, 'm'],
        [99, 15, 7, 122, 1, 0, 0, 1, 0, 'm'],
        [22, 15, 3, 12, 1, 0, 0, 0, 1, 'f'],
        [2, 1, 10, 7, 1, 0, 0, 1, 0, 'f'],
        [4, 1, 3, 122, 1, 0, 0, 1, 1, 'f'],
        [33, 1, 1, 2, 1, 0, 1, 1, 0, 'm'],
        [11, 15, 1, 122, 1, 0, 1, 0, 0, 'm'],
        [4, 1, 3, 122, 1, 0, 0, 1, 1, 'f'],
        [66, 15, 3, 122, 1, 0, 0, 1, 0, 'm'],
        [11, 15, 9, 122, 1, 0, 0, 1, 0, 'm'],
        [1, 15, 8, 12, 1, 1, 1, 1, 0, 'm'],
        [99, 15, 7, 122, 1, 0, 0, 1, 0, 'm'],
        [22, 15, 3, 12, 1, 0, 0, 0, 1, 'f'],
        [2, 1, 10, 7, 1, 0, 0, 1, 0, 'f'],
        [4, 1, 3, 122, 1, 0, 0, 1, 1, 'f'],
        [33, 1, 1, 2, 1, 0, 1, 1, 0, 'm'],
        [11, 15, 1, 122, 1, 0, 1, 0, 0, 'm'],
        [4, 1, 3, 122, 1, 0, 0, 1, 1, 'f'],
        [66, 15, 3, 122, 1, 0, 0, 1, 0, 'm']
    ]};

var columns = [
    {label: 'phenotype 1', metaNumber: 0.5, type: 'numeric', range: {low: 0, high: 100}},
    {label: 'phenotype 2', metaNumber: 0.2, type: 'numeric', range: {low: 0, high: 20}},
    {label: 'phenotype 3', metaNumber: 0.1, type: 'numeric', range: {low: 0, high: 10}},
    {label: 'phenotype 4', metaNumber: 0.5, type: 'numeric', range: {low: 0, high: 150}},
    {label: 'phenotype 5', metaNumber: 0.9, type: 'binary'},
    {label: 'phenotype 7', metaNumber: 0.7, type: 'binary'},
    {label: 'phenotype 8', metaNumber: 0.5, type: 'binary'},
    {label: 'phenotype 9', metaNumber: 1, type: 'binary'},
    {label: 'phenotype 10', metaNumber: 0.1, type: 'binary'},
    {label: 'gender', metaNumber: 0.1, type: 'gender'}
];

var rows = [
    {label: 'subject 1', group: 'X', subGroup: 'ASD'},
    {label: 'subject 2', group: 'X', subGroup: 'ASD'},
    {label: 'subject 3', group: 'X', subGroup: 'ASD'},
    {label: 'subject 4', group: 'X', subGroup: 'ASD'},
    {label: 'subject 5', group: 'X', subGroup: 'ASD'},
    {label: 'subject 6', group: 'X', subGroup: 'Control'},
    {label: 'subject 7', group: 'Y', subGroup: 'Control'},
    {label: 'subject 8', group: 'Y', subGroup: 'Control'},
    {label: 'subject 9', group: 'Y', subGroup: 'Control'},
    {label: 'subject 10', group: 'Y', subGroup: 'Control'},
    {label: 'subject 11', group: 'X', subGroup: 'ASD'},
    {label: 'subject 12', group: 'X', subGroup: 'ASD'},
    {label: 'subject 13', group: 'X', subGroup: 'ASD'},
    {label: 'subject 14', group: 'X', subGroup: 'ASD'},
    {label: 'subject 15', group: 'X', subGroup: 'ASD'},
    {label: 'subject 16', group: 'X', subGroup: 'Control'},
    {label: 'subject 17', group: 'Y', subGroup: 'Control'},
    {label: 'subject 18', group: 'Y', subGroup: 'Control'},
    {label: 'subject 19', group: 'Y', subGroup: 'Control'},
    {label: 'subject 20', group: 'Y', subGroup: 'Control'}
];
