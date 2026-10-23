"""Black-box tests: only HTTP requests in, observable responses out.

Each test records (test id, scenario, input, steps, expected, actual, status)
into test-results/blackbox-cases.json for use in the report.
"""

import json
import os
import unittest
import uuid

from tests.api_client import ApiTestCase, login, request, unique_email, valid_payload

RESULTS_FILE = os.path.join(
    os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))),
    "test-results",
    "blackbox-cases.json",
)
_records = []


def tearDownModule():
    if _records:
        os.makedirs(os.path.dirname(RESULTS_FILE), exist_ok=True)
        with open(RESULTS_FILE, "w", encoding="utf-8") as fh:
            json.dump(sorted(_records, key=lambda r: r["test_id"]), fh, indent=2)


class TestMembersApiBlackBox(ApiTestCase):
    def check(self, test_id, scenario, input_, steps, expected, res, expect_status, expect_code=None):
        """Record the case, then assert on observable behaviour only."""
        passed = res.status == expect_status and (
            expect_code is None or res.error_code == expect_code
        )
        _records.append(
            {
                "test_id": test_id,
                "scenario": scenario,
                "input": input_,
                "steps": steps,
                "expected": expected,
                "actual": res.describe(),
                "status": "Pass" if passed else "Fail",
            }
        )
        self.assertEqual(res.status, expect_status, f"{test_id}: {res.raw}")
        if expect_code:
            self.assertEqual(res.error_code, expect_code, f"{test_id}: {res.raw}")

    def post_invalid(self, test_id, scenario, input_, payload, expect_code="VALIDATION_ERROR"):
        res = self.api("POST", "/api/members", payload)
        self.check(
            test_id, scenario, input_,
            "POST /api/members with the input",
            f"HTTP 400, code={expect_code}", res, 400, expect_code,
        )
        return res

    def test_BB01_create_member_with_valid_data(self):
        payload = valid_payload()
        res = self.api("POST", "/api/members", payload)
        if res.status == 201:
            self.created_ids.append(res.data["id"])
        self.check(
            "BB-01", "Create member with valid data", "All six fields valid",
            "POST /api/members with valid JSON", "HTTP 201 with the new member", res, 201,
        )

    def test_BB02_empty_name_is_rejected(self):
        res = self.post_invalid("BB-02", "Empty full name", 'full_name = ""', valid_payload(full_name=""))
        self.assertIn("full_name", res.body["error"]["fields"])

    def test_BB03_invalid_email_is_rejected(self):
        res = self.post_invalid("BB-03", "Invalid email", 'email = "not-an-email"', valid_payload(email="not-an-email"))
        self.assertIn("email", res.body["error"]["fields"])

    def test_BB04_invalid_phone_is_rejected(self):
        res = self.post_invalid("BB-04", "Invalid phone", 'phone = "98ab"', valid_payload(phone="98ab"))
        self.assertIn("phone", res.body["error"]["fields"])

    def test_BB05_missing_required_fields_are_rejected(self):
        res = self.post_invalid("BB-05", "Missing required fields", "{} (empty object)", {})
        self.assertGreaterEqual(len(res.body["error"]["fields"]), 6)

    def test_BB06_invalid_plan_is_rejected(self):
        res = self.post_invalid("BB-06", "Invalid membership plan", 'membership_plan = "Weekly"', valid_payload(membership_plan="Weekly"))
        self.assertIn("membership_plan", res.body["error"]["fields"])

    def test_BB07_end_date_before_start_date_is_rejected(self):
        self.post_invalid(
            "BB-07", "End date earlier than start date",
            "start 2030-05-01, end 2030-04-01",
            valid_payload(start_date="2030-05-01", end_date="2030-04-01"),
        )

    def test_BB08_duplicate_email_is_rejected(self):
        email = unique_email("dup")
        self.create_member(email=email)
        res = self.api("POST", "/api/members", valid_payload(email=email))
        self.check(
            "BB-08", "Duplicate email", "Same email as an existing member",
            "POST twice with the same email", "HTTP 409, code=DUPLICATE_EMAIL", res, 409, "DUPLICATE_EMAIL",
        )

    def test_BB09_get_nonexistent_member_returns_404(self):
        res = self.api("GET", f"/api/members/{uuid.uuid4()}")
        self.check(
            "BB-09", "Request a nonexistent member", "Random valid UUID",
            "GET /api/members/<random uuid>", "HTTP 404, code=NOT_FOUND", res, 404, "NOT_FOUND",
        )

    def test_BB10_invalid_uuid_returns_400(self):
        res = self.api("GET", "/api/members/not-a-uuid")
        self.check(
            "BB-10", "Invalid UUID format", 'id = "not-a-uuid"',
            "GET /api/members/not-a-uuid", "HTTP 400, code=INVALID_ID", res, 400, "INVALID_ID",
        )

    def test_BB11_update_nonexistent_member_returns_404(self):
        res = self.api("PATCH", f"/api/members/{uuid.uuid4()}", {"phone": "9123456780"})
        self.check(
            "BB-11", "Update a nonexistent member", "Random valid UUID, phone = 9123456780",
            "PATCH /api/members/<random uuid>", "HTTP 404, code=NOT_FOUND", res, 404, "NOT_FOUND",
        )

    def test_BB12_delete_nonexistent_member_returns_404(self):
        res = self.api("DELETE", f"/api/members/{uuid.uuid4()}")
        self.check(
            "BB-12", "Delete a nonexistent member", "Random valid UUID",
            "DELETE /api/members/<random uuid>", "HTTP 404, code=NOT_FOUND", res, 404, "NOT_FOUND",
        )

    def test_BB13_malformed_json_is_rejected(self):
        res = self.api("POST", "/api/members", raw_body="{not json")
        self.check(
            "BB-13", "Malformed JSON body", "{not json",
            "POST /api/members with invalid JSON", "HTTP 400, code=INVALID_JSON", res, 400, "INVALID_JSON",
        )

    def test_BB14_update_with_invalid_phone_is_rejected(self):
        created = self.create_member().data
        res = self.api("PATCH", f"/api/members/{created['id']}", {"phone": "abc"})
        self.check(
            "BB-14", "Update existing member with invalid phone", 'phone = "abc"',
            "Create member, then PATCH it", "HTTP 400, code=VALIDATION_ERROR", res, 400, "VALIDATION_ERROR",
        )

    # ------------------------------------------------ authentication / roles
    def test_BB15_anonymous_cannot_list_members(self):
        res = request("GET", "/api/members")
        self.check(
            "BB-15", "Anonymous visitor lists members", "No login cookie",
            "GET /api/members without logging in", "HTTP 401, code=UNAUTHENTICATED", res, 401, "UNAUTHENTICATED",
        )

    def test_BB16_anonymous_cannot_create_member(self):
        res = request("POST", "/api/members", valid_payload())
        self.check(
            "BB-16", "Anonymous visitor creates a member", "Valid member data, no login",
            "POST /api/members without logging in", "HTTP 401, code=UNAUTHENTICATED", res, 401, "UNAUTHENTICATED",
        )

    def test_BB17_wrong_password_is_rejected(self):
        from tests import test_config

        email = test_config.credentials("admin")[0]
        res = request("POST", "/api/auth/login", {"email": email, "password": "definitely-wrong-password"})
        self.check(
            "BB-17", "Login with wrong password", "Valid admin email, wrong password",
            "POST /api/auth/login", "HTTP 401, code=INVALID_CREDENTIALS", res, 401, "INVALID_CREDENTIALS",
        )

    def test_BB18_anonymous_cannot_list_users(self):
        res = request("GET", "/api/users")
        self.check(
            "BB-18", "Anonymous visitor lists user accounts", "No login cookie",
            "GET /api/users without logging in", "HTTP 401, code=UNAUTHENTICATED", res, 401, "UNAUTHENTICATED",
        )

    def test_BB19_customer_cannot_list_members(self):
        res = request("GET", "/api/members", session=login("customer"))
        self.check(
            "BB-19", "Customer lists all members", "Logged in as customer",
            "GET /api/members as customer", "HTTP 403, code=FORBIDDEN", res, 403, "FORBIDDEN",
        )

    def test_BB20_customer_cannot_delete_member(self):
        res = request("DELETE", f"/api/members/{uuid.uuid4()}", session=login("customer"))
        self.check(
            "BB-20", "Customer deletes a member", "Logged in as customer, random UUID",
            "DELETE /api/members/<uuid> as customer", "HTTP 403, code=FORBIDDEN", res, 403, "FORBIDDEN",
        )

    def test_BB21_trainer_cannot_create_member(self):
        res = request("POST", "/api/members", valid_payload(), session=login("trainer"))
        if res.status == 201:
            self.created_ids.append(res.data["id"])  # clean up if access control is broken
        self.check(
            "BB-21", "Trainer creates a member", "Logged in as trainer, valid member data",
            "POST /api/members as trainer", "HTTP 403, code=FORBIDDEN", res, 403, "FORBIDDEN",
        )

    def test_BB22_trainer_cannot_list_users(self):
        res = request("GET", "/api/users", session=login("trainer"))
        self.check(
            "BB-22", "Trainer lists user accounts", "Logged in as trainer",
            "GET /api/users as trainer", "HTTP 403, code=FORBIDDEN", res, 403, "FORBIDDEN",
        )


if __name__ == "__main__":
    unittest.main()
