import unittest

from tests.reference_rules import (
    can,
    is_valid_password,
    is_valid_role,
    validate_registration,
)

ALL_ACTIONS = (
    "dashboard:read", "members:read", "members:write",
    "trainers:read", "trainers:write", "users:manage", "me:read",
)


class TestRolePermissions(unittest.TestCase):
    def test_UT44_admin_can_perform_every_action(self):
        for action in ALL_ACTIONS:
            with self.subTest(action=action):
                self.assertTrue(can("admin", action))

    def test_UT45_trainer_can_read_members_trainers_and_dashboard(self):
        for action in ("dashboard:read", "members:read", "trainers:read", "me:read"):
            with self.subTest(action=action):
                self.assertTrue(can("trainer", action))

    def test_UT46_trainer_cannot_write_members(self):
        self.assertFalse(can("trainer", "members:write"))

    def test_UT47_trainer_cannot_write_trainers(self):
        self.assertFalse(can("trainer", "trainers:write"))

    def test_UT48_trainer_cannot_manage_users(self):
        self.assertFalse(can("trainer", "users:manage"))

    def test_UT49_customer_can_only_read_own_details(self):
        self.assertTrue(can("customer", "me:read"))
        self.assertEqual([a for a in ALL_ACTIONS if can("customer", a)], ["me:read"])

    def test_UT50_customer_cannot_list_members(self):
        self.assertFalse(can("customer", "members:read"))

    def test_UT51_customer_has_no_dashboard(self):
        self.assertFalse(can("customer", "dashboard:read"))

    def test_UT52_unknown_role_has_no_permissions(self):
        for action in ALL_ACTIONS:
            with self.subTest(action=action):
                self.assertFalse(can("superuser", action))

    def test_UT53_missing_or_non_string_role_has_no_permissions(self):
        for role in (None, 1, ["admin"]):
            with self.subTest(role=role):
                self.assertFalse(can(role, "me:read"))

    def test_UT54_role_names_are_valid_only_when_exact(self):
        for role in ("admin", "trainer", "customer"):
            self.assertTrue(is_valid_role(role))
        for role in ("Admin", "owner", "", None):
            self.assertFalse(is_valid_role(role))


class TestPasswordRule(unittest.TestCase):
    def test_UT55_eight_character_password_is_accepted(self):
        self.assertTrue(is_valid_password("abcd1234"))

    def test_UT56_seven_character_password_is_rejected(self):
        self.assertFalse(is_valid_password("abcd123"))

    def test_UT57_seventy_two_character_password_is_accepted(self):
        self.assertTrue(is_valid_password("a" * 72))

    def test_UT58_seventy_three_character_password_is_rejected(self):
        self.assertFalse(is_valid_password("a" * 73))

    def test_UT59_empty_or_non_string_password_is_rejected(self):
        for value in ("", None, 12345678):
            with self.subTest(value=value):
                self.assertFalse(is_valid_password(value))


class TestRegistrationValidation(unittest.TestCase):
    def valid(self, **overrides):
        data = {"full_name": "Ravi Kumar", "email": "ravi@example.com", "password": "secret123"}
        data.update(overrides)
        return data

    def test_UT60_valid_registration_is_accepted(self):
        self.assertEqual(validate_registration(self.valid()), {})

    def test_UT61_invalid_email_is_reported(self):
        self.assertEqual(list(validate_registration(self.valid(email="ravi"))), ["email"])

    def test_UT62_short_password_is_reported(self):
        self.assertEqual(list(validate_registration(self.valid(password="123"))), ["password"])

    def test_UT63_blank_name_is_reported(self):
        self.assertEqual(list(validate_registration(self.valid(full_name=" "))), ["full_name"])

    def test_UT64_non_object_body_is_rejected(self):
        for body in (None, [], "x"):
            with self.subTest(body=body):
                self.assertEqual(list(validate_registration(body)), ["_form"])


if __name__ == "__main__":
    unittest.main()
