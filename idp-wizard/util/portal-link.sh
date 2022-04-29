#!/bin/bash
# chmod 755 portal-link.sh
#  
# Use this way
# $ ./portal-link.sh
# Host (format http://foo.com ): https://app.phasetwo.io
# Username: gpatil@phasetwo.io
# Password:
# orgId: fa33fe50-789e-44cd-9faf-75aad08613b7
# baseUri (e.g. http://localhost:9000/auth ): http://localhost:9000/auth
# 
# the baseUri value is a hack because we don’t assume that the wizard will be served from a different base
# 
# fa33fe50-789e-44cd-9faf-75aad08613b7 is the orgId of a test org 
# the Username and Password are the normal ones you use to log into the wizard
# the result of that script will return a json object with a link field that will contain the link to automagically log you in and redirect you to the wizard


read -p 'Host (format http://foo.com ): ' host
read -p 'Username: ' user
read -sp 'Password: ' pass

DIRECT_GRANT_RESPONSE=$(curl -i --request POST $host/auth/realms/wizard/protocol/openid-connect/token --header "Accept: application/json" --header "Content-Type: application/x-www-form-urlencoded" --data "grant_type=password&username=$user&password=$pass&client_id=admin-cli")
ACCESS_TOKEN=$(echo $DIRECT_GRANT_RESPONSE | grep "access_token" | sed 's/.*\"access_token\":\"\([^\"]*\)\".*/\1/g');

read -p 'orgId: ' orgid
read -p 'baseUri (e.g. http://localhost:9000/auth ): ' baseUri

curl --request POST $host/auth/realms/wizard/orgs/$orgid/portal-link --header "Accept: application/json" --header "Authorization: Bearer $ACCESS_TOKEN" --data-urlencode "baseUri=$baseUri"