<%@ page import="com.chrishurd.salesforce.MetadataObject" %>
<form action="save" class="saveMetaDef" >
    <div class="sixteen_column section" >
        <div class="eight column" >
            <div id="metadata-navigator" >
                <ul id="metaNavColumn1" class="treeview-famfamfam" >
                    <g:each in="${metadataMap.keySet().sort()}" var="type" status="i" >
                        <li class="closed" ><input type="checkbox" name="massObjects" value="${type}" class="${type}" /> ${type}
                            <ul>
                                <g:if test="${metadataMap.get(type).managedObjects}" >
                                    <li class="closed" ><input type="checkbox" name="massObjects" value="${type}_managed" class="${type}_managed" />Managed
                                        <ul>
                                            <li>
                                                <div class="sixteen_column" >
                                                    <div class="eight column" >Object</div>
                                                    <div class="two column" >Created By</div>
                                                    <div class="two column" >Modified By</div>
                                                    <div class="four column" >Modified On</div>
                                                </div>

                                            </li>
                                            <g:each in="${metadataMap.get(type).managedObjects.sort(MetadataObject.nameComparator)}" var="obj" >
                                                <li>
                                                    <div class="sixteen_column" >
                                                        <div class="eight column" >
                                                            <input type="checkbox" name="loadedObjects" value="${obj.name}" class="${type}_managed_object" ${ loadedObjects.contains(obj.name) ? "CHECKED" : ""} />
                                                            ${obj.name}
                                                        </div>
                                                        <div class="two column" >${obj.createdBy}</div>
                                                        <div class="two column" >${obj.lastModifiedBy}</div>
                                                        <div class="four column" ><g:formatDate date="${obj.lastModifiedOn}" dateStyle="MMM dd, yyyy HH:mm" /></div>
                                                    </div>
                                                </li>
                                            </g:each>
                                        </ul>
                                    </li>
                                </g:if>
                                <g:if test="${metadataMap.get(type).unmanagedObjects}" >
                                    <li class="closed" ><input type="checkbox" name="massObjects" value="${type}_unmanaged" class="${type}_unmanaged" />Un-managed
                                        <ul>
                                            <li>
                                                <div class="sixteen_column" >
                                                    <div class="eight column" >Object</div>
                                                    <div class="two column" >Created By</div>
                                                    <div class="two column" >Modified By</div>
                                                    <div class="four column" >Modified On</div>
                                                </div>

                                            </li>
                                            <g:each in="${metadataMap.get(type).unmanagedObjects.sort(MetadataObject.nameComparator)}" var="obj" >
                                                <li>
                                                    <div class="sixteen_column" >
                                                        <div class="eight column" >
                                                            <input type="checkbox" name="loadedObjects" value="${obj.name}" class="${type}_unmanaged_object" ${ loadedObjects.contains(obj.name) ? "CHECKED" : ""} />
                                                            ${obj.name}
                                                        </div>
                                                        <div class="two column" >${obj.createdBy}</div>
                                                        <div class="two column" >${obj.lastModifiedBy}</div>
                                                        <div class="four column" ><g:formatDate date="${obj.lastModifiedOn}" dateStyle="MMM dd, yyyy HH:mm" /></div>
                                                    </div>
                                                </li>
                                            </g:each>
                                        </ul>
                                    </li>
                                </g:if>
                            </ul>
                        </li>

                        <g:if test="${ i + 1 == metadataMap.size() / 2}" >
                </ul>
            </div>
        </div>
        <div class="eight column" >
            <div id="metadata-navigator2" >
                <ul id="metaNavColumn2" class="treeview-famfamfam" >
                        </g:if>
                    </g:each>
                </ul>
            </div>
        </div>
    </div>
    <div class="sixteen_column section" >
        <div class="fourteen column" >&nbsp;</div>
        <div class="two column" ><input type="button" value="Load Metadata" onClick="saveForm()" /></div>
    </div>
</form>


<script type="text/javascript" >
function saveForm() {
    $.blockUI();
    $.post(contextPath + '/metadata/save.json', $('.saveMetaDef').serialize(), function(data) {
        $.unblockUI();
    }, 'json');
}



</script>