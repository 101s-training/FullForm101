// Determine backend base URL: works both when served directly from Spring Boot (:8080)
// and when opened via static live-server / file protocol
const API_BASE = window.location.origin.includes(':8080')
    ? window.location.origin
    : (window.location.protocol === 'file:' || window.location.port !== '8080')
        ? 'http://localhost:8080'
        : '';

const form = document.getElementById('demoForm');
const skill = document.getElementById('skill');
const skillOut = document.getElementById('skillOut');
const photoInput = document.getElementById('photo');
const videoInput = document.getElementById('video');
const photoPreview = document.getElementById('photoPreview');
const videoPreview = document.getElementById('videoPreview');

let currentSubmissions = [];
let selectedSubmissionId = null;

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', () => {
    initLiveListeners();
    loadSubmissions();
});

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

// Form validation
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
    const genderBox = form.querySelector('input[name=gender]').closest('.field').querySelector('.error-msg');
    genderBox.textContent = genderChosen ? '' : 'Please select a gender.';
    if (!genderChosen) ok = false;

    // Terms
    const terms = document.getElementById('terms');
    const termsErr = document.getElementById('termsErr');
    termsErr.textContent = terms.checked ? '' : 'You must accept the terms.';
    if (!terms.checked) ok = false;

    return ok;
}

// Submit Form handler
form.addEventListener('submit', async (e) => {
    e.preventDefault();
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

    try {
        const response = await fetch(`${API_BASE}/api/v1/submissions`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText || 'Failed to submit form');
        }

        const savedData = await response.json();
        showToast('Successfully saved text, image and video locally!', 'success');

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
        showToast('Error: ' + err.message, 'error');
    } finally {
        submitBtn.disabled = false;
        spinner.style.display = 'none';
        submitText.textContent = 'Submit & Save to Local Files';
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
    toast.textContent = message;
    toast.className = `show ${type}`;
    setTimeout(() => {
        toast.className = '';
    }, 4000);
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
        document.getElementById('submissionsCount').textContent = count;
        document.getElementById('listCountLabel').textContent = `${count} submission${count === 1 ? '' : 's'}`;

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
        document.getElementById('submissionsList').innerHTML =
            `<div style="color:var(--muted); padding:12px; font-size:0.85rem;">Backend not connected or no submissions yet.</div>`;
    }
}

// Render Submissions list in sidebar
function renderSubmissionsList(list) {
    const container = document.getElementById('submissionsList');
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
    const q = document.getElementById('filterInput').value.toLowerCase().trim();
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

    document.getElementById('noSelectionMsg').style.display = 'none';
    document.getElementById('selectionContent').style.display = 'block';

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
    document.getElementById('detailName').textContent = data.fullName || 'Anonymous';
    document.getElementById('detailEmail').textContent = data.email || 'No email';
    document.getElementById('detailDate').textContent = data.createdAt ? new Date(data.createdAt).toLocaleString() : 'Recent';
    document.getElementById('detailStatus').textContent = data.status || 'SUBMITTED';

    // Local Storage paths
    document.getElementById('pathTextFile').textContent = data.localDataFilePath || 'uploads/submissions/submission_' + data.id + '.json';
    document.getElementById('pathImageFile').textContent = data.profilePhotoUrl || 'Not uploaded';
    document.getElementById('pathVideoFile').textContent = data.introVideoUrl || 'Not uploaded';

    // Image Display
    const imgEl = document.getElementById('detailImage');
    const noImgMsg = document.getElementById('noImageMsg');
    const imgFooter = document.getElementById('detailImageLink');

    if (data.profilePhotoUrl) {
        const fullImgUrl = resolveFileUrl(data.profilePhotoUrl);
        imgEl.src = fullImgUrl;
        imgEl.style.display = 'block';
        noImgMsg.style.display = 'none';
        imgFooter.innerHTML = `<a href="${fullImgUrl}" target="_blank" style="color:#60a5fa; text-decoration:none;">🔗 Open saved image in new tab</a>`;
    } else {
        imgEl.src = '';
        imgEl.style.display = 'none';
        noImgMsg.style.display = 'block';
        imgFooter.innerHTML = '<span>No image file saved</span>';
    }

    // Video Display & Playback
    const vidEl = document.getElementById('detailVideo');
    const noVidMsg = document.getElementById('noVideoMsg');
    const vidFooter = document.getElementById('detailVideoLink');

    if (data.introVideoUrl) {
        const fullVidUrl = resolveFileUrl(data.introVideoUrl);
        vidEl.src = fullVidUrl;
        vidEl.style.display = 'block';
        noVidMsg.style.display = 'none';
        vidFooter.innerHTML = `<a href="${fullVidUrl}" target="_blank" style="color:#60a5fa; text-decoration:none;">🔗 Direct stream: ${escapeHtml(data.introVideoUrl)}</a>`;
    } else {
        vidEl.src = '';
        vidEl.style.display = 'none';
        noVidMsg.style.display = 'block';
        vidFooter.innerHTML = '<span>No video file saved</span>';
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
    document.getElementById('dotFav').style.backgroundColor = data.favouriteColor || 'transparent';

    setText('textHatedColor', data.hatedColor || '-');
    document.getElementById('dotHate').style.backgroundColor = data.hatedColor || 'transparent';

    // Bio
    setText('textBio', data.bio || 'No bio provided.');

    // Local JSON File Raw Content
    const rawBox = document.getElementById('rawJsonContent');
    const badge = document.getElementById('localFileNameBadge');
    badge.textContent = data.localDataFilePath || '';

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

function showEmptyDetail() {
    document.getElementById('noSelectionMsg').style.display = 'block';
    document.getElementById('selectionContent').style.display = 'none';
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