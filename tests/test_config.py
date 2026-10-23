"""Configuration for HTTP-level tests (integration and black-box).

These tests talk to a RUNNING Next.js server which writes to a real Supabase
database, so they are opt-in:

  RUN_INTEGRATION_TESTS=1     required, otherwise the tests are skipped
  TEST_ADMIN_EMAIL / TEST_ADMIN_PASSWORD
                              required: an existing account whose profile role
                              is 'admin' (the API is login-protected)
  TEST_TRAINER_EMAIL / TEST_TRAINER_PASSWORD    optional: role 'trainer'
  TEST_CUSTOMER_EMAIL / TEST_CUSTOMER_PASSWORD  optional: role 'customer'
                              (role-restriction tests are skipped without them)
  TEST_BASE_URL               default http://localhost:3000
  ALLOW_REMOTE_TEST_SERVER=1  required if TEST_BASE_URL is not localhost

These can be set in the environment or in .env.local (see .env.example).

Point the server at a dedicated TEST Supabase project (.env.local), never at
production data. The tests only create records with @gymtest.example emails
and only delete the records they created themselves. Passwords are read from
the environment and are never written to files or printed.
"""

import os
from urllib.parse import urlparse


def _load_env_file():
    """Read KEY=VALUE lines from .env.local (real environment variables win)."""
    path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), ".env.local")
    try:
        with open(path, encoding="utf-8") as fh:
            for line in fh:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    key, value = line.split("=", 1)
                    os.environ.setdefault(key.strip(), value.strip())
    except OSError:
        pass  # no .env.local: rely on the real environment


_load_env_file()

BASE_URL = os.environ.get("TEST_BASE_URL", "http://localhost:3000").rstrip("/")
RUN_FLAG = os.environ.get("RUN_INTEGRATION_TESTS") == "1"
ALLOW_REMOTE = os.environ.get("ALLOW_REMOTE_TEST_SERVER") == "1"
TEST_EMAIL_DOMAIN = "gymtest.example"
REQUEST_TIMEOUT_SECONDS = 60  # Allow cold Next.js route compilation on the local PC.


def credentials(role):
    """Return (email, password) for 'admin' / 'trainer' / 'customer', or None."""
    email = os.environ.get(f"TEST_{role.upper()}_EMAIL")
    password = os.environ.get(f"TEST_{role.upper()}_PASSWORD")
    return (email, password) if email and password else None


def is_local_url(url):
    return urlparse(url).hostname in ("localhost", "127.0.0.1", "::1")


def skip_reason():
    """Return why HTTP tests must be skipped, or None when they may run."""
    if not RUN_FLAG:
        return "RUN_INTEGRATION_TESTS=1 is not set (HTTP tests are opt-in)"
    if not is_local_url(BASE_URL) and not ALLOW_REMOTE:
        return f"TEST_BASE_URL={BASE_URL} is not local; set ALLOW_REMOTE_TEST_SERVER=1 to allow"
    if credentials("admin") is None:
        return "TEST_ADMIN_EMAIL / TEST_ADMIN_PASSWORD are not set (an admin test account is required)"
    return None
