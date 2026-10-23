"""Integration tests: Next.js API routes + validation + Supabase + JSON output.

Requires a running server and RUN_INTEGRATION_TESTS=1 (see tests/test_config.py).
"""

import unittest

from tests.api_client import ApiTestCase, login, request, unique_email, valid_payload


class TestMembersApiIntegration(ApiTestCase):
    def test_IT01_create_valid_member(self):
        payload = valid_payload()
        res = self.create_member(**payload)
        member = res.data
        self.assertEqual(member["full_name"], payload["full_name"])
        self.assertEqual(member["email"], payload["email"])
        self.assertEqual(member["membership_plan"], "Monthly")
        self.assertEqual(member["status"], "Active")  # end date is in 2030
        self.assertRegex(member["id"], r"^[0-9a-f-]{36}$")
        self.assertIn("created_at", member)

    def test_IT02_retrieve_created_member(self):
        created = self.create_member().data
        res = self.api("GET", f"/api/members/{created['id']}")
        self.assertEqual(res.status, 200)
        self.assertEqual(res.data["id"], created["id"])
        self.assertEqual(res.data["email"], created["email"])

    def test_IT03_update_member_details(self):
        created = self.create_member().data
        res = self.api(
            "PATCH",
            f"/api/members/{created['id']}",
            {"phone": "9123456780", "membership_plan": "Yearly", "end_date": "2031-01-01"},
        )
        self.assertEqual(res.status, 200, res.raw)
        self.assertEqual(res.data["phone"], "9123456780")
        # The change must be persisted, not just echoed back.
        again = self.api("GET", f"/api/members/{created['id']}")
        self.assertEqual(again.data["membership_plan"], "Yearly")
        self.assertEqual(again.data["end_date"], "2031-01-01")

    def test_IT04_reject_invalid_phone(self):
        res = self.api("POST", "/api/members", valid_payload(phone="12345"))
        self.assertEqual(res.status, 400)
        self.assertEqual(res.error_code, "VALIDATION_ERROR")
        self.assertIn("phone", res.body["error"]["fields"])

    def test_IT05_reject_invalid_date_range(self):
        res = self.api(
            "POST",
            "/api/members",
            valid_payload(start_date="2030-05-01", end_date="2030-04-01"),
        )
        self.assertEqual(res.status, 400)
        self.assertIn("end_date", res.body["error"]["fields"])

    def test_IT06_reject_duplicate_email(self):
        first = self.create_member().data
        res = self.api("POST", "/api/members", valid_payload(email=first["email"]))
        self.assertEqual(res.status, 409)
        self.assertEqual(res.error_code, "DUPLICATE_EMAIL")
        # Uniqueness is case-insensitive.
        res = self.api("POST", "/api/members", valid_payload(email=first["email"].upper()))
        self.assertEqual(res.status, 409)

    def test_IT07_delete_member(self):
        created = self.create_member().data
        res = self.api("DELETE", f"/api/members/{created['id']}")
        self.assertEqual(res.status, 200)
        self.forget(created["id"])

    def test_IT08_deleted_member_cannot_be_retrieved(self):
        created = self.create_member().data
        self.assertEqual(self.api("DELETE", f"/api/members/{created['id']}").status, 200)
        self.forget(created["id"])
        res = self.api("GET", f"/api/members/{created['id']}")
        self.assertEqual(res.status, 404)
        self.assertEqual(res.error_code, "NOT_FOUND")

    def test_IT09_list_members_includes_created_member(self):
        created = self.create_member().data
        res = self.api("GET", "/api/members")
        self.assertEqual(res.status, 200)
        self.assertIsInstance(res.data, list)
        self.assertIn(created["id"], [m["id"] for m in res.data])
        # Search by email fragment finds the member.
        found = self.api("GET", "/api/members?q=" + created["email"].split("@")[0])
        self.assertEqual(found.status, 200)
        self.assertEqual([m["id"] for m in found.data], [created["id"]])

    def test_IT10_login_creates_session_that_identifies_the_admin(self):
        session = login("admin")
        res = request("GET", "/api/auth/me", session=session)
        self.assertEqual(res.status, 200)
        self.assertEqual(res.data["role"], "admin")
        # The session lives in httpOnly cookies; no token is in the JSON body.
        self.assertNotIn("access_token", res.raw)

    def test_IT11_logout_ends_the_session(self):
        session = login("admin")
        self.assertEqual(request("POST", "/api/auth/logout", session=session).status, 200)
        res = request("GET", "/api/auth/me", session=session)
        self.assertEqual(res.status, 401)
        self.assertEqual(res.error_code, "UNAUTHENTICATED")

    def test_IT12_admin_can_list_users_and_sees_own_account(self):
        me = self.api("GET", "/api/auth/me").data
        res = self.api("GET", "/api/users")
        self.assertEqual(res.status, 200)
        self.assertIn(me["id"], [u["id"] for u in res.data])
        self.assertTrue(all(u["role"] in ("admin", "trainer", "customer") for u in res.data))


if __name__ == "__main__":
    unittest.main()
