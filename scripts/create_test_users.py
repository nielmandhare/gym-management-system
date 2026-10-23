"""Register the demo accounts listed in .env.local through the running app.

Usage (app running on TEST_BASE_URL, schema.sql already applied):
    python scripts/create_test_users.py

Every account is created as a 'customer'. Promote admin and trainer with the
SQL printed at the end. Passwords come from .env.local and are never printed.
"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from tests import api_client, test_config  # noqa: E402

NAMES = {"admin": "Demo Admin", "trainer": "Demo Trainer", "customer": "Demo Customer"}


def main():
    emails = {}
    for role, name in NAMES.items():
        creds = test_config.credentials(role)
        if not creds:
            print(f"{role}: TEST_{role.upper()}_EMAIL / PASSWORD missing in .env.local, skipped")
            continue
        email, password = creds
        emails[role] = email
        res = api_client.request(
            "POST", "/api/auth/register",
            {"full_name": name, "email": email, "password": password},
            session=api_client.Session(),
        )
        if res.status == 201 and res.data and res.data.get("needs_confirmation"):
            print(f"{role}: created but UNCONFIRMED. Turn off 'Confirm email' in Supabase "
                  "(Authentication > Providers > Email), delete this user, and run again.")
        elif res.status == 201:
            print(f"{role}: created ({email})")
        elif res.status == 409:
            print(f"{role}: already exists ({email})")
        else:
            print(f"{role}: FAILED {res.describe()} {res.raw[:200]}")
    if emails:
        print("\nNow run this once in the Supabase SQL Editor:\n")
        for role in ("admin", "trainer"):
            if role in emails:
                print(f"update public.profiles set role = '{role}' where email = '{emails[role]}';")


if __name__ == "__main__":
    main()
