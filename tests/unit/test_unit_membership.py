import unittest

from tests.reference_rules import (
    calculate_membership_status,
    is_valid_date_string,
    is_valid_plan,
    validate_membership_dates,
)

# Fixed "today" so results never depend on the real clock.
TODAY = "2025-06-15"


class TestMembershipStatus(unittest.TestCase):
    def test_UT01_end_date_in_future_is_active(self):
        self.assertEqual(calculate_membership_status("2025-06-01", "2025-07-01", TODAY), "Active")

    def test_UT02_end_date_in_past_is_expired(self):
        self.assertEqual(calculate_membership_status("2025-04-01", "2025-05-01", TODAY), "Expired")

    def test_UT03_end_date_equal_to_today_is_active_boundary(self):
        # Documented rule: the end date itself is still Active.
        self.assertEqual(calculate_membership_status("2025-05-15", TODAY, TODAY), "Active")

    def test_UT04_day_after_end_date_is_expired_boundary(self):
        self.assertEqual(calculate_membership_status("2025-05-15", "2025-06-14", TODAY), "Expired")

    def test_UT05_invalid_range_raises_error(self):
        with self.assertRaises(ValueError):
            calculate_membership_status("2025-07-01", "2025-06-01", TODAY)

    def test_UT06_invalid_current_date_raises_error(self):
        with self.assertRaises(ValueError):
            calculate_membership_status("2025-06-01", "2025-07-01", "not-a-date")


class TestMembershipDates(unittest.TestCase):
    def test_UT07_end_date_before_start_date_is_rejected(self):
        self.assertIsNotNone(validate_membership_dates("2025-06-10", "2025-06-09"))

    def test_UT08_end_date_equal_to_start_date_is_accepted(self):
        self.assertIsNone(validate_membership_dates("2025-06-10", "2025-06-10"))

    def test_UT09_end_date_after_start_date_is_accepted(self):
        self.assertIsNone(validate_membership_dates("2025-06-10", "2025-07-10"))

    def test_UT10_malformed_date_is_rejected(self):
        self.assertFalse(is_valid_date_string("15/06/2025"))
        self.assertIsNotNone(validate_membership_dates("15/06/2025", "2025-07-10"))
        self.assertIsNotNone(validate_membership_dates("2025-06-10", "tomorrow"))

    def test_UT11_impossible_calendar_date_is_rejected(self):
        self.assertFalse(is_valid_date_string("2025-02-30"))

    def test_UT12_leap_day_is_accepted_only_in_leap_year(self):
        self.assertTrue(is_valid_date_string("2024-02-29"))
        self.assertFalse(is_valid_date_string("2025-02-29"))


class TestMembershipPlan(unittest.TestCase):
    def test_UT13_each_supported_plan_is_accepted(self):
        for plan in ("Monthly", "Quarterly", "Yearly"):
            with self.subTest(plan=plan):
                self.assertTrue(is_valid_plan(plan))

    def test_UT14_unsupported_plan_is_rejected(self):
        self.assertFalse(is_valid_plan("Weekly"))

    def test_UT15_plan_is_case_sensitive(self):
        self.assertFalse(is_valid_plan("monthly"))

    def test_UT16_empty_or_non_string_plan_is_rejected(self):
        self.assertFalse(is_valid_plan(""))
        self.assertFalse(is_valid_plan(None))
        self.assertFalse(is_valid_plan(1))


if __name__ == "__main__":
    unittest.main()
