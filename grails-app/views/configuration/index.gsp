<div class="box" id="sfmMigrationDiv">
    <h2>
        <a href="#" id="toggle-metaData" >SFM Migration <span id="sfmMigrationOrgName" >&nbsp;</span></a>
    </h2>
    <div class="first sfmMigration" >
        <form name="sfmMigrationOrgForm"  >
            <div class="sixteen_column section" >
                <div class="four column">
                    <div class="column_content" >
                        From <select id="fromOrg" name="fromOrg" style="width:80%" class="orgSelect" >
                        <option value="" ></option>
                        <g:each in="${orgList}" var="orgInfo" >
                            <option value="${orgInfo.id}" >${orgInfo.name}</option>
                        </g:each>
                    </select>
                    </div>
                </div>
                <div class="four column">
                    <div class="column_content" >
                        What <select id="migrateWhat" name="migrateWhat" style="width:80%"  >
                        <option value="sfmTransaction" >SFM Transactions</option>
                        <option value="module" >Modules/Submodules/Settings/Tagss/Translations</option>
                        <option value="profile" >Configuration Profiles</option>
                        <option value="sfAction" >SF Actions</option>

                    </select>
                    </div>
                </div>
                <div class="four column" >
                    <input type="button" value="Refresh/Load" id="sfmOrgRefresh" style="width:100px" />
                    <input type="button" value="Add Org" id="sfmMigrationAddOrg" style="width:100px" />
                </div>
            </div>
        </form>
        <div id="sfmMigrationMainDiv" >

        </div>
    </div>


</div>

<script type="text/javascript">
    $(function() {
        setToggle('metaData');
        setNewOrgInfoDialogClick("#sfmMigrationAddOrg");

        $('#sfmOrgRefresh').click(function() {
            if ($('#fromOrg').val() != '' && $('#migrateWhat').val() != '') {
                $.blockUI();
                $('#sfmMigrationMainDiv').load(contextPath + '/configuration/load', $("form[name='sfmMigrationOrgForm']").serialize(), function() {
                    $.unblockUI();
                });
            }
            else {
                alert("Please select a 'From' and 'What'");
            }
        });

    });



</script>