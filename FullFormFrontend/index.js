const form = document.getElementById('demoForm');
const output = document.getElementById('output');

// Live value for the range slider
const skill = document.getElementById('skill');
skill.addEventListener('input', () => {
    document.getElementById('skillOut').textContent = skill.value;
});

// Show or clear an error under a field
function setError(el, message) {
    const box = el.closest('.field')?.querySelector('.error-msg');
    if (box) box.textContent = message;
    el.classList.toggle('invalid', Boolean(message));
}

// Validate every field using the browser's built-in rules + a few custom ones
function validate() {
    let ok = true;

    form.querySelectorAll('input, select, textarea').forEach(el => {
        if (el.type === 'radio' || el.type === 'checkbox' || el.type === 'hidden') return;
        if (el.validity.valid) { setError(el, ''); return; }
        ok = false;
        if (el.validity.valueMissing) setError(el, 'This field is required.');
        else if (el.validity.typeMismatch) setError(el, 'Enter a valid ' + el.type + '.');
        else if (el.validity.tooShort) setError(el, 'Use at least ' + el.minLength + ' characters.');
        else if (el.validity.rangeUnderflow || el.validity.rangeOverflow)
            setError(el, 'Enter a value between ' + el.min + ' and ' + el.max + '.');
        else if (el.validity.patternMismatch) setError(el, 'Check the format.');
        else setError(el, 'Invalid value.');
    });

    // Radio group
    const genderChosen = form.querySelector('input[name=gender]:checked');
    const genderBox = form.querySelector('input[name=gender]').closest('.field').querySelector('.error-msg');
    genderBox.textContent = genderChosen ? '' : 'Choose one option.';
    if (!genderChosen) ok = false;

    // File size check
    const resume = document.getElementById('resume');
    if (resume.files[0] && resume.files[0].size > 2 * 1024 * 1024) {
        setError(resume, 'File must be under 2 MB.');
        ok = false;
    }

    // Terms checkbox
    const terms = document.getElementById('terms');
    document.getElementById('termsErr').textContent = terms.checked ? '' : 'You must accept the terms.';
    if (!terms.checked) ok = false;

    return ok;
}

form.addEventListener('submit', (e) => {
    e.preventDefault();               // stop the page from reloading
    if (!validate()) return;

    const fd = new FormData(form);    // collects every named field
    const data = {};
    for (const key of new Set(fd.keys())) {
        const values = fd.getAll(key).map(v => v instanceof File ? v.name : v);
        data[key] = values.length > 1 ? values : values[0];
    }
    output.textContent = JSON.stringify(data, null, 2);

    // To send to a server, use:
    // fetch('/api/register', { method: 'POST', body: fd });
});

form.addEventListener('reset', () => {
    form.querySelectorAll('.error-msg').forEach(m => m.textContent = '');
    form.querySelectorAll('.invalid').forEach(el => el.classList.remove('invalid'));
    document.getElementById('skillOut').textContent = '5';
    output.textContent = 'Submitted data will appear here.';
});