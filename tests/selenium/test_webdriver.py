"""Thirty browser-driven STQA cases, grouped to reuse one login per role."""
from urllib.parse import urlparse

from selenium.webdriver.support.ui import Select

from tests import test_config
from tests.selenium.browser_helpers import BrowserCase, admin_request, case, visible


class AAuthenticationTests(BrowserCase):
    def setUp(self):
        super().setUp()
        self.driver.delete_all_cookies()
        self.open('/login', 'GymManager')
        self.wait.until(lambda d: d.find_element('css selector', 'button[type=submit]').is_enabled())

    @case('WD01', 'Reject empty login credentials',
          'Email and password errors appear and no authenticated navigation appears.', 'Authentication')
    def test_WD01_empty_login(self):
        self.submit()
        self.validation('Enter a valid email address', 'Password is required')
        self.assertFalse(self.driver.find_elements('css selector', 'nav'))
        self.assertEqual(urlparse(self.driver.current_url).path, '/login')

    @case('WD02', 'Reject an invalid password',
          'Invalid credentials are reported and the protected area stays inaccessible.', 'Authentication')
    def test_WD02_invalid_credentials(self):
        self.fill('email', test_config.credentials('admin')[0])
        self.fill('password', 'DeliberatelyIncorrect-STQA-Password')
        self.submit()
        self.until_text('Invalid email or password')
        self.assertEqual(urlparse(self.driver.current_url).path, '/login')
        self.assertFalse(self.driver.find_elements('css selector', 'nav'))
        self.observe('Invalid email or password shown; URL remained /login; no authenticated navigation.')

    @case('WD03', 'Logout ends protected browser access',
          'Logout returns to login; a subsequent Members request redirects to login.', 'Authentication')
    def test_WD03_logout(self):
        self.authenticate('admin')
        self.click('Log out')
        self.wait_path('/login')
        self.open('/members')
        self.wait_path('/login')
        self.assertTrue(self.driver.find_element('name', 'password').is_displayed())
        self.observe('Logout and direct /members access both ended at /login.')


class BAdminFunctionalTests(BrowserCase):
    role = 'admin'

    @case('WD04', 'Dashboard reflects active and expired member fixtures',
          'All four counts match stored data and both newly added members appear in Recent members.', 'Dashboard')
    def test_WD04_dashboard_data(self):
        active = self.seed(full_name='WD Active ' + self.token)
        expired = self.seed(full_name='WD Expired ' + self.token,
                            email='expired.' + self.token + '@gymtest.example',
                            start_date='2000-01-01', end_date='2000-02-01')
        members = admin_request('GET', '/api/members').data
        trainers = admin_request('GET', '/api/trainers').data
        expected = {'Total members': len(members),
                    'Active members': sum(m['status'] == 'Active' for m in members),
                    'Expired memberships': sum(m['status'] == 'Expired' for m in members),
                    'Total trainers': len(trainers)}
        self.open('/', 'Dashboard')
        self.loaded()
        actual = {}
        for label, count in expected.items():
            value = self.driver.find_element('xpath', f"//p[normalize-space()='{label}']/following-sibling::p").text
            actual[label] = int(value)
            self.assertEqual(actual[label], count)
        self.row(active['full_name'])
        self.row(expired['full_name'])
        self.observe('Dashboard counts matched API data: ' + str(actual) + '; both known recent records were visible.')

    @case('WD05', 'Navigate between management pages using the sidebar',
          'Members, Trainers, Users and Plans links open the corresponding pages without application errors.', 'Navigation')
    def test_WD05_sidebar_navigation(self):
        self.open('/', 'Dashboard')
        for path, heading in (('/members', 'Members'), ('/trainers', 'Trainers'),
                              ('/users', 'Users'), ('/plans', 'Membership Plans')):
            self.driver.find_element('css selector', f'nav a[href="{path}"]').click()
            self.wait_path(path)
            self.wait.until(lambda d: visible(d, 'css selector', 'h1').text == heading)
            self.loaded()
        self.observe('All four sidebar links opened the intended loaded page.')

    @case('WD06', 'Verify all plan prices and durations',
          'Monthly is INR 1000 for 1 month, Quarterly INR 2700 for 3 months, Yearly INR 9600 for 12 months.', 'Plans')
    def test_WD06_plans(self):
        self.open('/plans', 'Membership Plans')
        actual = []
        for name, price, duration in (('Monthly', '1,000', '1 month'),
                                      ('Quarterly', '2,700', '3 months'), ('Yearly', '9,600', '12 months')):
            card = self.driver.find_element('xpath', f"//h2[normalize-space()='{name}']/parent::div")
            self.assertIn(price, card.text)
            self.assertIn(duration, card.text)
            actual.append(name + ': INR ' + price + ', ' + duration)
        self.observe('; '.join(actual))

    @case('WD07', 'Complete a member create view edit reload delete journey',
          'A valid member is created in the UI, viewed, edited, persists after reload, and is deleted through confirmation.', 'Members')
    def test_WD07_member_journey(self):
        data = self.payload()
        self.owned.append(('members', data['email'], True))
        self.member_form()
        self.fill_contact(data)
        self.submit()
        self.wait_path('/members')
        self.row(data['email']).find_element('css selector', 'a').click()
        self.until_text('Member details')
        self.assertEqual(self.detail('Phone'), data['phone'])
        self.assertEqual(self.detail('Status'), 'Active')
        detail_path = urlparse(self.driver.current_url).path
        self.evidence('created')
        self.click('Edit')
        self.fill('phone', '9123456780')
        self.submit()
        self.wait.until(lambda d: not any(e.is_displayed() for e in d.find_elements('name', 'phone')))
        self.wait.until(lambda d: self.detail('Phone') == '9123456780')
        self.driver.refresh()
        self.until_text('Member details')
        self.assertEqual(self.detail('Phone'), '9123456780')
        self.evidence('edited-reloaded')
        self.click('Delete')
        self.click('Delete', "//*[@role='dialog']")
        self.wait_path('/members')
        self.search(data['email'])
        self.until_text('No members match your search')
        self.assertNotIn(data['email'], '\n'.join(r.text for r in self.driver.find_elements('css selector', 'tbody tr')))
        self.observe('UI create and view succeeded; phone edit persisted after reload; confirmed deletion removed the record. Detail path: ' + detail_path)

    @case('WD08', 'Retain stored member values after page reload',
          'Name, email, phone, plan and dates remain identical after reloading the member detail page.', 'Members')
    def test_WD08_member_persistence(self):
        member = self.seed(membership_plan='Yearly', end_date='2031-01-01')
        self.member_detail(member)
        self.driver.refresh()
        self.until_text('Member details')
        for label, key in (('Email', 'email'), ('Phone', 'phone'), ('Plan', 'membership_plan'),
                           ('Start date', 'start_date'), ('End date', 'end_date')):
            self.assertEqual(self.detail(label), member[key])
        self.observe('All five stored detail values survived a full browser reload.')

    @case('WD09', 'Reject missing required member fields',
          'Blank name, email and phone produce the appropriate field messages and remain unsaved.', 'Members')
    def test_WD09_required_member_fields(self):
        self.member_form()
        self.submit()
        self.validation('Full name is required', 'Enter a valid email address', 'exactly 10 digits')
        self.assertEqual(urlparse(self.driver.current_url).path, '/members/new')

    @case('WD10', 'Reject an invalid member email',
          'Invalid email produces an email field error and the form stays open.', 'Members')
    def test_WD10_invalid_member_email(self):
        self.member_form()
        self.fill_contact(self.payload(email='invalid-address'))
        self.submit()
        self.validation('Enter a valid email address')
        self.assertEqual(urlparse(self.driver.current_url).path, '/members/new')

    @case('WD11', 'Reject a short member phone number',
          'A five-digit phone is rejected with the exact ten-digit rule.', 'Members')
    def test_WD11_invalid_member_phone(self):
        self.member_form()
        self.fill_contact(self.payload(phone='12345'))
        self.submit()
        self.validation('Phone number must contain exactly 10 digits')

    @case('WD12', 'Reject a membership end date before its start',
          'The reversed date range produces an ordering error and no member is saved.', 'Members')
    def test_WD12_reversed_dates(self):
        self.member_form()
        self.fill_contact(self.payload())
        # Set the ISO value of Chromium's segmented date input and dispatch its
        # normal DOM events; the actual form submit and app validation run in UI.
        element = self.driver.find_element('name', 'end_date')
        self.driver.execute_script("""
            const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
            setter.call(arguments[0], '2000-01-01');
            arguments[0].dispatchEvent(new Event('input', {bubbles:true}));
            arguments[0].dispatchEvent(new Event('change', {bubbles:true}));
        """, element)
        self.submit()
        self.validation('End date must not be earlier than start date')

    @case('WD13', 'Reject a duplicate email regardless of letter case',
          'Creating a member with the uppercase form of an existing email is rejected and the original record remains.', 'Members')
    def test_WD13_duplicate_member_email(self):
        member = self.seed()
        self.member_form()
        self.fill_contact(self.payload(email=member['email'].upper()))
        self.submit()
        self.wait.until(lambda d: 'already' in d.find_element('css selector', 'form').text.lower())
        found = admin_request('GET', '/api/members?q=' + member['email']).data
        self.assertEqual([m['id'] for m in found], [member['id']])
        self.observe('Duplicate-email message displayed; exactly the original member remained.')

    @case('WD14', 'Find a member by name and exclude a nonmatch',
          'Name search returns the matching member and excludes an unrelated fixture.', 'Members')
    def test_WD14_search_name(self):
        member = self.seed(full_name='WD Name ' + self.token)
        other = self.seed(full_name='WD Other ' + self.token, email='other.' + self.token + '@gymtest.example')
        self.search(member['full_name'])
        self.row(member['email'])
        def search_settled(driver):
            rows = [r for r in driver.find_elements('css selector', 'tbody tr') if r.is_displayed()]
            return len(rows) == 1 and member['email'] in rows[0].text and other['email'] not in rows[0].text
        self.wait.until(search_settled)
        self.assertEqual(len(self.driver.find_elements('css selector', 'tbody tr')), 1)
        self.observe('Name search returned one matching row and excluded the nonmatching fixture.')

    @case('WD15', 'Find a member by email',
          'A full unique email query returns the correct member row.', 'Members')
    def test_WD15_search_email(self):
        member = self.seed()
        self.search(member['email'])
        row = self.row(member['email'])
        self.assertIn(member['full_name'], row.text)
        self.wait.until(lambda d: len(d.find_elements('css selector', 'tbody tr')) == 1)
        self.observe('Email search returned exactly the intended member.')

    @case('WD16', 'Show an empty result for an unmatched search',
          'An unmatched query shows the no-match message and no member rows.', 'Members')
    def test_WD16_search_no_results(self):
        self.search('no-such-member-' + self.token)
        self.until_text('No members match your search')
        self.assertFalse(self.driver.find_elements('css selector', 'tbody tr'))
        self.observe('No-match message appeared with zero member rows.')

    @case('WD17', 'Show the selected member details accurately',
          'The list link opens that member with the correct identity, plan, dates and active status.', 'Members')
    def test_WD17_member_details(self):
        member = self.seed(membership_plan='Quarterly', end_date='2030-04-01')
        self.open('/members', 'Members')
        self.row(member['email']).find_element('css selector', 'a').click()
        self.until_text('Member details')
        for label, expected in (('Member ID', member['id']), ('Email', member['email']),
                                ('Plan', 'Quarterly'), ('Start date', '2030-01-01'),
                                ('End date', '2030-04-01'), ('Status', 'Active')):
            self.assertEqual(self.detail(label), expected)
        self.observe('List navigation opened the correct ID with all six expected detail values.')

    @case('WD18', 'Persist an edited member phone and plan',
          'The phone and plan change through the UI and remain changed after a reload.', 'Members')
    def test_WD18_edit_member(self):
        member = self.seed()
        self.member_detail(member)
        self.click('Edit')
        self.fill('phone', '9123456780')
        Select(self.driver.find_element('name', 'membership_plan')).select_by_visible_text('Yearly')
        self.submit()
        self.wait.until(lambda d: not any(e.is_displayed() for e in d.find_elements('name', 'phone')))
        self.wait.until(lambda d: self.detail('Phone') == '9123456780')
        self.driver.refresh()
        self.until_text('Member details')
        self.assertEqual(self.detail('Phone'), '9123456780')
        self.assertEqual(self.detail('Plan'), 'Yearly')
        self.assertEqual(self.detail('End date'), '2031-01-01')
        self.observe('After reload: phone 9123456780, Yearly plan, end date 2031-01-01.')

    @case('WD19', 'Cancel deletion without losing the member',
          'Cancel closes the confirmation dialog and the same member remains after reload.', 'Members')
    def test_WD19_cancel_member_delete(self):
        member = self.seed()
        self.member_detail(member)
        self.click('Delete')
        self.until_text('Delete member?')
        self.evidence('confirmation')
        self.click('Cancel', "//*[@role='dialog']")
        self.wait.until(lambda d: not d.find_elements('css selector', '[role=dialog]'))
        self.driver.refresh()
        self.until_text('Member details')
        self.assertEqual(self.detail('Member ID'), member['id'])
        self.observe('Cancellation retained the exact member ID after reload.')

    @case('WD20', 'Confirm deletion and verify the member disappears',
          'Confirm removes the member from the UI search and the stored record returns 404.', 'Members')
    def test_WD20_confirm_member_delete(self):
        member = self.seed()
        self.member_detail(member)
        self.click('Delete')
        self.click('Delete', "//*[@role='dialog']")
        self.wait_path('/members')
        self.search(member['email'])
        self.until_text('No members match your search')
        self.assertEqual(admin_request('GET', '/api/members/' + member['id']).status, 404)
        self.observe('Confirmed deletion: no UI search result and record GET returned HTTP 404.')

    @case('WD21', 'Create a trainer through the form',
          'A valid trainer appears with the submitted name, email, phone and specialization.', 'Trainers')
    def test_WD21_create_trainer(self):
        data = self.payload('trainers')
        self.owned.append(('trainers', data['email'], True))
        self.trainer_form()
        self.fill_contact(data)
        self.fill('specialization', data['specialization'])
        self.submit()
        row = self.row(data['email'])
        for field in ('full_name', 'phone', 'specialization'):
            self.assertIn(data[field], row.text)
        self.assertFalse(self.driver.find_elements('name', 'specialization'))
        self.observe('New trainer row displayed all four submitted contact/specialization values.')

    @case('WD22', 'Validate required trainer name and specialization',
          'A blank trainer form reports required name and specialization.', 'Trainers')
    def test_WD22_required_trainer_fields(self):
        self.trainer_form()
        self.submit()
        self.validation('Full name is required', 'Specialization is required')

    @case('WD23', 'Reject invalid trainer contact details',
          'Invalid email and five-digit phone produce their field errors.', 'Trainers')
    def test_WD23_invalid_trainer_contact(self):
        self.trainer_form()
        self.fill_contact(self.payload('trainers', email='invalid', phone='12345'))
        self.fill('specialization', 'Strength training')
        self.submit()
        self.validation('Enter a valid email address', 'exactly 10 digits')

    @case('WD24', 'Delete a trainer through confirmation',
          'The selected trainer is removed from the table and stored record after confirmation.', 'Trainers')
    def test_WD24_delete_trainer(self):
        trainer = self.seed('trainers')
        self.open('/trainers', 'Trainers')
        self.row(trainer['email']).find_element('css selector', 'button').click()
        self.until_text('Delete trainer?')
        self.evidence('confirmation')
        self.click('Delete', "//*[@role='dialog']")
        self.wait.until(lambda d: not d.find_elements('css selector', '[role=dialog]'))
        self.wait.until(lambda d: trainer['email'] not in '\n'.join(r.text for r in d.find_elements('css selector', 'tbody tr')))
        self.assertEqual(admin_request('GET', '/api/trainers/' + trainer['id']).status, 404)
        self.observe('Trainer row disappeared and record GET returned HTTP 404.')

    @case('WD25', 'List accounts and protect the admin own-role control',
          'The configured three accounts appear; the current admin is marked (you) with no editable role selector.', 'Users')
    def test_WD25_user_list_self_protection(self):
        self.open('/users', 'Users')
        self.loaded()
        for role in ('admin', 'trainer', 'customer'):
            self.row(test_config.credentials(role)[0])
        own = self.row(test_config.credentials('admin')[0])
        self.assertIn('admin (you)', own.text.casefold())
        self.assertFalse(own.find_elements('css selector', 'select'))
        self.assertTrue(self.row(test_config.credentials('trainer')[0]).find_elements('css selector', 'select'))
        self.observe('Three configured accounts listed; own role was noneditable while another account had a role selector.')

    @case('WD26', 'Validate the create-user form without creating an account',
          'Blank name, invalid email and missing password are reported; role choices remain admin, trainer and customer.', 'Users')
    def test_WD26_user_form_validation(self):
        self.open('/users', 'Users')
        self.loaded()
        self.click('Create user')
        self.submit()
        self.validation('Full name is required', 'Enter a valid email address', 'Password must be 8 to 72 characters')
        options = Select(self.driver.find_element('name', 'role')).options
        self.assertEqual({o.text for o in options}, {'admin', 'trainer', 'customer'})
        self.observe('Required-field messages appeared and all three allowed roles were offered; no auth account was created.')


class CTrainerPermissionTests(BrowserCase):
    role = 'trainer'

    @case('WD27', 'Trainer can read a member without write controls',
          'Trainer can view a known member, with no Add member, Edit, Delete or Users control.', 'Trainer permissions')
    def test_WD27_trainer_read_only(self):
        member = self.seed()
        self.open('/members', 'Members')
        self.row(member['email'])
        self.assertFalse(self.driver.find_elements('css selector', 'a[href="/members/new"]'))
        self.row(member['email']).find_element('css selector', 'a').click()
        self.until_text('Member details')
        self.assertEqual(self.detail('Member ID'), member['id'])
        self.assertFalse(self.driver.find_elements('xpath', "//button[normalize-space()='Edit' or normalize-space()='Delete']"))
        self.assertFalse(self.driver.find_elements('css selector', 'nav a[href="/users"]'))
        self.observe('Known member was readable; create/edit/delete controls and Users navigation were absent.')

    @case('WD28', 'Trainer cannot bypass restrictions with direct URLs',
          'Direct Add Member and Users pages both show Access denied with no editable form.', 'Trainer permissions')
    def test_WD28_trainer_direct_urls(self):
        for path in ('/members/new', '/users'):
            self.open(path, 'Access denied')
            self.assertFalse(self.driver.find_elements('css selector', 'form'))
            self.assertIn('trainer', self.body())
        self.observe('Both restricted URLs displayed Access denied and no editable form.')


class DCustomerPermissionTests(BrowserCase):
    role = 'customer'

    @case('WD29', 'Customer sees only the membership linked to their account',
          'Own membership values are visible; an unrelated member is absent and management links are hidden.', 'Customer permissions')
    def test_WD29_customer_own_membership(self):
        email = test_config.credentials('customer')[0]
        members = admin_request('GET', '/api/members').data
        own = next((m for m in members if m['email'].lower() == email.lower()), None)
        if own is None:
            own = self.seed(email=email, full_name='WD Customer ' + self.token)
        other = self.seed(email='foreign.' + self.token + '@gymtest.example', full_name='WD Foreign ' + self.token)
        self.open('/me', 'My Membership')
        self.loaded()
        self.assertEqual(self.detail('Name'), own['full_name'])
        self.assertEqual(self.detail('Plan'), own['membership_plan'])
        self.assertEqual(self.detail('Phone'), own['phone'])
        self.assertNotIn(other['full_name'], self.body())
        for path in ('/members', '/trainers', '/users', '/'):
            self.assertFalse(self.driver.find_elements('css selector', f'nav a[href="{path}"]'))
        self.observe('Own name/plan/phone matched the linked record; unrelated fixture and management links were absent.')

    @case('WD30', 'Customer cannot access management pages or dashboard',
          'Members, Trainers and Users direct URLs are denied; dashboard redirects to My Membership.', 'Customer permissions')
    def test_WD30_customer_direct_urls(self):
        for path in ('/members', '/trainers', '/users'):
            self.open(path, 'Access denied')
            self.assertFalse(self.driver.find_elements('css selector', 'table, form'))
        self.open('/')
        self.wait_path('/me')
        self.until_text('My Membership')
        self.observe('All three management URLs denied access; dashboard redirected to /me.')
