<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        Invitations
    <#elseif section = "form">
    <div id="kc-terms-text">
        You have been invited to join the following organizations. Uncheck those you wish to decline.
    </div>
    <form class="form-actions" action="${url.loginAction}" method="POST">
    <#list invitations.orgs as org>
      <div class="checkbox">
        <label>
          <input id="org-${org.id}" name="orgs" type="checkbox" value="${org.id}" checked> ${org.displayName}
        </label>
      </div>
    </#list>
      <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="accept" id="kc-accept" type="submit" value="${msg("doAccept")}"/>
    </form>
    <div class="clearfix"></div>
    </#if>
</@layout.registrationLayout>
