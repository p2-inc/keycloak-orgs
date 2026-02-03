<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        ${msg("selectIdpTitle")}
    <#elseif section = "header">
        ${msg("selectIdpHeader")}
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                     <div class="${properties.kcFormGroupClass!}">
                        <label for="providerId" class="${properties.kcLabelClass!}">${msg("doSelectIdp")}</label>
                        <input id="providerId" type="text" name="providerId" class="${properties.kcInputClass!}" />
                     </div>
                     <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
                     </div>
                </form>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>