<form action="save" class="saveSfmMigration" >
    <g:hiddenField id="migrateFromOrg" name="migrateFromOrg" value="${orgInfo.id}" />
    <div class="sixteen_column section" >
        <div class="four column">
            <div class="column_content" >
                To <select id="migrateToOrg" name="migrateToOrg" style="width:80%" class="orgSelect" >
                <option value="" ></option>
                <g:each in="${orgList}" var="toOrgInfo" >
                    <g:if test="${orgInfo.id != toOrgInfo.id}" >
                        <option value="${toOrgInfo.id}" >${toOrgInfo.name}</option>
                    </g:if>
                </g:each>
            </select>
            </div>
        </div>
        <div class="eight column" >
            <input type="button" value="Migrate" id="sfmMigrateButton" style="width:100px" />
        </div>
    </div>
    <div class="sixteen_column section" >
        <div class="sixteen column" >
            <g:if test="${migrationObjects.size() > 0}" >
                <div class="sixteen_column section" >
                    <div class="one column" style="height:auto" ><input type="checkbox" id="allObjects" name="allObjects" onclick="toggleMigrationObjects()" /> </div>
                    <div class="six column" style="height:auto;" >Name</div>
                    <div class="three column" >Last Modified</div>
                    <div class="six column"  >Error/Comment</div>
                </div>
                <g:each in="${migrationObjects}" var="obj" >
                    <div class="sixteen_column section" >
                        <div class="one column" style="height:auto" >
                            <input type="checkbox" name="sfmObjects" value="${obj.type}|${obj.id}" class="sfmObjects"  />
                        </div>
                        <div class="six column" style="height:auto;" >${obj.name}</div>
                        <div class="three column" >
                            <g:formatDate date="${obj.modifiedDate}"  dateStyle="MMM dd, yyyy HH:mm" />
                        </div>
                        <div class="six column" id="${obj.id}Error" style="height:auto;" >&nbsp;</div>
                    </div>
                </g:each>
            </g:if>
        </div>
    </div>
</form>

<script type="text/javascript"  >

    var sfmObjects = new Array();

    $(function() {

        $('#sfmMigrateButton').click(function() {
            if ($('#toOrg').val() != '' && $('.sfmObjects:checked').size() > 0) {
                $.blockUI();
                sfmObjects = new Array();
                $('.sfmObjects:checked').each(function(index) {
                    sfmObjects.push($(this).val());
                    $('#' + $(this).val() + 'Error').html('');
                })
                migrateObject(0)
            }
            else {
                alert("Please select a 'To' and something to migrate");
            }
        });

    });

    function toggleMigrationObjects() {
        var checked =  $('#allObjects:checked').size() > 0;
        $('.sfmObjects').each(function(index) {
            if (checked) {
                $(this).attr('checked', true);
            }
            else {
                $(this).removeAttr('checked')
            }
        });
    }

    function migrateObject(index) {
        if (sfmObjects.length >= index + 1 && sfmObjects[index] != null && sfmObjects[index] != '') {
            var ids = sfmObjects[index].split('|');
            $('#' + ids[1] + 'Error').html('Processing.....');
            $.post(contextPath + '/configuration/save', {fromOrg : $('#migrateFromOrg').val(), toOrg : $('#migrateToOrg').val(), objectId : ids[1], type : ids[0] }, function(data) {
                if ('true' == data.success) {
                    $('#' + ids[1] + 'Error').html('Complete');
                }
                else {
                    $('#' + ids[1] + 'Error').html(data.errors.error);
                }

                migrateObject(index + 1);
            }, 'json');
        }
        else {
            $.unblockUI();
        }
    }




</script>