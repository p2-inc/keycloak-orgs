<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false displayInfo=false; section>
    <#if section = "header">
        ${kcSanitize(msg("validationSuccessTitle"))?no_esc}
    <#elseif section = "form">
        <div id="kc-validation-message">
            <p class="instruction">${message.summary}</p>
	    <p class="instruction">${kcSanitize(msg("validationCloseWindow"))}</p>
        </div>
    <#elseif section == "info">
        <#-- intentionally empty: suppress username + restart login -->
    </#if>
</@layout.registrationLayout>
