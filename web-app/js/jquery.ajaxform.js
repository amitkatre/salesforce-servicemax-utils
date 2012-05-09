(function($) {
    $.fn.extend({
        ajaxForm: function(options) {
            var defaults = {
                formElement: '',
                successMessage: 'Save successfull',
                errorMessage: 'We had a problem saving your data',
                errorElement: null,
                onSuccess: function() {
                },
                onError: function() {
                }
            };
            var settings = $.extend(defaults, options);
            return this.each(function() {
                var element = $(this);
                $(element).button().click(function() {
                    $(settings.formElement).ajaxSubmit({
                        dataType: 'json',
                        success: function(jsonData) {
                            if (jsonData.success) {
                                $.jnotify(settings.successMessage);
                                if (settings.errorElement) {
                                    $(settings.errorElement).html("").hide();
                                } else {
                                    $(".errors").html("").hide();
                                }
                                settings.onSuccess.call(this);
                            } else {
                                $.jnotify(settings.errorMessage, 'error');
                                showErrors(jsonData.errors, settings.errorElement);
                                settings.onError.call(this);
                            }
                        }
                    });
                    return false;
                });
            });
        }
    });
})(jQuery);