<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${kcSanitize(msg("validationSuccess"))?no_esc}
    <#elseif section = "form">
        <div id="kc-validation-message">
            <p class="instruction">${message.summary}</p>
	    <p>${kcSanitize(msg("validationCloseWindow"))}</p>
        </div>
    </#if>
</@layout.registrationLayout>
