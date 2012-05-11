
<div class="box" id="regOrgsDiv">
    <h2>
        <a href="#" id="toggle-regOrgs" >Registered Orgs</a>
    </h2>
    <div class="block" id="regOrgs">
        <table id="registered_orgs_list" ></table>
        <div id="registerd_orgs_pager" ></div>
        <a href="#" onclick="" id="add-organizationInfo" >Add</a>
    </div>
</div>
<div id="delete-organizationInfo-dialog" style="display:none"><p>Are you sure?</p></div>

<script type="text/javascript">
    $(function() {
        setToggle('regOrgs');

        $('#registered_orgs_list').jqGrid({
            url: '<g:createLink controller="organizationInfo" action="list" />.json',
            datatype: 'json',
            mtype: 'POST',
            colNames: ['id', 'Name', 'Env', ''],
            colModel: [
                {name: 'id', index: 'id', hidden: true},
                {name: 'Name',index: 'name', width: 200, search: true},
                {name: 'Env', index: 'env', width:50, search: false},
                {name: '', index: 'actions', width:30, search: false}
            ],
            pager: '#registerd_orgs_pager',
            onSelectRow: function(id) {

            },
            gridComplete: function() {

                setNewOrgInfoDialogClick("span.edit-organizationInfo, #add-organizationInfo");

                $("span.delete-organizationInfo").click(function() {
                    var data = $(this).metadata();

                    $('#delete-organizationInfo-dialog').dialog({
                        autoOpen: true,
                        buttons: {
                            "Ok" : function() {
                                $.post('<g:createLink controller="organizationInfo" action="delete" />', { id: data.id}, function(data) {
                                    $('#registered_orgs_list').trigger('reloadGrid');
                                    $.gritter.add({title: 'An update has been made', text: "Org details deleted"});
                                })
                                $(this).dialog("close");
                            },
                            "Cancel": function() {
                                $(this).dialog("close");
                            }
                        }
                    });
                });

                $("span.check-organzationInfo").click(function() {
                    var data = $(this).metadata();

                    $.post('<g:createLink controller="organizationInfo" action="checkConnection" />', { id: data.id}, function(data) {
                        if (data.success == 'true') {
                            $.gritter.add({title: 'Checked connection', text: "All good"});
                        }
                        else {
                            $.gritter.add({title: 'Checked connection', text: "Bad Connection: " + data.errors.error});
                        }

                    }, 'json');

                });
            }
        });

    });


</script>