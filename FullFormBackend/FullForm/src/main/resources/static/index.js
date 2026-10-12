// Determine backend base URL: works both when served directly from Spring Boot (:8080)
// and when opened via static live-server / file protocol
const isLocalDevServer =
    ['localhost', '127.0.0.1'].includes(window.location.hostname) &&
    window.location.port !== '8080';

const API_BASE = (window.location.protocol === 'file:' || isLocalDevServer)
    ? 'http://localhost:8080'   // opened via file:// or a local live-server
    : '';                       // same origin as the page (Spring Boot or Render)

const form = document.getElementById('demoForm');
const skill = document.getElementById('skill');
const skillOut = document.getElementById('skillOut');
const photoInput = document.getElementById('photo');
const videoInput = document.getElementById('video');
const photoPreview = document.getElementById('photoPreview');
const videoPreview = document.getElementById('videoPreview');

let currentSubmissions = [];
let selectedSubmissionId = null;

// Auth State
let currentAuthUser = null;
let currentAuthMode = 'login'; // 'login' or 'register'

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', () => {
    initAuthState();
    initLiveListeners();
});

/* =========================================================
   AUTHENTICATION WINDOW LOGIC
   ========================================================= */

function initAuthState() {
    const savedUser = sessionStorage.getItem('fullform_auth_user');
    if (savedUser) {
        try {
            currentAuthUser = JSON.parse(savedUser);
            if (currentAuthUser && currentAuthUser.email) {
                unlockApplication(currentAuthUser.email);
                loadSubmissions();
                return;
            }
        } catch (e) {
            sessionStorage.removeItem('fullform_auth_user');
        }
    }

    // If not authenticated, keep application locked and show window
    lockApplication();
}

function lockApplication() {
    document.body.classList.add('auth-locked');
    const overlay = document.getElementById('authOverlay');
    if (overlay) overlay.classList.add('active');
    const sessionBar = document.getElementById('userSessionBar');
    if (sessionBar) sessionBar.style.display = 'none';
}

function unlockApplication(email) {
    document.body.classList.remove('auth-locked');
    const overlay = document.getElementById('authOverlay');
    if (overlay) overlay.classList.remove('active');

    const sessionBar = document.getElementById('userSessionBar');
    const userBadge = document.getElementById('userEmailBadge');
    if (sessionBar && userBadge) {
        userBadge.textContent = `👤 ${email}`;
        sessionBar.style.display = 'flex';
    }
}

function setAuthMode(mode) {
    currentAuthMode = mode;
    const tabLogin = document.getElementById('authTabLogin');
    const tabRegister = document.getElementById('authTabRegister');
    const authTitle = document.getElementById('authTitle');
    const authSubtitle = document.getElementById('authSubtitle');
    const authBtnText = document.getElementById('authBtnText');
    const authToggleHint = document.getElementById('authToggleHint');
    const authToggleBtn = document.getElementById('authToggleBtn');
    const alertBox = document.getElementById('authAlert');

    if (alertBox) alertBox.style.display = 'none';
    clearAuthErrors();

    if (mode === 'login') {
        tabLogin.classList.add('active');
        tabRegister.classList.remove('active');
        authTitle.textContent = 'Actor Portal Sign In';
        authSubtitle.textContent = 'Enter your email and password to enter the application.';
        authBtnText.textContent = 'Sign In & Enter Application';
        authToggleHint.textContent = 'New to the portal?';
        authToggleBtn.textContent = 'Register here';
    } else {
        tabRegister.classList.add('active');
        tabLogin.classList.remove('active');
        authTitle.textContent = 'Register New Actor';
        authSubtitle.textContent = 'Create your account with email and password to enter.';
        authBtnText.textContent = 'Create Account & Enter Application';
        authToggleHint.textContent = 'Already registered?';
        authToggleBtn.textContent = 'Sign In here';
    }
}

function toggleAuthMode() {
    setAuthMode(currentAuthMode === 'login' ? 'register' : 'login');
}

function clearAuthErrors() {
    const emailErr = document.getElementById('authEmailErr');
    const passErr = document.getElementById('authPasswordErr');
    if (emailErr) emailErr.textContent = '';
    if (passErr) passErr.textContent = '';
    const emailInput = document.getElementById('authEmail');
    const passInput = document.getElementById('authPassword');
    if (emailInput) emailInput.classList.remove('invalid');
    if (passInput) passInput.classList.remove('invalid');
}

async function handleAuthSubmit(e) {
    e.preventDefault();
    clearAuthErrors();

    const emailInput = document.getElementById('authEmail');
    const passInput = document.getElementById('authPassword');
    const alertBox = document.getElementById('authAlert');
    const submitBtn = document.getElementById('authSubmitBtn');
    const spinner = document.getElementById('authSpinner');
    const btnText = document.getElementById('authBtnText');

    const email = emailInput.value.trim().toLowerCase();
    const password = passInput.value;

    let hasErr = false;
    if (!email || !email.includes('@')) {
        document.getElementById('authEmailErr').textContent = 'Please enter a valid email address.';
        emailInput.classList.add('invalid');
        hasErr = true;
    }
    if (!password || password.length < 6) {
        document.getElementById('authPasswordErr').textContent = 'Password must be at least 6 characters.';
        passInput.classList.add('invalid');
        hasErr = true;
    }

    if (hasErr) return;

    submitBtn.disabled = true;
    spinner.style.display = 'inline-block';
    const originalText = btnText.textContent;
    btnText.textContent = currentAuthMode === 'login' ? 'Verifying Credentials...' : 'Creating Account...';
    if (alertBox) alertBox.style.display = 'none';

    try {
        const endpoint = currentAuthMode === 'login' ? '/api/v1/auth/login' : '/api/v1/auth/register';
        const response = await fetch(`${API_BASE}${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        let data = {};
        try {
            data = await response.json();
        } catch (ignored) {}

        if (!response.ok) {
            const errMsg = data.message || (currentAuthMode === 'login' ? 'Invalid email or password.' : 'Failed to register account.');
            throw new Error(errMsg);
        }

        // Successfully authenticated!
        currentAuthUser = { email: email };
        sessionStorage.setItem('fullform_auth_user', JSON.stringify(currentAuthUser));

        unlockApplication(email);
        showToast(currentAuthMode === 'login' ? `Welcome back, ${email}!` : `Account created! Welcome, ${email}!`, 'success');

        // Reset auth form inputs
        document.getElementById('authForm').reset();

        // Load submissions into the portal
        loadSubmissions();

    } catch (err) {
        if (alertBox) {
            alertBox.textContent = err.message;
            alertBox.style.display = 'block';
        }
        showToast(err.message, 'error');
    } finally {
        submitBtn.disabled = false;
        spinner.style.display = 'none';
        btnText.textContent = originalText;
    }
}

function handleLogout() {
    if (confirm('Are you sure you want to sign out?')) {
        sessionStorage.removeItem('fullform_auth_user');
        currentAuthUser = null;
        lockApplication();
        setAuthMode('login');
        document.getElementById('authForm').reset();
        showToast('You have signed out successfully.', 'info');
    }
}

/* =========================================================
   PORTAL TAB & UI MANAGEMENT
   ========================================================= */

// Tab Switcher
function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

    if (tab === 'submit') {
        document.getElementById('tabSubmitBtn').classList.add('active');
        document.getElementById('submitTab').classList.add('active');
    } else {
        document.getElementById('tabViewBtn').classList.add('active');
        document.getElementById('viewTab').classList.add('active');
        loadSubmissions();
    }
}

// Live range slider
if (skill && skillOut) {
    skill.addEventListener('input', () => {
        skillOut.textContent = skill.value;
    });
}

// Live Preview for Image and Video inputs
function initLiveListeners() {
    if (photoInput && photoPreview) {
        photoInput.addEventListener('change', () => {
            const file = photoInput.files[0];
            if (file && file.type.startsWith('image/')) {
                const url = URL.createObjectURL(file);
                photoPreview.src = url;
                photoPreview.style.display = 'block';
            } else {
                photoPreview.src = '';
                photoPreview.style.display = 'none';
            }
        });
    }

    if (videoInput && videoPreview) {
        videoInput.addEventListener('change', () => {
            const file = videoInput.files[0];
            if (file && file.type.startsWith('video/')) {
                const url = URL.createObjectURL(file);
                videoPreview.src = url;
                videoPreview.style.display = 'block';
            } else {
                videoPreview.src = '';
                videoPreview.style.display = 'none';
            }
        });
    }
}

// Error handling helper
function setError(el, message) {
    const box = el.closest('.field')?.querySelector('.error-msg');
    if (box) box.textContent = message;
    el.classList.toggle('invalid', Boolean(message));
}

// Form validation (Strictly validates ProspectProfile fields)
function validateForm() {
    let ok = true;

    form.querySelectorAll('input, select, textarea').forEach(el => {
        if (el.type === 'radio' || el.type === 'checkbox' || el.type === 'hidden' || el.type === 'file') return;
        if (el.validity.valid) {
            setError(el, '');
            return;
        }
        ok = false;
        if (el.validity.valueMissing) setError(el, 'This field is required.');
        else if (el.validity.typeMismatch) setError(el, 'Enter a valid ' + el.type + '.');
        else if (el.validity.tooShort) setError(el, 'Use at least ' + el.minLength + ' characters.');
        else if (el.validity.rangeUnderflow || el.validity.rangeOverflow)
            setError(el, 'Enter a value between ' + el.min + ' and ' + el.max + '.');
        else if (el.validity.patternMismatch) setError(el, 'Check format.');
        else setError(el, 'Invalid value.');
    });

    // Radio Gender
    const genderChosen = form.querySelector('input[name=gender]:checked');
    const genderField = form.querySelector('input[name=gender]')?.closest('.field');
    const genderBox = genderField?.querySelector('.error-msg');
    if (genderBox) {
        genderBox.textContent = genderChosen ? '' : 'Please select a gender.';
    }
    if (!genderChosen) ok = false;

    return ok;
}

// Submit Form handler
form.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!currentAuthUser || !currentAuthUser.email) {
        lockApplication();
        showToast('Please sign in before submitting.', 'error');
        return;
    }

    if (!validateForm()) {
        showToast('Please correct form errors before submitting', 'error');
        return;
    }

    const submitBtn = document.getElementById('submitBtn');
    const spinner = document.getElementById('submitSpinner');
    const submitText = document.getElementById('submitText');

    submitBtn.disabled = true;
    spinner.style.display = 'inline-block';
    submitText.textContent = 'Saving to Local Files & Database...';

    const formData = new FormData(form);

    // Automatically associate the submission with the authenticated user's email
    formData.append('email', currentAuthUser.email);

    try {
        const response = await fetch(`${API_BASE}/api/v1/submissions`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            let errorMsg = 'Failed to submit form';
            try {
                const errJson = await response.json();
                errorMsg = errJson.message || errJson.error || errorMsg;
            } catch (ignored) {
                try {
                    const rawText = await response.text();
                    errorMsg = rawText ? rawText.substring(0, 150) : errorMsg;
                } catch (ignored2) {}
            }
            if (errorMsg.length > 200) {
                errorMsg = errorMsg.substring(0, 197) + '...';
            }
            throw new Error(errorMsg);
        }

        const savedData = await response.json();
        showToast('Successfully saved profile, image and video locally!', 'success');

        // Reset form inputs & previews
        form.reset();
        if (photoPreview) { photoPreview.src = ''; photoPreview.style.display = 'none'; }
        if (videoPreview) { videoPreview.src = ''; videoPreview.style.display = 'none'; }
        if (skillOut) skillOut.textContent = '5';
        form.querySelectorAll('.error-msg').forEach(m => m.textContent = '');
        form.querySelectorAll('.invalid').forEach(el => el.classList.remove('invalid'));

        // Refresh submissions and open saved profile
        await loadSubmissions();
        switchTab('view');
        selectSubmission(savedData.id);

    } catch (err) {
        console.error('Submit error:', err);
        showToast(err.message || 'An error occurred while saving submission.', 'error');
    } finally {
        submitBtn.disabled = false;
        spinner.style.display = 'none';
        submitText.textContent = 'Submit & Save';
    }
});

// Reset button
form.addEventListener('reset', () => {
    form.querySelectorAll('.error-msg').forEach(m => m.textContent = '');
    form.querySelectorAll('.invalid').forEach(el => el.classList.remove('invalid'));
    if (skillOut) skillOut.textContent = '5';
    if (photoPreview) { photoPreview.src = ''; photoPreview.style.display = 'none'; }
    if (videoPreview) { videoPreview.src = ''; videoPreview.style.display = 'none'; }
});

// Toast Notifications
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    if (!toast) return;
    const cleanMsg = (typeof message === 'string' && message.length > 200)
        ? message.substring(0, 197) + '...'
        : message;
    toast.textContent = cleanMsg;
    toast.className = `show ${type}`;
    setTimeout(() => {
        toast.className = '';
    }, 4500);
}

// Resolve full media file URL
function resolveFileUrl(path) {
    if (!path) return '';
    if (path.startsWith('http://') || path.startsWith('https://')) return path;
    const clean = path.startsWith('/') ? path : '/' + path;
    return `${API_BASE}${clean}`;
}

// Load Submissions list from Backend
async function loadSubmissions() {
    try {
        const res = await fetch(`${API_BASE}/api/v1/submissions`);
        if (!res.ok) throw new Error('Failed to fetch submissions');
        currentSubmissions = await res.json();

        // Update count badges
        const count = currentSubmissions.length;
        const countBadge = document.getElementById('submissionsCount');
        const countLabel = document.getElementById('listCountLabel');
        if (countBadge) countBadge.textContent = count;
        if (countLabel) countLabel.textContent = `${count} submission${count === 1 ? '' : 's'}`;

        renderSubmissionsList(currentSubmissions);

        // Keep selection or select first
        if (currentSubmissions.length > 0) {
            if (!selectedSubmissionId || !currentSubmissions.some(s => s.id === selectedSubmissionId)) {
                selectSubmission(currentSubmissions[0].id);
            } else {
                selectSubmission(selectedSubmissionId);
            }
        } else {
            showEmptyDetail();
        }
    } catch (err) {
        console.error('Fetch error:', err);
        const subList = document.getElementById('submissionsList');
        if (subList) {
            subList.innerHTML = `<div style="color:var(--muted); padding:12px; font-size:0.85rem;">Backend not connected or no submissions yet.</div>`;
        }
    }
}

// Render Submissions list in sidebar
function renderSubmissionsList(list) {
    const container = document.getElementById('submissionsList');
    if (!container) return;
    if (!list || list.length === 0) {
        container.innerHTML = `<div style="color:var(--muted); padding:12px; font-size:0.85rem;">No submissions found.</div>`;
        return;
    }

    container.innerHTML = list.map(item => {
        const hasImg = Boolean(item.profilePhotoUrl);
        const hasVid = Boolean(item.introVideoUrl);
        const isActive = item.id === selectedSubmissionId ? 'active' : '';

        return `
            <div class="submission-item ${isActive}" onclick="selectSubmission(${item.id})">
                <h4>
                    <span>${escapeHtml(item.fullName || 'Untitled')}</span>
                    <span style="font-size:0.75rem; color:var(--muted);">#${item.id}</span>
                </h4>
                <div class="item-email">${escapeHtml(item.email || 'No email')}</div>
                <div class="item-tags">
                    <span class="item-badge">${escapeHtml(item.status || 'SUBMITTED')}</span>
                    ${hasImg ? '<span class="item-badge has-media">🖼️ Image</span>' : ''}
                    ${hasVid ? '<span class="item-badge has-media">🎬 Video</span>' : ''}
                </div>
            </div>
        `;
    }).join('');
}

// Filter submissions dynamically
function filterSubmissions() {
    const filterInput = document.getElementById('filterInput');
    if (!filterInput) return;
    const q = filterInput.value.toLowerCase().trim();
    if (!q) {
        renderSubmissionsList(currentSubmissions);
        return;
    }
    const filtered = currentSubmissions.filter(s =>
        (s.fullName && s.fullName.toLowerCase().includes(q)) ||
        (s.email && s.email.toLowerCase().includes(q)) ||
        (s.country && s.country.toLowerCase().includes(q)) ||
        (s.framework && s.framework.toLowerCase().includes(q))
    );
    renderSubmissionsList(filtered);
}

// Select and Display a specific Submission
async function selectSubmission(id) {
    selectedSubmissionId = id;

    // Highlight item in sidebar
    document.querySelectorAll('.submission-item').forEach(el => el.classList.remove('active'));
    renderSubmissionsList(currentSubmissions);

    const noSelect = document.getElementById('noSelectionMsg');
    const selectContent = document.getElementById('selectionContent');
    if (noSelect) noSelect.style.display = 'none';
    if (selectContent) selectContent.style.display = 'block';

    try {
        const res = await fetch(`${API_BASE}/api/v1/submissions/${id}`);
        if (!res.ok) throw new Error('Submission not found');
        const data = await res.json();
        populateDetailView(data);
    } catch (err) {
        console.error('Error fetching detail:', err);
        showToast('Error loading submission details', 'error');
    }
}

// Populate the detail panel with text, images, video, and local files
function populateDetailView(data) {
    // Header
    setText('detailName', data.fullName || 'Anonymous');
    setText('detailEmail', data.email || 'No email');
    setText('detailDate', data.createdAt ? new Date(data.createdAt).toLocaleString() : 'Recent');
    setText('detailStatus', data.status || 'SUBMITTED');

    // Local Storage paths
    setText('pathTextFile', data.localDataFilePath || 'uploads/submissions/submission_' + data.id + '.json');
    setText('pathImageFile', data.profilePhotoUrl || 'Not uploaded');
    setText('pathVideoFile', data.introVideoUrl || 'Not uploaded');

    // Image Display
    const imgEl = document.getElementById('detailImage');
    const noImgMsg = document.getElementById('noImageMsg');
    const imgFooter = document.getElementById('detailImageLink');

    if (data.profilePhotoUrl) {
        const fullImgUrl = resolveFileUrl(data.profilePhotoUrl);
        if (imgEl) { imgEl.src = fullImgUrl; imgEl.style.display = 'block'; }
        if (noImgMsg) noImgMsg.style.display = 'none';
        if (imgFooter) imgFooter.innerHTML = `<a href="${fullImgUrl}" target="_blank" style="color:#60a5fa; text-decoration:none;">🔗 Open saved image in new tab</a>`;
    } else {
        if (imgEl) { imgEl.src = ''; imgEl.style.display = 'none'; }
        if (noImgMsg) noImgMsg.style.display = 'block';
        if (imgFooter) imgFooter.innerHTML = '<span>No image file saved</span>';
    }

    // Video Display & Playback
    const vidEl = document.getElementById('detailVideo');
    const noVidMsg = document.getElementById('noVideoMsg');
    const vidFooter = document.getElementById('detailVideoLink');

    if (data.introVideoUrl) {
        const fullVidUrl = resolveFileUrl(data.introVideoUrl);
        if (vidEl) { vidEl.src = fullVidUrl; vidEl.style.display = 'block'; }
        if (noVidMsg) noVidMsg.style.display = 'none';
        if (vidFooter) vidFooter.innerHTML = `<a href="${fullVidUrl}" target="_blank" style="color:#60a5fa; text-decoration:none;">🔗 Direct stream: ${escapeHtml(data.introVideoUrl)}</a>`;
    } else {
        if (vidEl) { vidEl.src = ''; vidEl.style.display = 'none'; }
        if (noVidMsg) noVidMsg.style.display = 'block';
        if (vidFooter) vidFooter.innerHTML = '<span>No video file saved</span>';
    }

    // Personal & Contact Info
    setText('textName', data.fullName);
    setText('textEmail', data.email);
    setText('textPhone', data.phone);
    setText('textWebsite', data.website);
    setText('textCountry', data.country);
    setText('textGender', data.gender);

    // Schedule
    setText('textAge', data.age ? `${data.age} years` : '-');
    setText('textDob', data.dob);
    setText('textSlot', data.slot);
    setText('textMonth', data.graduationMonth);
    setText('textWeek', data.week);
    setText('textMeeting', data.meetingTime);

    // Skills & Preferences
    setText('textSkill', data.skillLevel !== null ? `${data.skillLevel} / 10` : '-');
    setText('textFramework', data.framework);
    setText('textLanguages', data.languages);
    setText('textInterests', data.interests);

    // Colors
    setText('textFavColor', data.favouriteColor || '-');
    const dotFav = document.getElementById('dotFav');
    if (dotFav) dotFav.style.backgroundColor = data.favouriteColor || 'transparent';

    setText('textHatedColor', data.hatedColor || '-');
    const dotHate = document.getElementById('dotHate');
    if (dotHate) dotHate.style.backgroundColor = data.hatedColor || 'transparent';

    // Bio
    setText('textBio', data.bio || 'No bio provided.');

    // Local JSON File Raw Content
    const rawBox = document.getElementById('rawJsonContent');
    const badge = document.getElementById('localFileNameBadge');
    if (badge) badge.textContent = data.localDataFilePath || '';

    if (rawBox) {
        if (data.localTextContent) {
            rawBox.textContent = data.localTextContent;
        } else {
            // Fetch on-demand from local-file endpoint
            fetch(`${API_BASE}/api/v1/submissions/${data.id}/local-file`)
                .then(r => r.text())
                .then(txt => { rawBox.textContent = txt; })
                .catch(() => { rawBox.textContent = JSON.stringify(data, null, 2); });
        }
    }
}

function showEmptyDetail() {
    const noSelect = document.getElementById('noSelectionMsg');
    const selectContent = document.getElementById('selectionContent');
    if (noSelect) noSelect.style.display = 'block';
    if (selectContent) selectContent.style.display = 'none';
}

function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val || '-';
}

// Delete Submission
async function deleteCurrentSubmission() {
    if (!selectedSubmissionId) return;
    if (!confirm(`Are you sure you want to delete submission #${selectedSubmissionId} and its local files?`)) {
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/v1/submissions/${selectedSubmissionId}`, {
            method: 'DELETE'
        });
        if (!res.ok) throw new Error('Failed to delete submission');
        showToast(`Submission #${selectedSubmissionId} deleted`, 'info');
        selectedSubmissionId = null;
        await loadSubmissions();
    } catch (err) {
        showToast('Delete error: ' + err.message, 'error');
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}
