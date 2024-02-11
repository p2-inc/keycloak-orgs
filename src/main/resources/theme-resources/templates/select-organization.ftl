<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        ${msg("selectOrgTitle")}
    <#elseif section = "header">
        ${msg("selectOrgHeader")}
    <#elseif section = "form">
      <div id="kc-form">
        <div id="kc-form-wrapper">
          <form data-cy="kc-form-login" id="kc-form-login" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
              <label data-cy="select-org-label" for="select-organization" class="${properties.kcLabelClass!}">${msg("selectOrganization")}</label>
              <select data-cy="select-org-input" class="${properties.kcInputClass}" id="select-organization" name="organizationId">
                  <#list organizations as organization>
                    <option data-cy="select-org-options" value="${organization.id}">${organization.name}</option>
                  </#list>
              </select>
            </div>
            <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
              <input data-cy="submit" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
            </div>
          </form>
        </div>
      </div>
    </#if>
</@layout.registrationLayout>
