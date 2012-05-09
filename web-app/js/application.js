if (typeof jQuery !== 'undefined') {
	(function($) {
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
	})(jQuery);
}


function loadPage(div, position, link, params) {
    if ($(div).size() > 0) {
        $(div).remove();
    }
    $.post(link, params, function(data) {
        $(position).prepend(data);
    });
}

function setToggle(divName) {
    var default_hide = {"grid": true, "navigator": true };

    var el = $("#" + (divName == 'accordon' ? 'accordion-block' : divName) );
    if (default_hide[divName]) {
        el.hide();
        $("[id='toggle-"+divName+"']").addClass("hidden")
    }
    $("[id='toggle-"+divName+"']")
        .bind("click", function(e) {
            if ($(this).hasClass('hidden')){
                $(this).removeClass('hidden').addClass('visible');
                el.slideDown();
            } else {
                $(this).removeClass('visible').addClass('hidden');
                el.slideUp();
            }
            e.preventDefault();
        });
}

function showErrors(errors, element) {
    var errorList = $("<ul>");
    for (field in errors) {
        errorList.append("<li>" + errors[field] + "</li>")
        $('input[name=' + field + ']').addClass('error');
    }
    if (!element) {
        $(".errors").html("").append(errorList).show(500);
    } else {
        $(element).html("").append(errorList).show(500);
    }

}