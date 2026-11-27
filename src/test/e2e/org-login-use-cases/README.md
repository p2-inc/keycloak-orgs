# Organization login use cases (browser tests)

End-to-end checks for organization-aware login with the Select Organization authenticator
(`ext-select-org`), run in a real browser (Playwright) against the Phase Two Keycloak image
with **this checkout's** keycloak-orgs jar. They back the blog post "Keycloak organization
login vs Auth0 and WorkOS" and also record its video.

CI runs them in `.github/workflows/org-login-use-cases.yml` on every pull request that touches
`src/main`, `pom.xml` or this folder. The job summary lists every check; screenshots of the
organization pages are uploaded as an artifact.

Last local run: 2026-11-27, `quay.io/phasetwo/phasetwo-keycloak:26.8.2` (Keycloak 26.8.2) with
keycloak-orgs 0.201-SNAPSHOT built from `main`, 27/27 checks passed.

## What it sets up

`setup_cases.py` creates two realms:

- **`shophost`**: a store platform where each store is an organization (`globex`, `initech`,
  `acme`). The `store-admin` client maps the active organization into the token.
- **`upstream`**: stands in for the outside identity providers: Globex's and Acme's company
  SSO (organization-owned, routed by email domain), Google, and a partner IdP.

Flows:

| Flow | Steps |
|---|---|
| `shop login` (browser) | session: cookie, Auto Join Invitations, Select Organization; or IdP redirector; or Home IdP Discovery / password, then Auto Join Invitations, Select Organization |
| `company sso post broker` | Add User to Org, Auto Join Invitations, Select Organization |
| `external idp post broker` (Google, partner) | Auto Join Invitations, Select Organization |

Users (password `Passw0rd!`):

| User | Login | Stores |
|---|---|---|
| `paula@mail.test` | password | member of globex |
| `ivan@mail.test` | password | invited to globex |
| `nina@mail.test` | password | none |
| `carl@contractor.test` | password | member of globex and initech |
| `sam@gmail.test` | Google | invited to globex |
| `zoe@gmail.test` | Google | none |
| `erin@globex.test` | Globex SSO | joins globex on first sign-in |
| `ann@acme.test` | Acme SSO | joins acme on first sign-in |
| `pete@partner.test` | partner IdP | member of globex (account pre-linked) |
| `quinn@partner.test` | partner IdP | none |

## What it checks

1. `account_hint=globex` for every login method: members and invitees get in, everyone else
   is refused. Invitations are accepted during sign-in.
2. A contractor picks a store with `prompt=select_account`, switches with `account_hint` on the
   session and with the switch-organization API, and is refused for stores he is not in.
3. `kc_idp_hint=partner` plus `account_hint`: the check runs in the IdP's post-broker flow.
4. Refusals: the message shown, and that no session is left behind.

## Run it

Keycloak listens on 8081 (not 8080), so it does not collide with other local Keycloaks. To test
your own build, put the jar in `provider/`; it overwrites the bundled keycloak-orgs jar in the
image (the image is pre-built, so the file name has to stay the same). With `provider/` empty,
the published image is tested as is.

```bash
mvn -DskipTests package && cp target/keycloak-orgs-*-SNAPSHOT.jar src/test/e2e/org-login-use-cases/provider/
```

```bash
docker compose up -d --build
```

```bash
python3 -m venv .venv && .venv/bin/pip install -r requirements.txt && .venv/bin/python -m playwright install chromium
```

```bash
.venv/bin/python setup_cases.py > orgs_cases.json && .venv/bin/python tests_cases.py
```

Results go to `results_cases.json`, screenshots of the organization pages to `shots_cases/`.
The script exits 1 if any check fails.
To record the use-case 1 video (`video_cases/use-case-1.webm`), reset the realm first, because
the run consumes invitations:

```bash
.venv/bin/python setup_cases.py > orgs_cases.json && .venv/bin/python tests_cases.py --video
```

Stop everything with:

```bash
docker compose down -v
```

## Gotchas found while building it

- The organization IdP link API (`POST /orgs/{id}/idps/link`) takes snake_case fields:
  `post_broker_flow`, `sync_mode`. A camelCase `postBrokerFlow` is ignored, and the IdP gets the
  default `post org broker login` flow, which has no Select Organization, so `account_hint` is
  not checked for that IdP.
- The Phase Two image is pre-built. Deleting the bundled keycloak-orgs jar and adding another
  file breaks startup (`NoSuchFileException` for the old jar); overwrite the file instead.
- Client flow overrides must be set by flow id after the realm exists; a realm import with an
  alias in `authenticationFlowBindingOverrides` fails with a 500.
- Chromium resolves `localhost` to IPv6; the tests map it to 127.0.0.1. Playwright route
  interception does not catch the redirect to the app callback, so the tests serve a real
  callback on port 3000.
