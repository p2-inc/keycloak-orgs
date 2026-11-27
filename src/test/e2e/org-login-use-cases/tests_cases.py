"""Browser tests for the four use cases (Keycloak 26.8.2 + Phase Two), plus the case-1 video.

  python setup_cases.py > orgs_cases.json
  python tests_cases.py           run all checks; writes results_cases.json and shots_cases/,
                                  exits 1 if any check fails
  python tests_cases.py --video   record video_cases/use-case-1.webm (run setup_cases.py first)
"""
import base64
import json
import os
import re
import sys
import threading
import time
import urllib.parse
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

import requests
from playwright.sync_api import sync_playwright

KC = "http://localhost:8081"
REALM = "shophost"
CLIENT = "store-admin"
PW = "Passw0rd!"
CB = "http://localhost:3000/cb"
HERE = Path(__file__).parent
SHOTS = HERE / "shots_cases"
VIDEO = HERE / "video_cases"
SHOTS.mkdir(exist_ok=True)
_args = [a for a in sys.argv[1:] if not a.startswith("--")]
ORGS = json.loads(_args[0]) if _args else json.loads((HERE / "orgs_cases.json").read_text())
RESULTS = []
LAUNCH = {"args": ["--host-resolver-rules=MAP localhost 127.0.0.1"]}
INVALID = "Invalid Organization"
NO_ORG = "not part of any organization"


class _Callback(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html")
        self.end_headers()
        self.wfile.write(b"<h1>Signed in</h1>")

    def log_message(self, *a):
        pass


threading.Thread(target=ThreadingHTTPServer(("127.0.0.1", 3000), _Callback).serve_forever, daemon=True).start()


def record(case, check, expected, actual, ok):
    RESULTS.append({"case": case, "check": check, "expected": expected, "actual": str(actual), "pass": bool(ok)})
    print(("PASS " if ok else "FAIL ") + f"[{case}] {check}: {actual}")


def auth_url(**extra):
    q = {"client_id": CLIENT, "response_type": "code", "scope": "openid", "redirect_uri": CB,
         "state": "s", "nonce": "n", **extra}
    return f"{KC}/realms/{REALM}/protocol/openid-connect/auth?" + urllib.parse.urlencode(q)


def tokens_from(url):
    code = urllib.parse.parse_qs(urllib.parse.urlparse(url).query)["code"][0]
    r = requests.post(f"{KC}/realms/{REALM}/protocol/openid-connect/token",
                      data={"grant_type": "authorization_code", "client_id": CLIENT, "code": code, "redirect_uri": CB})
    r.raise_for_status()
    return r.json()


def org_name(tokens):
    p = tokens["access_token"].split(".")[1]
    claims = json.loads(base64.urlsafe_b64decode(p + "=" * (-len(p) % 4)))
    return (claims.get("active_organization") or {}).get("name")


def submit(page):
    page.locator("#kc-login, input[type=submit], button[type=submit]").first.click()


def identify(page, email):
    page.wait_for_selector("#username")
    page.fill("#username", email)
    submit(page)


def password(page):
    page.wait_for_selector("#password")
    page.fill("#password", PW)
    submit(page)


def upstream(page, email):
    page.wait_for_url(re.compile(r".*/realms/upstream/.*"))
    page.fill("#username", email)
    page.fill("#password", PW)
    submit(page)


def landed(page, timeout=10000):
    try:
        page.wait_for_url(re.compile(r"^http://localhost:3000/cb.*"), timeout=timeout)
        return True
    except Exception:
        return False


def outcome(page):
    """('in', org) if the app got a token, ('rejected', message) if Keycloak refused."""
    if landed(page):
        return "in", org_name(tokens_from(page.url))
    page.wait_for_load_state()
    body = page.inner_text("body")
    for msg in (INVALID, NO_ORG):
        if msg in body:
            return "rejected", msg
    return "unknown", body[:80]


def shot(page, name, pad=36):
    box = page.locator(".pf-v5-c-login__container").first.bounding_box()
    vw = page.viewport_size
    x, y = max(box["x"] - pad, 0), max(box["y"] - pad, 0)
    page.screenshot(path=str(SHOTS / f"{name}.png"), clip={
        "x": x, "y": y, "width": min(box["width"] + 2 * pad, vw["width"] - x),
        "height": min(box["height"] + 2 * pad, vw["height"] - y)})


def fresh(browser):
    ctx = browser.new_context(viewport={"width": 1100, "height": 760}, device_scale_factor=2)
    return ctx, ctx.new_page()


def no_session_left(page):
    """After a rejection, a new sign-in must start from the login form again."""
    page.goto(auth_url())
    return not landed(page, 4000) and page.locator("#username").count() > 0


def password_login(page, email, **hint):
    page.goto(auth_url(**hint))
    identify(page, email)
    password(page)


def admin_headers():
    t = requests.post(f"{KC}/realms/master/protocol/openid-connect/token",
                      data={"grant_type": "password", "client_id": "admin-cli", "username": "admin",
                            "password": "admin"}).json()["access_token"]
    return {"Authorization": f"Bearer {t}"}


def is_member(org, email):
    h = admin_headers()
    members = requests.get(f"{KC}/realms/{REALM}/orgs/{ORGS[org]}/members?max=200", headers=h).json()
    return any(m.get("email") == email for m in members)


def pending_invites(org, email):
    h = admin_headers()
    inv = requests.get(f"{KC}/realms/{REALM}/orgs/{ORGS[org]}/invitations", headers=h).json()
    return [i for i in inv if i.get("email") == email]


def check(browser, case, label, expected, run, shot_name=None, after_reject=True):
    ctx, page = fresh(browser)
    run(page)
    got = outcome(page)
    ok = got == expected
    if shot_name and got[0] == "rejected":
        shot(page, shot_name)
    record(case, label, expected, got, ok)
    if got[0] == "rejected" and after_reject:
        record(case, f"{label} -> no session left behind", True, no_session_left(page), no_session_left(page))
    ctx.close()


def run_checks():
    with sync_playwright() as p:
        b = p.chromium.launch(**LAUNCH)
        g = {"account_hint": "globex"}

        # Use case 1: account_hint=globex from the store admin app, every way of signing in.
        check(b, "1 password", "member", ("in", "globex"), lambda pg: password_login(pg, "paula@mail.test", **g))
        check(b, "1 password", "invited, not yet a member", ("in", "globex"),
              lambda pg: password_login(pg, "ivan@mail.test", **g))
        record("1 password", "invitation accepted on sign-in", "member, invitation gone",
               (is_member("globex", "ivan@mail.test"), len(pending_invites("globex", "ivan@mail.test"))),
               is_member("globex", "ivan@mail.test") and not pending_invites("globex", "ivan@mail.test"))
        check(b, "1 password", "neither member nor invited", ("rejected", INVALID),
              lambda pg: password_login(pg, "nina@mail.test", **g), "rejected-password")

        def google(email):
            def go(pg):
                pg.goto(auth_url(kc_idp_hint="google", **g))
                upstream(pg, email)
            return go
        check(b, "1 social", "invited Google user", ("in", "globex"), google("sam@gmail.test"))
        check(b, "1 social", "uninvited Google user", ("rejected", INVALID), google("zoe@gmail.test"),
              "rejected-google")

        def company(email):
            def go(pg):
                pg.goto(auth_url(**g))
                identify(pg, email)
                upstream(pg, email)
            return go
        check(b, "1 corporate SSO", "Globex employee through Globex SSO", ("in", "globex"), company("erin@globex.test"))
        check(b, "1 corporate SSO", "Acme employee through Acme SSO asks for Globex", ("rejected", INVALID),
              company("ann@acme.test"), "rejected-company-sso")

        # Use case 2: contractor in globex and initech.
        ctx, page = fresh(b)
        password_login(page, "carl@contractor.test", prompt="select_account")
        page.wait_for_selector("select[name=organizationId]")
        opts = page.locator("select[name=organizationId] option").all_inner_texts()
        record("2 contractor", "picker lists his stores", "globex, initech", opts, sorted(opts) == ["globex", "initech"])
        shot(page, "org-picker")
        page.select_option("select[name=organizationId]", label="initech")
        submit(page)
        got = outcome(page)
        record("2 contractor", "picks initech", ("in", "initech"), got, got == ("in", "initech"))
        page.goto(auth_url(account_hint="globex"))
        got = outcome(page)
        record("2 contractor", "switch to globex on the same session, no password", ("in", "globex"), got,
               got == ("in", "globex"))
        page.goto(auth_url(prompt="select_account"))
        page.wait_for_selector("select[name=organizationId]")
        record("2 contractor", "picker again while signed in, no password", True,
               page.locator("#password").count() == 0, page.locator("#password").count() == 0)
        page.select_option("select[name=organizationId]", label="globex")
        submit(page)
        tok = tokens_from(page.url) if landed(page) else None
        r = requests.put(f"{KC}/realms/{REALM}/users/switch-organization", json={"id": ORGS["initech"]},
                         headers={"Authorization": f"Bearer {tok['access_token']}"}) if tok else None
        got = org_name(r.json()) if r is not None and r.ok else r and r.status_code
        record("2 contractor", "switch API to initech", "initech", got, got == "initech")
        r = requests.put(f"{KC}/realms/{REALM}/users/switch-organization", json={"id": ORGS["acme"]},
                         headers={"Authorization": f"Bearer {tok['access_token']}"}) if tok else None
        record("2 contractor", "switch API to a store he is not in", "refused (401)", r and r.status_code,
               r is not None and r.status_code == 401)
        page.goto(auth_url(account_hint="acme"))
        got = outcome(page)
        record("2 contractor", "account_hint=acme, not his store", ("rejected", INVALID), got,
               got == ("rejected", INVALID))
        ctx.close()

        # Use case 3: external IdP with kc_idp_hint and account_hint.
        def partner(email, store):
            def go(pg):
                pg.goto(auth_url(kc_idp_hint="partner", account_hint=store))
                upstream(pg, email)
            return go
        check(b, "3 external IdP", "member of globex", ("in", "globex"), partner("pete@partner.test", "globex"))
        check(b, "3 external IdP", "not a member of globex", ("rejected", INVALID),
              partner("quinn@partner.test", "globex"), "rejected-external-idp")
        check(b, "3 external IdP", "member of globex asks for initech", ("rejected", INVALID),
              partner("pete@partner.test", "initech"))

        # Use case 4: other rejections.
        check(b, "4 rejections", "store that does not exist", ("rejected", INVALID),
              lambda pg: password_login(pg, "paula@mail.test", account_hint="no-such-store"))
        check(b, "4 rejections", "no store at all, asked to pick", ("rejected", NO_ORG),
              lambda pg: password_login(pg, "nina@mail.test", prompt="select_account"), "rejected-no-organization")
        b.close()

    (HERE / "results_cases.json").write_text(json.dumps(RESULTS, indent=2))
    passed = sum(r["pass"] for r in RESULTS)
    print(f"\n{passed}/{len(RESULTS)} checks passed")
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a") as f:
            f.write(f"### Organization login use cases: {passed}/{len(RESULTS)} passed\n\n")
            f.write("| | Case | Check | Expected | Actual |\n|---|---|---|---|---|\n")
            for r in RESULTS:
                f.write(f"| {'✅' if r['pass'] else '❌'} | {r['case']} | {r['check']} | {r['expected']} | {r['actual']} |\n")
    return passed == len(RESULTS)


CAPTION_JS = """
(() => {
  const draw = () => {
    if (!document.body || !window.name) return;
    let el = document.getElementById('__caption');
    if (!el) {
      el = document.createElement('div'); el.id = '__caption';
      el.style.cssText = 'position:fixed;left:0;right:0;top:0;z-index:2147483647;padding:14px 24px;'
        + 'background:#0f172a;color:#f8fafc;font:600 19px/1.35 Inter,system-ui,sans-serif;'
        + 'border-bottom:3px solid #2563eb';
      document.body.appendChild(el);
    }
    el.textContent = window.name;
  };
  document.addEventListener('DOMContentLoaded', draw); draw();
})();
"""


def record_video():
    VIDEO.mkdir(exist_ok=True)
    with sync_playwright() as p:
        b = p.chromium.launch(slow_mo=350, **LAUNCH)
        ctx = b.new_context(viewport={"width": 1100, "height": 720},
                            record_video_dir=str(VIDEO), record_video_size={"width": 1100, "height": 720})
        ctx.add_init_script(CAPTION_JS)
        page = ctx.new_page()

        def caption(text):
            page.evaluate("t => { window.name = t; }", text)
            page.evaluate(CAPTION_JS)

        def app_page(who, how):
            got = outcome(page)
            colour, title = ("#16a34a", f"Signed in to the {got[1]} store") if got[0] == "in" else ("#dc2626", "Refused")
            page.set_content(f"""<body style="margin:0;font-family:Inter,system-ui,sans-serif;background:#f8fafc">
              <div style="max-width:640px;margin:150px auto;background:#fff;border-radius:14px;padding:36px;
                          box-shadow:0 10px 30px rgba(15,23,42,.12);border-top:6px solid {colour}">
              <div style="color:#64748b;font-size:16px">ShopHost store admin</div>
              <div style="font-size:30px;font-weight:700;margin:8px 0 14px">{title}</div>
              <div style="font-size:18px;color:#334155">{who}, {how}</div></div></body>""")
            page.wait_for_timeout(2400)

        def scene(text, run, who, how, rejected=False):
            ctx.clear_cookies()
            page.goto("about:blank")
            page.evaluate("t => { window.name = t; }", text)
            page.goto(auth_url(account_hint="globex"))
            caption(text)
            page.wait_for_timeout(1200)
            run()
            if rejected:
                page.wait_for_load_state()
                caption(text + "  ->  refused")
                page.wait_for_timeout(4000)
            else:
                app_page(who, how)

        def pw(email):
            def go():
                identify(page, email)
                password(page)
            return go

        def via_upstream(email, first_identify):
            def go():
                if first_identify:
                    identify(page, email)
                else:
                    page.locator("a#social-google, a:has-text('Google')").first.click()
                upstream(page, email)
            return go

        page.goto("about:blank")
        scene("1/6  account_hint=globex  ·  Paula, Globex member, password", pw("paula@mail.test"),
              "paula@mail.test", "member, signed in with a password")
        scene("2/6  account_hint=globex  ·  Ivan, invited to Globex, password", pw("ivan@mail.test"),
              "ivan@mail.test", "invitation accepted during sign-in")
        scene("3/6  account_hint=globex  ·  Sam, invited to Globex, Google", via_upstream("sam@gmail.test", False),
              "sam@gmail.test", "invited, signed in with Google")
        scene("4/6  account_hint=globex  ·  Erin, Globex employee, company SSO", via_upstream("erin@globex.test", True),
              "erin@globex.test", "routed to Globex SSO by email domain")
        scene("5/6  account_hint=globex  ·  Nina, not a member, not invited, password", pw("nina@mail.test"),
              "", "", rejected=True)
        scene("6/6  account_hint=globex  ·  Zoe, not invited, Google", via_upstream("zoe@gmail.test", False),
              "", "", rejected=True)
        path = page.video.path()
        ctx.close()
        b.close()
    final = VIDEO / "use-case-1.webm"
    Path(path).replace(final)
    print("video:", final)


if __name__ == "__main__":
    if "--video" in sys.argv:
        record_video()
    else:
        sys.exit(0 if run_checks() else 1)
