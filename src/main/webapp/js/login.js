const form = document.querySelector('#login-form');
document.querySelector('#show-password').onclick = function () {
  const input = form.elements.password; const show = input.type === 'password';
  input.type = show ? 'text' : 'password'; this.textContent = show ? 'Hide' : 'Show'; this.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
};
document.querySelector('#fill-demo').onclick = () => { form.elements.email.value = 'admin@studentmanagement.local'; form.elements.password.value = 'Admin@123'; };
form.onsubmit = async event => {
  event.preventDefault(); const button = form.querySelector('[type=submit]'); const error = document.querySelector('#login-error');
  button.disabled = true; button.textContent = 'Signing in…'; error.hidden = true;
  try { const response = await fetch('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(Object.fromEntries(new FormData(form))) });
    const data = await response.json(); if (!response.ok) throw new Error(data.error); location.assign('/app.html');
  } catch (e) { error.textContent = e.message || 'Unable to connect. Please try again.'; error.hidden = false; }
  finally { button.disabled = false; button.textContent = 'Sign in →'; }
};
