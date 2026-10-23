"""Python reference implementation of the documented business rules.

IMPORTANT: this is a SEPARATE implementation written from the written
specification (see docs/test-plan.md). PyUnit does not import or execute the
TypeScript code in lib/. The unit tests check these rules in isolation; the
HTTP integration/black-box tests check that the real application behaves the
same way.

Rules (must match lib/business/membership.ts and lib/validation/member.ts):
  * Phone: exactly 10 ASCII digits.
  * Email: one '@', no whitespace, domain with at least one dot and no empty
    labels, max 254 characters.
  * Name: not blank after trimming, max 100 characters.
  * Plan: one of Monthly, Quarterly, Yearly (case-sensitive).
  * Dates: real calendar dates as YYYY-MM-DD; end date >= start date.
  * Status: Active if end_date >= current_date (end date itself is Active),
    otherwise Expired.
"""

import re
from datetime import date

MEMBERSHIP_PLANS = ("Monthly", "Quarterly", "Yearly")

_PHONE_RE = re.compile(r"[0-9]{10}")
_EMAIL_RE = re.compile(r"[^\s@]+@[^\s@.]+(\.[^\s@.]+)+")
_DATE_RE = re.compile(r"[0-9]{4}-[0-9]{2}-[0-9]{2}")


def is_valid_phone(phone):
    return isinstance(phone, str) and _PHONE_RE.fullmatch(phone) is not None


def is_valid_email(email):
    return (
        isinstance(email, str)
        and len(email) <= 254
        and _EMAIL_RE.fullmatch(email) is not None
    )


def is_valid_name(name):
    return isinstance(name, str) and len(name.strip()) > 0 and len(name) <= 100


def is_valid_plan(plan):
    return isinstance(plan, str) and plan in MEMBERSHIP_PLANS


def is_valid_date_string(value):
    if not isinstance(value, str) or _DATE_RE.fullmatch(value) is None:
        return False
    try:
        date.fromisoformat(value)
    except ValueError:
        return False
    return True


def validate_membership_dates(start_date, end_date):
    """Return an error message, or None when the range is acceptable."""
    if not is_valid_date_string(start_date):
        return "Start date must be a valid date (YYYY-MM-DD)"
    if not is_valid_date_string(end_date):
        return "End date must be a valid date (YYYY-MM-DD)"
    if end_date < start_date:  # ISO strings compare chronologically
        return "End date must not be earlier than start date"
    return None


def calculate_membership_status(start_date, end_date, current_date):
    error = validate_membership_dates(start_date, end_date)
    if error:
        raise ValueError(error)
    if not is_valid_date_string(current_date):
        raise ValueError("Current date must be a valid date (YYYY-MM-DD)")
    return "Active" if end_date >= current_date else "Expired"


def validate_member_input(data):
    """Return a dict of field -> error message (empty dict means valid)."""
    if not isinstance(data, dict):
        return {"_form": "Request body must be a JSON object"}
    errors = {}
    if not is_valid_name(data.get("full_name")):
        errors["full_name"] = "Full name is required"
    email = data.get("email")
    if not is_valid_email(email.strip() if isinstance(email, str) else email):
        errors["email"] = "Enter a valid email address"
    phone = data.get("phone")
    if not is_valid_phone(phone.strip() if isinstance(phone, str) else phone):
        errors["phone"] = "Phone number must contain exactly 10 digits"
    if not is_valid_plan(data.get("membership_plan")):
        errors["membership_plan"] = "Plan must be Monthly, Quarterly or Yearly"
    # Each date is checked on its own; ordering applies only if both are valid.
    if not is_valid_date_string(data.get("start_date")):
        errors["start_date"] = "Start date must be a valid date (YYYY-MM-DD)"
    if not is_valid_date_string(data.get("end_date")):
        errors["end_date"] = "End date must be a valid date (YYYY-MM-DD)"
    if "start_date" not in errors and "end_date" not in errors:
        order_error = validate_membership_dates(data["start_date"], data["end_date"])
        if order_error:
            errors["end_date"] = order_error
    return errors


def is_valid_member_input(data):
    return not validate_member_input(data)


# ---------------------------------------------------------------- roles
# Must match lib/auth/permissions.ts and lib/validation/auth.ts.
ROLES = ("admin", "trainer", "customer")

PERMISSIONS = {
    "admin": {
        "dashboard:read", "members:read", "members:write",
        "trainers:read", "trainers:write", "users:manage", "me:read",
    },
    "trainer": {"dashboard:read", "members:read", "trainers:read", "me:read"},
    "customer": {"me:read"},
}


def is_valid_role(role):
    return isinstance(role, str) and role in ROLES


def can(role, action):
    """Unknown roles (and non-strings) have no permissions."""
    return is_valid_role(role) and action in PERMISSIONS[role]


def is_valid_password(password):
    return isinstance(password, str) and 8 <= len(password) <= 72


def validate_registration(data):
    """Return a dict of field -> error message (empty dict means valid)."""
    if not isinstance(data, dict):
        return {"_form": "Request body must be a JSON object"}
    errors = {}
    if not is_valid_name(data.get("full_name")):
        errors["full_name"] = "Full name is required"
    email = data.get("email")
    if not is_valid_email(email.strip() if isinstance(email, str) else email):
        errors["email"] = "Enter a valid email address"
    if not is_valid_password(data.get("password")):
        errors["password"] = "Password must be 8 to 72 characters"
    return errors
