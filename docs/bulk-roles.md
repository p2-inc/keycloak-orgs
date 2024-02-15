# Bulk roles feature

## Contents
<!-- TOC -->
* [Bulk roles feature](#bulk-roles-feature)
  * [Contents](#contents)
  * [Overview](#overview)
  * [Endpoints](#endpoints)
    * [Switch Organization](#switch-organization)
    * [Active organization](#active-organization)
<!-- TOC -->

## Overview
This document outlines the functionality and usage of the new Bulk Roles Grant/Revoke feature in the REST resources API.
The feature facilitates the efficient assignment and removal of roles for both organizations and users within those organizations.

## Endpoints
4 new endpoints were added.

Bulk operations process each submitted record independently, allowing failures to be caught but the remainder of the import to succeed. 
Each bulk operation response provides the success or failure status for each record for processing:
```
HTTP/1.1 207 Multi-Status
Content-Type: application/json

[
    { "status": 201, error: null, item: {} },
    { "status": 201, error: null, item: {} },
    { "status": 400, error: "HTTP 409 Conflict", item: {} },
]
```
In this design, we return the result of each record individually by returning the 207 Multi-Status response.

### Create organization roles
Method: `PUT`  
Path: `:realm/orgs/{orgId}/roles`  
Body: array of OrganizationRole representations  
Body example: ```[{"name":"eat-apples"}, {"name":"bake-pies"},...]```

Response:
- 207: Multi-Status

Individual item response:
- 201: Created
  - This role has successfully created for given organization.
  - Body contains the OrganizationRole representation
- 400: Bad request
  - Something went wrong `error` field contains error description


### Delete organization roles
Method: `PATCH`  
Path: `:realm/orgs/{orgId}/roles`  
Body: array of OrganizationRole representations  
Body example: ```[{"name":"eat-apples"},{"name":"drink-coffee"},...]```

Response:
- 207: Multi-Status

Individual item response:
- 204: No content
  - This role was successfully deleted for given organization.
  - Body contains the OrganizationRole representation
- 400: Bad request
  - Something went wrong, error description will be in response "error" field.

### Grant organization roles for user
Method: `PUT`  
Path: `:realm/{userId}/orgs/{orgId}/roles`  
Body: array of OrganizationRole representations  
Body example: ```[{"name":"eat-apples"},{"name":"drink-coffee"},...]```

Response:
- 207: Multi-Status

Individual item response:
- 201: Created
  - This role has successfully granted for specified user of given organization.
  - Body contains the OrganizationRole representation
- 400: Bad request
  - Something went wrong `error` field contains error description

### Revoke organization roles for user
DELETE /{userId}/orgs/{orgId}/roles - input json List<OrganizationRole>
Method: `PATCH`  
Path: `:realm/{userId}/orgs/{orgId}/roles`  
Body: array of OrganizationRole representations  
Body example: ```[{"name":"eat-apples"},{"name":"drink-coffee"},...]```

Response:
- 207: Multi-Status

Individual item response:
- 204: No content
  - This role was successfully revoked for specified user of given organization.
  - Body contains the OrganizationRole representation
- 400: Bad request
  - Something went wrong, error description will be in response "error" field.