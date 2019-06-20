/* ------------------------------------------------------------------------------
*
*  # Extended form controls
*
*  Specific JS code additions for form_controls_extended.html page
*
*  Version: 1.1
*  Latest update: Mar 5, 2016
*
* ---------------------------------------------------------------------------- */

$(function() {



    // Elastic textarea
    // ------------------------------

    // Basic example
    autosize($('.elastic'));

    // Manual trigger
    $('.elastic-manual-trigger').on('click', function() {
        var manual = autosize($('.elastic-manual'));
        $('.elastic-manual').val('Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam sed ultricies nibh, sed faucibus eros. Vivamus tristique fringilla ante, vitae pellentesque quam porta vel. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nunc vehicula gravida nisl non imperdiet. Mauris felis odio, vehicula et laoreet non, tempor non enim. Cras convallis sapien hendrerit nibh sagittis sollicitudin. Fusce nec ultricies justo. Interdum et malesuada fames ac ante ipsum primis in faucibus. Fusce ac urna in dui consequat cursus vel sit amet mauris. Proin nec bibendum arcu. Aenean sit amet nisi mi. Sed non leo nisl. Mauris leo odio, ultricies interdum ornare ac, posuere eu risus. Suspendisse adipiscing sapien sit amet gravida sollicitudin. Maecenas laoreet velit in dui adipiscing, vel fermentum tellus ullamcorper. Nullam et mi rhoncus, tempus nulla sit amet, varius ipsum.');
        autosize.update(manual);
    });

    // Destroy method
    var destroyAutosize = autosize($('.elastic-destroy'));
    $('.elastic-destroy-trigger').on('click', function() {
        autosize.destroy(destroyAutosize);
    });



    // Maxlength
    // ------------------------------

    // Basic example
    $('.maxlength').maxlength();

    // Threshold
    $('.maxlength-threshold').maxlength({
        threshold: 15
    });

    // Custom label color
    $('.maxlength-custom').maxlength({
        threshold: 10,
        warningClass: "label label-primary",
        limitReachedClass: "label label-danger"
    });

    // Options
    $('.maxlength-options').maxlength({
        alwaysShow: true,
        threshold: 10,
        warningClass: "text-success",
        limitReachedClass: "text-danger",
        separator: ' of ',
        preText: 'You have ',
        postText: ' chars remaining.',
        validate: true
    });

    // Always show label
    $('.maxlength-textarea').maxlength({
        alwaysShow: true
    });

    // Label position
    $('.maxlength-label-position').maxlength({
        alwaysShow: true,
        placement: 'top'
    });




    // ========================================
    //
    // Typeahead
    //
    // ========================================


    // Remote data
    // ------------------------------

    // Constructs the suggestion engine
    var bestPictures = new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        prefetch: 'assets/demo_data/typeahead/movies.json',
        remote: 'assets/json/queries/%QUERY.json'
    });

    // Initialize engine
    bestPictures.initialize();

    // Initialize
    $('.typeahead-remote').typeahead(null, {
        name: 'best-pictures',
        displayKey: 'value',
        source: bestPictures.ttAdapter()
    });



    // Custom templates
    // ------------------------------

    // Initialize with opitons
    $('.typeahead-custom-templates').typeahead(null, {
        name: 'best-pictures',
        displayKey: 'value',
        source: bestPictures.ttAdapter(),
        templates: {
            empty: [
                '<div class="empty-message">',
                'unable to find any Best Picture winners that match the current query',
                '</div>'
            ].join('\n'),
            suggestion: Handlebars.compile('<p><strong>{{value}}</strong> – {{year}}</p>')
        }
    });



    // Multiple datasets
    // ------------------------------

    // Constructs the suggestion engine for 1st dataset
    var nbaTeams = new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.obj.whitespace('team'),
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        prefetch: 'assets/demo_data/typeahead/nba.json'
    });

    // Constructs the suggestion engine for 2nd dataset
    var nhlTeams = new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.obj.whitespace('team'),
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        prefetch: 'assets/demo_data/typeahead/nhl.json'
    });

    // Initialize engines
    nbaTeams.initialize();
    nhlTeams.initialize();

    // Initialize 1st dataset
    $('.typeahead-multiple-datasets').typeahead(
        {
            highlight: true
        },
        {
            name: 'group',
            displayKey: 'team',
            source: nbaTeams.ttAdapter(),
            templates: {
                header: '<span class="tt-heading">NBA Teams</span>'
            }
        },
        {
            name: 'group',
            displayKey: 'team',
            source: nhlTeams.ttAdapter(),
            templates: {
                header: '<span class="tt-heading">NHL Teams</span>'
            }
        }
    );



    // Dropdown height
    // ------------------------------

    // Initialize with options
    $('.typeahead-scrollable-menu').typeahead(null, {
        name: 'countries',
        displayKey: 'name',
        source: countries.ttAdapter()
    });



    // RTL support
    // ------------------------------

    // Constructs the suggestion engine
    var arabicPhrases = new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.obj.whitespace('word'),
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        local: [
            { word: "الإنجليزية" },
            { word: "نعم" },
            { word: "لا" },
            { word: "مرحبا" },
            { word: "أهلا" }
        ]
    });

    // Initialize engine
    arabicPhrases.initialize();

    // Initialize
    $('.typeahead-rtl-support').typeahead(
        {
            hint: false
        },
        {
            name: 'arabic-phrases',
            displayKey: 'word',
            source: arabicPhrases.ttAdapter()
        }
    );

});
