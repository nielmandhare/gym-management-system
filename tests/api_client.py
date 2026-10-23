"""Shared helpers for HTTP tests, using only the standard library."""

import json
import unittest
import urllib.error
import urllib.request
import uuid
from http.cookies import SimpleCookie

from tests import test_config


class Session:
    """A minimal cookie jar: remembers cookies set by the server."""

    def __init__(self):
        self.cookies = {}

    def update_from(self, headers):
        for header in headers.get_all("Set-Cookie") or []:
            parsed = SimpleCookie()
            parsed.load(header)
            for name, morsel in parsed.items():
                if morsel.value == "" or morsel["max-age"] == "0":
                    self.cookies.pop(name, None)  # server deleted the cookie
                else:
                    self.cookies[name] = morsel.value

    def header(self):
        return "; ".join(f"{k}={v}" for k, v in self.cookies.items())


class Response:
    def __init__(self, status, body, raw):
        self.status = status
        self.body = body  # parsed JSON, or None if the body was not JSON
        self.raw = raw

    @property
    def data(self):
        return (self.body or {}).get("data")

    @property
    def error_code(self):
        return ((self.body or {}).get("error") or {}).get("code")

    def describe(self):
        code = f", code={self.error_code}" if self.error_code else ""
        return f"HTTP {self.status}{code}"


def request(method, path, payload=None, raw_body=None, session=None):
    """Send an HTTP request and return a Response (never raises on 4xx/5xx).

    `session=None` means no cookies are sent (an anonymous visitor).
    """
    data = raw_body.encode() if raw_body is not None else (
        json.dumps(payload).encode() if payload is not None else None
    )
    headers = {"Content-Type": "application/json"}
    if session is not None and session.cookies:
        headers["Cookie"] = session.header()
    req = urllib.request.Request(test_config.BASE_URL + path, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=test_config.REQUEST_TIMEOUT_SECONDS) as res:
            status, raw, resp_headers = res.status, res.read().decode(), res.headers
    except urllib.error.HTTPError as err:
        status, raw, resp_headers = err.code, err.read().decode(), err.headers
    if session is not None:
        session.update_from(resp_headers)
    try:
        body = json.loads(raw)
    except ValueError:
        body = None
    return Response(status, body, raw)


def login(role):
    """Log in with the configured test account for a role; returns a Session."""
    creds = test_config.credentials(role)
    if creds is None:
        raise unittest.SkipTest(f"TEST_{role.upper()}_EMAIL / PASSWORD not set")
    session = Session()
    res = request("POST", "/api/auth/login", {"email": creds[0], "password": creds[1]}, session=session)
    if res.status != 200:
        raise unittest.SkipTest(f"Could not log in as test {role}: {res.describe()}")
    if res.data["role"] != role:
        raise unittest.SkipTest(f"Test {role} account has role '{res.data['role']}', expected '{role}'")
    return session


def unique_email(label="member"):
    return f"pyunit.{label}.{uuid.uuid4().hex[:12]}@{test_config.TEST_EMAIL_DOMAIN}"


def valid_payload(**overrides):
    payload = {
        "full_name": "PyUnit Test Member",
        "email": unique_email(),
        "phone": "9876543210",
        "membership_plan": "Monthly",
        "start_date": "2030-01-01",
        "end_date": "2030-02-01",
    }
    payload.update(overrides)
    return payload


class ApiTestCase(unittest.TestCase):
    """Base class: skips safely, logs in as admin, cleans up only its own records."""

    admin = None

    @classmethod
    def setUpClass(cls):
        reason = test_config.skip_reason()
        if reason:
            raise unittest.SkipTest(reason)
        try:
            cls.admin = login("admin")
        except OSError as err:
            raise unittest.SkipTest(f"Server not reachable at {test_config.BASE_URL}: {err}")
        res = request("GET", "/api/members", session=cls.admin)
        if res.status != 200:
            raise unittest.SkipTest(
                f"GET /api/members as admin returned {res.describe()}; "
                "check .env.local and that supabase/schema.sql was run"
            )

    def setUp(self):
        self.created_ids = []

    def api(self, method, path, payload=None, raw_body=None):
        """Request as the admin test account."""
        return request(method, path, payload, raw_body, session=self.admin)

    def create_member(self, **overrides):
        """Create a member through the API and register it for cleanup."""
        res = self.api("POST", "/api/members", valid_payload(**overrides))
        self.assertEqual(res.status, 201, f"setup create failed: {res.raw}")
        self.created_ids.append(res.data["id"])
        return res

    def forget(self, member_id):
        """Call after a test deleted the member itself."""
        self.created_ids.remove(member_id)

    def tearDown(self):
        # Delete ONLY records this test created. Failures are reported.
        failures = []
        for member_id in self.created_ids:
            try:
                res = self.api("DELETE", f"/api/members/{member_id}")
            except OSError as err:
                failures.append(f"{member_id}: {err}")
                continue
            if res.status != 200:
                failures.append(f"{member_id}: {res.describe()}")
        if failures:
            self.fail("Cleanup failed, remove manually: " + "; ".join(failures))
