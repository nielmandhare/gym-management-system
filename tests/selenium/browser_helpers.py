"""Browser interaction and fixture ownership for the STQA WebDriver suite."""
import json
import os
import time
import unittest
import uuid
from datetime import datetime
from pathlib import Path
from urllib.parse import quote, urlparse

from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException, StaleElementReferenceException
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import Select, WebDriverWait

from tests import test_config
from tests.api_client import login, request

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = Path(os.environ.get('WEBDRIVER_OUTPUT', ROOT / 'test-results/webdriver/chrome'))
API_SESSION = None
LOGIN_COUNTS = {}
VERSIONS = {}


def visible(driver, by, locator):
    # Next.js Activity can retain earlier route DOM while hiding it. Interact
    # with the current visible page, not the first cached matching element.
    for element in driver.find_elements(by, locator):
        if element.is_displayed():
            return element
    raise NoSuchElementException('No visible element for ' + locator)


def admin_request(method, path, payload=None):
    global API_SESSION
    if API_SESSION is None:
        API_SESSION = login('admin')
    return request(method, path, payload, session=API_SESSION)


def case(case_id, title, expected, area):
    def decorate(function):
        function.case_info = dict(id=case_id, title=title, expected=expected, area=area)
        function.__doc__ = case_id + ' ' + title
        return function
    return decorate


@unittest.skipUnless(os.environ.get('RUN_SELENIUM_TESTS') == '1',
                     'Use npm.cmd run test:webdriver to run the browser suite')
class BrowserCase(unittest.TestCase):
    role = None

    @classmethod
    def setUpClass(cls):
        browser = os.environ.get('SELENIUM_BROWSER', 'chrome')
        holds = ROOT / 'selenium/local-browser-status.json'
        if holds.exists():
            hold = json.loads(holds.read_text(encoding='utf-8')).get(browser, {})
            if hold.get('blocked'):
                raise RuntimeError(browser + ' is paused: ' + hold['reason'])
        if browser != 'chrome':
            raise RuntimeError('This functional suite is configured for the authorized Chrome run')
        if not test_config.is_local_url(test_config.BASE_URL) and not test_config.ALLOW_REMOTE:
            raise RuntimeError('Remote tests need ALLOW_REMOTE_TEST_SERVER=1')
        options = webdriver.ChromeOptions()
        options.add_argument('--no-first-run')
        options.add_argument('--no-default-browser-check')
        options.set_capability('goog:loggingPrefs', {'browser': 'ALL'})
        if os.environ.get('SELENIUM_HEADLESS') == '1':
            options.add_argument('--headless=new')
        if os.environ.get('CHROME_BINARY'):
            options.binary_location = os.environ['CHROME_BINARY']
        cls.driver = webdriver.Chrome(options=options)
        cls.addClassCleanup(cls.driver.quit)
        cls.driver.set_window_size(1280, 900)
        cls.driver.set_page_load_timeout(90)
        cls.wait = WebDriverWait(cls.driver, 60, poll_frequency=0.25,
                                ignored_exceptions=(NoSuchElementException, StaleElementReferenceException))
        VERSIONS.update(browser=cls.driver.capabilities.get('browserVersion'),
                        driver=cls.driver.capabilities.get('chrome', {}).get('chromedriverVersion', '').split(' ')[0])
        print('Browser group:', cls.__name__, 'role:', cls.role or 'authentication',
              'Chrome:', VERSIONS['browser'], flush=True)
        if cls.role:
            try:
                cls.authenticate(cls.role)
            except Exception:
                OUTPUT.mkdir(parents=True, exist_ok=True)
                cls.driver.save_screenshot(str(OUTPUT / (cls.__name__ + '-setup.png')))
                (OUTPUT / (cls.__name__ + '-browser-log.json')).write_text(
                    json.dumps(cls.driver.get_log('browser'), indent=2), encoding='utf-8')
                raise

    @classmethod
    def authenticate(cls, role):
        creds = test_config.credentials(role)
        if not creds:
            raise RuntimeError('Missing configured credentials for ' + role)
        cls.driver.get(test_config.BASE_URL + '/login')
        cls.wait.until(lambda d: visible(d, 'css selector', 'button[type=submit]').is_enabled())
        print('Login form ready for role:', role, flush=True)
        visible(cls.driver, 'name', 'email').send_keys(creds[0])
        visible(cls.driver, 'name', 'password').send_keys(creds[1])
        visible(cls.driver, 'css selector', 'button[type=submit]').click()
        cls.wait.until(lambda d: d.find_elements('css selector', 'nav'))
        cls.wait.until(lambda d: visible(d, 'css selector', 'h1').text != 'GymManager')
        LOGIN_COUNTS[role] = LOGIN_COUNTS.get(role, 0) + 1
        print('Authenticated browser group:', role, flush=True)

    def setUp(self):
        self.owned = []
        self.notes = []
        self.screenshots = []
        self.cleanup = []
        self.token = uuid.uuid4().hex[:10]
        self.started = time.perf_counter()

    def tearDown(self):
        try:
            self.evidence('result')
        finally:
            failures = []
            for resource, identity, by_email in reversed(self.owned):
                try:
                    if by_email:
                        listing = admin_request('GET', '/api/' + resource +
                                                ('?q=' + quote(identity) if resource == 'members' else ''))
                        if listing.status != 200:
                            raise RuntimeError(listing.describe())
                        ids = [row['id'] for row in listing.data if row['email'].lower() == identity.lower()]
                    else:
                        ids = [identity]
                    for record_id in ids:
                        response = admin_request('DELETE', f'/api/{resource}/{record_id}')
                        if response.status not in (200, 404):
                            raise RuntimeError(record_id + ': ' + response.describe())
                    self.cleanup.append({'resource': resource, 'identity': identity, 'status': 'clean'})
                except Exception as error:
                    failures.append(resource + ' ' + identity + ': ' + str(error))
                    self.cleanup.append({'resource': resource, 'identity': identity, 'status': 'failed'})
            if failures:
                self.fail('Fixture cleanup failed: ' + '; '.join(failures))

    def evidence(self, label):
        info = getattr(getattr(self, self._testMethodName), 'case_info', {})
        filename = info.get('id', self._testMethodName) + '-' + label + '.png'
        OUTPUT.mkdir(parents=True, exist_ok=True)
        target = OUTPUT / filename
        if not self.driver.save_screenshot(str(target)):
            raise RuntimeError('Could not save browser evidence')
        self.screenshots.append(str(target))

    def observe(self, value):
        self.notes.append(value)

    def body(self):
        return self.driver.find_element('tag name', 'body').text

    def until_text(self, text):
        self.wait.until(lambda d: text in d.find_element('tag name', 'body').text)

    def open(self, path, heading=None):
        self.driver.get(test_config.BASE_URL + path)
        if heading:
            self.wait.until(lambda d: visible(d, 'css selector', 'h1').text == heading)
        else:
            self.wait.until(lambda d: d.find_elements('css selector', 'main'))

    def loaded(self):
        self.wait.until(lambda d: not any(
            'Loading' in item.text or 'Redirecting' in item.text
            for item in d.find_elements('css selector', 'main [role=status]')))
        self.assertFalse(self.driver.find_elements('css selector', 'main [role=alert]'),
                         'Unexpected application error while loading data')

    def click(self, text, scope=''):
        xpath = (scope + '//button' if scope else '//button') + f"[normalize-space()='{text}']"
        def action(driver):
            element = visible(driver, 'xpath', xpath)
            if not element.is_enabled() or not element.is_displayed():
                return False
            element.click()
            return True
        self.wait.until(action)

    def fill(self, name, value):
        def action(driver):
            element = visible(driver, 'name', name)
            if not element.is_enabled():
                return False
            element.send_keys(Keys.CONTROL, 'a')
            element.send_keys(Keys.BACKSPACE)
            if value:
                element.send_keys(value)
            return element.get_property('value') == value
        self.wait.until(action)

    def submit(self):
        def action(driver):
            element = visible(driver, 'css selector', 'form button[type=submit]')
            if not element.is_enabled():
                return False
            element.click()
            return True
        self.wait.until(action)

    def field_errors(self):
        return '\n'.join(element.text for element in self.driver.find_elements('css selector', 'form label span.text-red-700'))

    def validation(self, *messages):
        self.wait.until(lambda d: all(message in self.field_errors() for message in messages))
        self.observe('Displayed validation: ' + ' | '.join(messages))

    def payload(self, resource='members', **overrides):
        data = dict(full_name='WD ' + self.token, email='webdriver.' + self.token + '@gymtest.example', phone='9876543210')
        if resource == 'members':
            data.update(membership_plan='Monthly', start_date='2030-01-01', end_date='2030-02-01')
        else:
            data['specialization'] = 'Strength training'
        data.update(overrides)
        return data

    def seed(self, resource='members', **overrides):
        data = self.payload(resource, **overrides)
        response = admin_request('POST', '/api/' + resource, data)
        self.assertEqual(response.status, 201, 'Fixture creation: ' + response.describe())
        self.owned.append((resource, response.data['id'], False))
        return response.data

    def member_form(self):
        self.open('/members/new', 'Add Member')
        self.wait.until(lambda d: d.find_elements('name', 'full_name'))

    def fill_contact(self, data):
        for field in ('full_name', 'email', 'phone'):
            self.fill(field, data[field])

    def member_detail(self, member):
        self.open('/members/' + member['id'], member['full_name'])
        self.loaded()

    def detail(self, label):
        return visible(self.driver, 'xpath', f"//dt[normalize-space()='{label}']/following-sibling::dd").text

    def search(self, term):
        self.open('/members', 'Members')
        self.loaded()
        box = visible(self.driver, 'css selector', 'input[aria-label="Search members"]')
        box.send_keys(term)

    def row(self, text):
        return self.wait.until(lambda d: visible(d, 'xpath', f"//tbody/tr[td[normalize-space()='{text}']]"))

    def trainer_form(self):
        self.open('/trainers', 'Trainers')
        self.loaded()
        self.click('Add trainer')
        self.wait.until(lambda d: d.find_elements('name', 'specialization'))

    def wait_path(self, path):
        self.wait.until(lambda d: urlparse(d.current_url).path == path)
