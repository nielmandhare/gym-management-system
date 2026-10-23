import unittest

from tests.reference_rules import (
    is_valid_email,
    is_valid_member_input,
    is_valid_name,
    is_valid_phone,
    validate_member_input,
)


def valid_member(**overrides):
    data = {
        "full_name": "Asha Patil",
        "email": "asha.patil@example.com",
        "phone": "9876543210",
        "membership_plan": "Monthly",
        "start_date": "2025-06-01",
        "end_date": "2025-07-01",
    }
    data.update(overrides)
    return data


class TestPhoneValidation(unittest.TestCase):
    def test_UT17_valid_ten_digit_phone_is_accepted(self):
        self.assertTrue(is_valid_phone("9876543210"))

    def test_UT18_phone_with_letters_is_rejected(self):
        self.assertFalse(is_valid_phone("98765abc10"))

    def test_UT19_phone_shorter_than_ten_digits_is_rejected(self):
        self.assertFalse(is_valid_phone("987654321"))

    def test_UT20_phone_longer_than_ten_digits_is_rejected(self):
        self.assertFalse(is_valid_phone("98765432101"))

    def test_UT21_phone_with_spaces_or_symbols_is_rejected(self):
        self.assertFalse(is_valid_phone("98765 43210"))
        self.assertFalse(is_valid_phone("+919876543210"))

    def test_UT22_empty_or_non_string_phone_is_rejected(self):
        self.assertFalse(is_valid_phone(""))
        self.assertFalse(is_valid_phone(None))
        self.assertFalse(is_valid_phone(9876543210))

    def test_UT23_non_ascii_digits_are_rejected(self):
        # Arabic-Indic digits are digits to Python's \d but not valid here.
        self.assertFalse(is_valid_phone("٩" * 10))


class TestEmailValidation(unittest.TestCase):
    def test_UT24_valid_email_is_accepted(self):
        self.assertTrue(is_valid_email("asha.patil@example.com"))

    def test_UT25_email_without_at_sign_is_rejected(self):
        self.assertFalse(is_valid_email("asha.example.com"))

    def test_UT26_email_without_domain_dot_is_rejected(self):
        self.assertFalse(is_valid_email("asha@example"))

    def test_UT27_email_with_whitespace_is_rejected(self):
        self.assertFalse(is_valid_email("asha patil@example.com"))

    def test_UT28_email_with_empty_domain_label_is_rejected(self):
        self.assertFalse(is_valid_email("asha@example..com"))
        self.assertFalse(is_valid_email("asha@.com"))

    def test_UT29_empty_or_non_string_email_is_rejected(self):
        self.assertFalse(is_valid_email(""))
        self.assertFalse(is_valid_email(None))

    def test_UT30_overlong_email_is_rejected(self):
        self.assertFalse(is_valid_email("a" * 250 + "@x.com"))


class TestNameValidation(unittest.TestCase):
    def test_UT31_nonblank_name_is_accepted(self):
        self.assertTrue(is_valid_name("Asha Patil"))

    def test_UT32_empty_name_is_rejected(self):
        self.assertFalse(is_valid_name(""))

    def test_UT33_whitespace_only_name_is_rejected(self):
        self.assertFalse(is_valid_name("    "))

    def test_UT34_overlong_name_is_rejected(self):
        self.assertFalse(is_valid_name("A" * 101))

    def test_UT35_missing_name_is_rejected(self):
        self.assertFalse(is_valid_name(None))


class TestMemberInputValidation(unittest.TestCase):
    def test_UT36_correct_member_data_is_accepted(self):
        self.assertTrue(is_valid_member_input(valid_member()))
        self.assertEqual(validate_member_input(valid_member()), {})

    def test_UT37_blank_name_makes_member_invalid(self):
        errors = validate_member_input(valid_member(full_name="  "))
        self.assertEqual(list(errors), ["full_name"])

    def test_UT38_unsupported_plan_makes_member_invalid(self):
        errors = validate_member_input(valid_member(membership_plan="Weekly"))
        self.assertEqual(list(errors), ["membership_plan"])

    def test_UT39_end_before_start_makes_member_invalid(self):
        errors = validate_member_input(valid_member(start_date="2025-07-01", end_date="2025-06-01"))
        self.assertEqual(list(errors), ["end_date"])

    def test_UT40_bad_phone_and_email_are_both_reported(self):
        errors = validate_member_input(valid_member(phone="123", email="nope"))
        self.assertEqual(sorted(errors), ["email", "phone"])

    def test_UT41_missing_required_fields_are_all_reported(self):
        errors = validate_member_input({})
        self.assertEqual(
            sorted(errors),
            ["email", "end_date", "full_name", "membership_plan", "phone", "start_date"],
        )

    def test_UT42_malformed_request_body_is_rejected(self):
        for body in (None, [], "text", 42):
            with self.subTest(body=body):
                self.assertEqual(list(validate_member_input(body)), ["_form"])

    def test_UT43_surrounding_spaces_in_email_and_phone_are_trimmed(self):
        self.assertTrue(is_valid_member_input(valid_member(email=" a@b.com ", phone=" 9876543210 ")))


if __name__ == "__main__":
    unittest.main()
