var URLS = {
    edit_org_info : contextPath + "/organizationInfo/edit",
    select_list_org_info : contextPath + "/organizationInfo/selectList"
};


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

function setNewOrgInfoDialogClick(div) {
    $(div).dialogForm({
        formUrl: URLS.edit_org_info,
        formElement: '.create_organizationInfo',
        dialogTitle: 'New Org Info',
        formHeight: 370,
        formWidth: 350,
        beforeLoad: function(ui) {
            var data = $(ui).metadata();
            if (data.id != null) {
                return {id: data.id}
            }
        },
        onSuccess: function(data) {
            if (data.success == true) {
                if ($('#registered_orgs_list').size() > 0) {
                    $('#registered_orgs_list').trigger('reloadGrid');
                }
                $.gritter.add({title: 'Update has been completed', text: "Org details saved"});
            }
            else {
                showErrors(data.errors);
            }
        }
    });
}

function openSelectOrgDialog(funct) {
    var selectOrgDialog = $().dialogForm({
        formUrl: URLS.select_list_org_info,
        formElement: ".select_organizationInfo",
        dialogTitle: 'Select Salesforce Org',
        formHeight: 300,
        formWidth: 200,
        onSuccess: funct
    });

    selectOrgDialog.dialog('show');
}