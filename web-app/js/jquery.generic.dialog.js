$.extend({
    genericDialog: function(options) {
        var defaults = {
            formUrl: '',
            formElement: '',
            dialogTitle: 'Create',
            formWidth: 500,
            formHeight: 500,
            successMessage: 'Save successfull',
            saveButtonLabel: 'Save',
            params: {},
            beforeSave: function() {

            },
            beforeClose: function() {

            },
            beforeLoad: function(event, ui) {
                return {}

            },
            onSave: function() {

            },
            onSuccess: function(event, jsonData) {
            },
            onError: function(event, jsonData) {
            }
        };
        var settings = $.extend(defaults, options);


        $('#dialog-form').load(settings.formUrl, settings.params, function() {

            var dialog = $(this);


            $(dialog).find('.submittable').keypress(function(e) {
                var k = e.keyCode || e.which;
                if (k == 13) {
                    submitForm(dialog);
                    return false;
                }
            });


            dialog.dialog({
                autoOpen:true,
                modal:true,
                width: settings.formWidth,
                height: settings.formHeight,
                title:settings.dialogTitle,
                buttons: {
                    'Save': function() {
                        settings.beforeSave.call(this);
                        submitForm(dialog);

                    },
                    Cancel: function() {
                        settings.beforeClose.call(this);
                        $(dialog).dialog('close');

                    }
                },
                open:function(event, ui) {
                    $(".ui-dialog-buttonset button").addClass("btn");
                }

            });

        });


        function submitForm(dialog) {
            // submit the form
            $(settings.formElement).ajaxSubmit({
                dataType: 'json',
                success: function(jsonData) {
                    if (jsonData.success) {
                        settings.onSuccess.call(this, jsonData);
                        $(dialog).dialog('close');
                    } else {
                        showErrors(jsonData.errors);
                        settings.onError.call(this, jsonData);
                    }
                }
            });

        }
    }
});