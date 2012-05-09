(function($) {
    $.fn.extend({
        dialogForm: function(options) {
            var defaults = {
                formUrl: '',
                formElement: '',
                dialogTitle: 'Create',
                formWidth: 500,
                formHeight: 500,
                successMessage: 'Save successfull',
                saveButtonLabel: 'Save',
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

            return this.each(function() {
                var element = $(this);
                $(element).click(function() {
                    var params = settings.beforeLoad.call(this, element);

                    $('#dialog-form').load(settings.formUrl, params, function(response, status, xhr) {
                        if (status == "success") {
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
                                }

                            });
                        }

                    });

                    return false;
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
})(jQuery);