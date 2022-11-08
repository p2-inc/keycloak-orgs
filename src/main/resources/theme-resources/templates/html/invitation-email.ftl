<#import "template.ftl" as layout>
<@layout.emailLayout>
${kcSanitize(msg("invitationEmailBodyHtml", email, realmName, orgName, inviterName, link))?no_esc}
</@layout.emailLayout>
