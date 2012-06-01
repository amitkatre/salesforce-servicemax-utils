<%--
  Created by IntelliJ IDEA.
  User: churd
  Date: 5/18/12
  Time: 10:32 PM
  To change this template use File | Settings | File Templates.
--%>

<div class="box" id="metaDataDiv">
    <h2>
        <a href="#" id="toggle-metaData" >Manage Org Metadata <span id="orgName" >&nbsp;</span></a>
    </h2>
    <div class="first metaData" >
        <div class="sixteen_column section" >
            <div class="five column">
                <div class="column_content" >
                    <select id="currentOrg" >
                        <option value="" ></option>
                        <g:each in="${orgList}" var="orgInfo" >
                            <option value="${orgInfo.id}" >${orgInfo.name}</option>
                        </g:each>
                    </select>
                </div>
            </div>
            <div class="eleven column" >&nbsp;</div>
        </div>
        <div id="metaDataOrgDiv" >

        </div>
    </div>


</div>

<script type="text/javascript">
    $(function() {
        setToggle('metaData');

        $('#currentOrg').change(function() {
            if ($(this).val() != '') {
                $.blockUI();
                $('#metaDataOrgDiv').load(contextPath + '/metadata/load/' + $(this).val(), function() {
                    $("#metaNavColumn1").treeview();
                    $("#metaNavColumn2").treeview();
                    $.unblockUI();
                });
            }
        });

    });



</script>
