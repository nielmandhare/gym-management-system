"""Build selenium/gym-simple-100.side: 100 short Selenium IDE tests.

Credentials are read from .env.local (TEST_*_EMAIL / TEST_*_PASSWORD), so the
generated file contains them: keep it out of version control.
Fills fields with executeScript (type and sendKeys both fail in Firefox: initKeyEvent is not a function).
"""
import json
import os
import uuid

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
env = {}
for line in open(os.path.join(ROOT, ".env.local"), encoding="utf-8"):
    if "=" in line and not line.lstrip().startswith("#"):
        k, v = line.strip().split("=", 1)
        env[k] = v
cred = {r: (env[f"TEST_{r.upper()}_EMAIL"], env[f"TEST_{r.upper()}_PASSWORD"]) for r in ("admin", "trainer", "customer")}
W = "15000"


def c(command, target="", value=""):
    return {"id": str(uuid.uuid4()), "comment": "", "command": command, "target": target, "targets": [], "value": value}


def fill(css, text):
    """Set a React-controlled input without key events (type/sendKeys break in Firefox)."""
    js = ("var el = document.querySelector(%s); "
          "Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set.call(el, %s); "
          "el.dispatchEvent(new Event('input', {bubbles: true}));") % (json.dumps(css), json.dumps(text))
    return c("executeScript", js, "ignored")


def start():
    return [
        c("setWindowSize", "1280x900"),
        c("open", "/login"),
        c("executeAsyncScript",
          "var done = arguments[arguments.length - 1]; fetch('/api/auth/logout', {method: 'POST'}).then(function () { done(1); });",
          "ignored"),
        c("open", "/login"),
        c("waitForElementVisible", "css=form", W),
    ]


def login(role):
    e, p = cred[role]
    return start() + [
        fill("[name='email']", e),
        fill("[name='password']", p),
        c("click", "css=button[type='submit']"),
        c("waitForElementVisible", "css=nav", W),
    ]


def go(path, title):
    return [c("open", path), c("waitForElementVisible", h1(title), W)]


def path_is(p):
    return [c("executeScript", "return window.location.pathname", "path"), c("assert", "path", p)]


def nav(href):
    return f"xpath=//nav//a[@href='{href}']"


def h1(t):
    return f"xpath=//h1[normalize-space()='{t}']"


def err(t):
    return f"xpath=//span[text()='{t}']"


def see(locator):
    return c("waitForElementVisible", locator, W)


SUBMIT = "css=button[type='submit']"
SEARCH = "css=input[aria-label='Search members']"
ROLE = "xpath=//nav//span[contains(@class,'capitalize')]"
tests = []


def T(name, cmds):
    tests.append((name, cmds))


# ---- Login page (1-12)
T("Login page has the correct browser title", start() + [c("assertTitle", "Gym Management System")])
T("Login page shows the GymManager heading", start() + [c("assertText", "css=h1", "GymManager")])
T("Login page shows the subtitle", start() + [c("assertText", "css=h2", "Log in to your account")])
T("Login button is labelled Log in", start() + [c("assertText", SUBMIT, "Log in")])
T("Login page has an email field", start() + [c("assertElementPresent", "name=email")])
T("Login page has a password field", start() + [c("assertElementPresent", "name=password")])
T("Password field hides its text", start() + [c("assertElementPresent", "css=input[name='password'][type='password']")])
T("Login page links to registration", start() + [c("assertText", "xpath=//a[@href='/register']", "Create an account")])
T("Empty login shows the email error", start() + [c("click", SUBMIT), see(err("Enter a valid email address"))])
T("Empty login shows the password error", start() + [c("click", SUBMIT), see(err("Password is required"))])
T("Badly formed email is rejected", start() + [fill("[name='email']", "not-an-email"), c("click", SUBMIT), see(err("Enter a valid email address"))])
T("Wrong password shows an alert", start() + [fill("[name='email']", cred["admin"][0]), fill("[name='password']", "WrongPass#999"), c("click", SUBMIT), see("css=[role='alert']")])


# ---- Register page (13-22)
def reg():
    return start() + [c("open", "/register"), c("waitForElementVisible", "css=form", W)]


T("Register page shows the customer subtitle", reg() + [c("assertText", "css=h2", "Create a customer account")])
T("Register page has a full name field", reg() + [c("assertElementPresent", "name=full_name")])
T("Register page has an email field", reg() + [c("assertElementPresent", "name=email")])
T("Register page has a password field", reg() + [c("assertElementPresent", "name=password")])
T("Register button says Register", reg() + [c("assertText", SUBMIT, "Register")])
T("Register page links back to login", reg() + [c("assertText", "xpath=//a[@href='/login']", "Log in")])
T("Empty registration shows the name error", reg() + [c("click", SUBMIT), see(err("Full name is required (max 100 characters)"))])
T("Empty registration shows the email error", reg() + [c("click", SUBMIT), see(err("Enter a valid email address"))])
T("Short password is rejected", reg() + [fill("[name='full_name']", "Test User"), fill("[name='email']", "short.pw@gymtest.example"), fill("[name='password']", "abc"), c("click", SUBMIT), see(err("Password must be 8 to 72 characters"))])
T("Register link opens the register page", start() + [c("click", "xpath=//a[@href='/register']"), see("name=full_name")] + path_is("/register"))

# ---- Anonymous redirects (23-30)
for p in ["/", "/members", "/members/new", "/trainers", "/users", "/me", "/plans"]:
    T(f"Anonymous visitor to {p} is sent to login", start() + [c("open", p), c("waitForElementVisible", "css=form", W)] + path_is("/login"))
T("Login link on register returns to login", reg() + [c("click", "xpath=//a[@href='/login']"), see("name=password")] + path_is("/login"))


# ---- Admin login and dashboard (31-44)
def A():
    return login("admin")


T("Admin login lands on the dashboard", A() + [see(h1("Dashboard"))] + path_is("/"))
T("Admin sidebar shows the admin role", A() + [c("assertText", ROLE, "admin")])
T("Admin sidebar shows the GymManager name", A() + [c("assertText", "xpath=//nav/div/div[1]", "GymManager")])
for card in ["Total members", "Active members", "Expired memberships", "Total trainers"]:
    T(f"Dashboard shows the {card} card", A() + [see(f"xpath=//p[text()='{card}']")])
T("Dashboard shows the Recent members section", A() + [see("xpath=//h2[text()='Recent members']")])
T("Dashboard subtitle is shown", A() + [see("xpath=//p[text()='Overview of your gym']")])
ADD = "xpath=//a[@href='/members/new' and text()='Add member']"
T("Dashboard has an Add member button", A() + [see(ADD)])
T("Add member button opens the form", A() + [see(ADD), c("click", ADD), see(h1("Add Member"))])
T("Logged-in admin visiting /login goes to the dashboard", A() + [c("open", "/login"), see(h1("Dashboard"))] + path_is("/"))
T("Logged-in admin visiting /register goes to the dashboard", A() + [c("open", "/register"), see(h1("Dashboard"))] + path_is("/"))
T("Admin logout returns to the login page", A() + [c("click", "xpath=//nav//button[text()='Log out']"), c("waitForElementVisible", "css=form", W)] + path_is("/login"))

# ---- Admin sidebar and navigation (45-56)
for href, label in [("/", "Dashboard"), ("/members", "Members"), ("/trainers", "Trainers"), ("/users", "Users"), ("/me", "My Profile"), ("/plans", "Plans")]:
    T(f"Admin sidebar has the {label} link", A() + [c("assertText", nav(href), label)])
for href, title in [("/members", "Members"), ("/trainers", "Trainers"), ("/users", "Users"), ("/plans", "Membership Plans"), ("/me", "My Profile")]:
    T(f"Admin can open {title} from the sidebar", A() + [c("click", nav(href)), see(h1(title))] + path_is(href))
T("Admin Log out button is in the sidebar", A() + [c("assertElementPresent", "xpath=//nav//button[text()='Log out']")])


# ---- Members page (57-64)
def M():
    return A() + go("/members", "Members")


T("Members page has a subtitle", M() + [see("xpath=//p[text()='Search, view, edit or remove gym members']")])
T("Members page has a search box", M() + [c("assertElementPresent", SEARCH)])
T("Search box has a placeholder", M() + [c("assertElementPresent", "css=input[aria-label='Search members'][placeholder='Search by name or email']")])
T("Members page has an Add member button", M() + [c("assertElementPresent", "xpath=//a[@href='/members/new']")])
T("Searching for nonsense shows the empty state", M() + [fill("input[aria-label='Search members']", "zzqxnomatch999"), see("xpath=//p[text()='No members match your search']")])
T("Empty state suggests a different search", M() + [fill("input[aria-label='Search members']", "zzqxnomatch999"), see("xpath=//p[text()='Try a different name or email.']")])
T("Add member button on members page opens the form", M() + [c("click", "xpath=//a[@href='/members/new']"), see(h1("Add Member"))])
T("Admin can open the Members page directly", M() + path_is("/members"))


# ---- Add member form (65-77)
def N():
    return A() + go("/members/new", "Add Member")


T("Add member page shows its subtitle", N() + [see("xpath=//p[text()='Register a new gym member']")])
for f in ["full_name", "email", "phone", "membership_plan", "start_date", "end_date"]:
    T(f"Add member form has the {f} field", N() + [c("assertElementPresent", f"name={f}")])
T("Membership plan defaults to Monthly", N() + [c("assertValue", "name=membership_plan", "Monthly")])
T("Blank member form shows the name error", N() + [c("click", SUBMIT), see(err("Full name is required (max 100 characters)"))])
T("Blank member form shows the email error", N() + [c("click", SUBMIT), see(err("Enter a valid email address"))])
T("Blank member form shows the phone error", N() + [c("click", SUBMIT), see(err("Phone number must contain exactly 10 digits"))])
T("Short phone number is rejected", N() + [fill("[name='phone']", "12345"), c("click", SUBMIT), see(err("Phone number must contain exactly 10 digits"))])
T("Cancel on add member returns to the list", N() + [c("click", "xpath=//button[text()='Cancel']"), see(h1("Members"))] + path_is("/members"))


# ---- Trainers (78-81)
def R():
    return A() + go("/trainers", "Trainers")


ADDT = "xpath=//button[text()='Add trainer']"
T("Trainers page has a subtitle", R() + [see("xpath=//p[text()='Gym trainers and their specializations']")])
T("Trainers page has an Add trainer button", R() + [c("assertElementPresent", ADDT)])
T("Add trainer opens the form", R() + [c("click", ADDT), see("name=specialization")])
T("Blank trainer form shows the specialization error", R() + [c("click", ADDT), see("name=specialization"), c("click", SUBMIT), see(err("Specialization is required"))])


# ---- Users, plans and profile as admin (82-90)
def U():
    return A() + go("/users", "Users")


T("Users page has a Create user button", U() + [c("assertElementPresent", "xpath=//button[text()='Create user']")])
T("Users page lists the admin email", U() + [see(f"xpath=//td[contains(.,'{cred['admin'][0]}')]")])
T("Create user form has a role selector", U() + [c("click", "xpath=//button[text()='Create user']"), see("name=role")])
for plan in ["Monthly", "Quarterly", "Yearly"]:
    T(f"Plans page shows the {plan} plan", A() + go("/plans", "Membership Plans") + [c("assertElementPresent", f"xpath=//h2[text()='{plan}']")])
T("Monthly plan costs 1,000 rupees", A() + go("/plans", "Membership Plans") + [c("assertElementPresent", "xpath=//h2[text()='Monthly']/following-sibling::p[contains(.,'1,000')]")])
T("Yearly plan lasts 12 months", A() + go("/plans", "Membership Plans") + [c("assertElementPresent", "xpath=//h2[text()='Yearly']/following-sibling::p[text()='12 months']")])
T("Admin profile shows the admin email", A() + go("/me", "My Profile") + [see(f"xpath=//dd[text()='{cred['admin'][0]}']")])


# ---- Trainer role (91-96)
def TR():
    return login("trainer")


T("Trainer login lands on the dashboard", TR() + [see(h1("Dashboard"))] + path_is("/"))
T("Trainer sidebar shows the trainer role", TR() + [c("assertText", ROLE, "trainer")])
T("Trainer sidebar has no Users link", TR() + [c("assertElementNotPresent", nav("/users"))])
T("Trainer sees no Add member button", TR() + go("/members", "Members") + [c("assertElementNotPresent", "xpath=//a[@href='/members/new']")])
T("Trainer is denied the add member page", TR() + [c("open", "/members/new"), see(h1("Access denied"))])
T("Trainer is denied the users page", TR() + [c("open", "/users"), see(h1("Access denied"))])


# ---- Customer role (97-100)
def CU():
    return login("customer")


T("Customer login lands on My Membership", CU() + [see(h1("My Membership"))] + path_is("/me"))
T("Customer sidebar shows the customer role", CU() + [c("assertText", ROLE, "customer")])
T("Customer sidebar has no Members link", CU() + [c("assertElementNotPresent", nav("/members"))])
T("Customer visiting the dashboard is sent to My Membership", CU() + [c("open", "/"), see(h1("My Membership"))] + path_is("/me"))

assert len(tests) == 100, len(tests)
out = {
    "id": str(uuid.uuid4()), "version": "2.0", "name": "Gym Management System (simple 100)",
    "url": "http://localhost:3000",
    "tests": [{"id": str(uuid.uuid4()), "name": f"TC{i:03d} {n}", "commands": cm} for i, (n, cm) in enumerate(tests, 1)],
    "suites": [], "urls": ["http://localhost:3000/"], "plugins": [],
}
out["suites"] = [{"id": str(uuid.uuid4()), "name": "All 100", "persistSession": False, "parallel": False,
                  "timeout": 300, "tests": [t["id"] for t in out["tests"]]}]
path = os.path.join(ROOT, "selenium", "gym-simple-100.side")
json.dump(out, open(path, "w", encoding="utf-8"), indent=2)
print("wrote", path, len(tests))
