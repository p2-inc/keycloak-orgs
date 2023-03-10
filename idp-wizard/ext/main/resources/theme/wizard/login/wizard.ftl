<!doctype html>
<html lang="en-US">
  <head>
    <meta charset="utf-8"/>
    <title>${realmName} IdP Wizard</title>
    <meta id="appName" name="application-name" content="${realmName} IdP Wizard"/>
    <meta name="viewport" content="width=device-width,initial-scale=1"/>
    <base href="/auth/realms/${realmName}/wizard/"/>
    <link rel="apple-touch-icon" sizes="180x180" href="${wizardResources}/images/apple-touch-icon.png"/>
    <link rel="icon" type="image/png" sizes="32x32" href="${wizardResources}/images/favicon-32x32.png"/>
    <link rel="icon" type="image/png" sizes="16x16" href="${wizardResources}/images/favicon-16x16.png"/>
    <link rel="manifest" href="${wizardResources}/site.webmanifest"/>
    <link rel="mask-icon" href="${wizardResources}/images/safari-pinned-tab.svg" color="#5bbad5"/>
    <meta name="msapplication-TileColor" content="#da532c"/>
    <meta name="theme-color" content="#ffffff"/>
    <script defer="defer" src="${wizardResources}/main.bundle.js">
    </script>
    <link href="${wizardResources}/main.css" rel="stylesheet">
  </head>
  <body>
    <noscript>Enabling JavaScript is required to run this app.</noscript>
    <div id="root"></div>
  </body>
</html>
