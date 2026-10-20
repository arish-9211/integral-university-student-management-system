'use strict';
const $ = selector => document.querySelector(selector);
const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
const paths = { dashboard:'M3 3h7v7H3z M14 3h7v7h-7z M3 14h7v7H3z M14 14h7v7h-7z', students:'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M16 3a4 4 0 0 1 0 8 M22 21v-2a4 4 0 0 0-3-3.87 M13 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0', courses:'M4 3h7a3 3 0 0 1 3 3v15a4 4 0 0 0-4-2H4z M14 6a3 3 0 0 1 3-3h5v16h-5a4 4 0 0 0-3 2', enrollments:'M8 3H5v18h14V3h-3 M9 2h6v4H9z M8 12l3 3 5-5', attendance:'M4 5h16v16H4z M8 2v6 M16 2v6 M4 10h16 M8 15l3 3 5-5', results:'M4 21h16 M7 17V9 M12 17V3 M17 17v-5', reports:'M5 3h10l4 4v14H5z M14 3v5h5 M8 12h8 M8 16h8', profile:'M20 21a8 8 0 0 0-16 0 M16 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0' };
const icon = name => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="${paths[name] || paths.students}"/></svg>`;
const names = { dashboard:'Dashboard', students:'Students', courses:'Courses', enrollments:'Enrollments', attendance:'Attendance', results:'Results', reports:'Reports', profile:'Profile' };
const singular = { students:'Student', courses:'Course', enrollments:'Enrollment', attendance:'Attendance', results:'Result' };
let csrf = '', user, current = 'dashboard', page = 1, listData = [], generation = 0, editing = null;
let lookup = { students: [], courses: [] };
let programs = [];
const filters = {};
const badge = value => `<span class="badge ${['ACTIVE','PRESENT','A+','A','Excellent'].includes(value)?'green':['ABSENT','F','Attendance Warning'].includes(value)?'red':['LATE','INACTIVE','D'].includes(value)?'amber':'slate'}">${escapeHtml(value?.replaceAll('_',' '))}</span>`;
const date = value => value ? new Date(value.length === 10 ? value+'T12:00:00' : value).toLocaleDateString('en-IN', {day:'numeric',month:'short',year:'numeric'}) : '—';
const initials = (first,last='') => escapeHtml((first?.[0] || '')+(last?.[0] || ''));
async function api(path, options={}) {
  const response = await fetch('/api/'+path, {...options, headers:{'Content-Type':'application/json', 'X-CSRF-Token':csrf, ...options.headers}});
  const data = await response.json();
  if (response.status === 401) { location.assign('/login.html'); throw new Error('Session expired. Please sign in.'); }
  if (!response.ok) throw new Error(data.error || 'The request failed. Please try again.');
  return data;
}
function toast(message, error=false) { const item=document.createElement('div'); item.className='toast'+(error?' error':''); item.textContent=message; $('#toasts').append(item); setTimeout(()=>item.remove(),5000); }
function pageHeading(title, subtitle, action='') { return `<div class="page-heading"><div><div class="eyebrow">STUDENT MANAGEMENT SYSTEM</div><h1>${title}</h1><p class="muted">${subtitle}</p></div>${action}</div>`; }
function empty(message='No records found', hint='Try different filters or add your first record.') { return `<div class="empty-state">${icon(current)}<h3>${message}</h3><p>${hint}</p></div>`; }
function setContent(html) { $('#content').innerHTML=html; }
function bars(data, color='emerald') { const entries=Object.entries(data); const max=Math.max(1,...entries.map(x=>x[1])); return entries.length ? entries.map(([label,value])=>`<div class="bar-row"><span>${escapeHtml(label)}</span><div class="bar-track"><i class="${color}" style="width:${Math.max(2,value/max*100)}%"></i></div><strong>${value}</strong></div>`).join('') : '<p class="muted">No data recorded yet.</p>'; }
async function dashboard(token) {
  const d=await api('dashboard'); if(token!==generation)return;
  const cards=[['Total students',d.stats.totalStudents,'students','All student records'],['Active students',d.stats.activeStudents,'students','Currently studying'],['Total courses',d.stats.totalCourses,'courses','Academic catalog'],['Enrollments',d.stats.totalEnrollments,'enrollments','Student–course connections'],['Avg. attendance',d.stats.averageAttendance+'%','attendance','Present + late / recorded classes'],['Avg. marks',d.stats.averageMarks+'%','results','Mean of recorded exam percentages']];
  setContent(pageHeading('Academic overview','Your campus at a glance. Every record, connected.', '<a href="#students" class="button primary">+ Add student</a>')+
    `<section class="welcome-banner"><div><span class="eyebrow light">YOUR ACADEMIC WORKSPACE</span><h2>Good to see you, ${escapeHtml(user.name.split(' ')[0])}.</h2><p>A little organization makes room for a lot of potential.</p><a href="#reports">Explore academic reports <span>↗</span></a></div><div class="banner-art" aria-hidden="true"><div class="orbit one"></div><div class="orbit two"></div><div class="art-book">${icon('courses')}</div><span class="art-dot dot-one"></span><span class="art-dot dot-two"></span></div></section>`+
    `<section class="stats-grid">${cards.map(([label,value,ic,caption])=>`<article class="stat-card"><div class="stat-top"><span>${label}</span><span class="stat-icon">${icon(ic)}</span></div><strong class="stat-number">${value}</strong><p>${caption}</p></article>`).join('')}</section>`+
    `<section class="dashboard-grid"><article class="panel"><div class="panel-heading"><div><h2>Students by program</h2><p>Enrollment across academic programs</p></div><span class="tiny-label">LIVE DATA</span></div><div class="chart-bars">${bars(d.programs)}</div><div class="chart-caption"><span class="legend-dot"></span> Total registered students</div></article><article class="panel"><div class="panel-heading"><div><h2>Grade distribution</h2><p>Performance across recorded exams</p></div>${icon('results')}</div><div class="grade-chart">${['A+','A','B','C','D','F'].map(g=>`<div><strong>${d.grades[g]||0}</strong><div class="grade-track"><i style="height:${Math.max(3,(d.grades[g]||0)/Math.max(1,...Object.values(d.grades))*100)}%" class="grade-${g==='F'?'fail':'pass'}"></i></div><span>${g}</span></div>`).join('')}</div></article></section>`+
    `<section class="dashboard-grid lower"><article class="panel"><div class="panel-heading"><div><h2>Recently added students</h2><p>The newest members of your academic community</p></div><a class="text-link" href="#students">View all →</a></div><div class="table-scroll"><table><thead><tr><th>Student</th><th>Program</th><th>Status</th><th></th></tr></thead><tbody>${d.recentStudents.map(s=>`<tr><td>${studentCell(s)}</td><td>${escapeHtml(s.program)}</td><td>${badge(s.status)}</td><td><a class="row-link" aria-label="View ${escapeHtml(s.firstName)}" href="#student/${s.id}">↗</a></td></tr>`).join('')}</tbody></table></div>${d.recentStudents.length?'':empty('No students yet','Add a student to get started.')}</article><article class="panel"><div class="panel-heading"><div><h2>Recent activity</h2><p>Latest changes to your records</p></div></div><div class="activity-list">${d.activity.map(a=>`<div class="activity-item"><span class="activity-dot"></span><div><strong>${escapeHtml(a.title)}</strong><p>${escapeHtml(a.detail)}</p><small>${date(a.at)}</small></div></div>`).join('') || '<p class="muted">Your recent changes will appear here.</p>'}</div></article></section>`+
    `<section class="quick-actions"><span>KEEP THINGS MOVING</span><a href="#enrollments">${icon('enrollments')} Manage enrollments →</a><a href="#attendance">${icon('attendance')} Record attendance →</a><a href="#results">${icon('results')} Update results →</a></section>`);
}
function studentCell(s) {return `<div class="person-cell"><span class="avatar soft">${initials(s.firstName,s.lastName)}</span><div><strong>${escapeHtml(s.firstName+' '+s.lastName)}</strong><small>${escapeHtml(s.enrollmentNumber)}</small></div></div>`;}
async function allRecords(module) {let items=[], p=1, pages=1; do { const d=await api(`${module}?size=100&page=${p}&sort=firstName&direction=asc`); items.push(...d.items);pages=d.pages;p++; }while(p<=pages);return items;}
async function loadLookups() { const [students,courses]=await Promise.all([allRecords('students'),allRecords('courses')]);lookup={students,courses}; }
function option(value,label,selected) {return `<option value="${escapeHtml(value)}" ${String(value)===String(selected)?'selected':''}>${escapeHtml(label)}</option>`;}
function selectFilter(field,label,options) {return `<label class="filter-label"><span>${label}</span><select data-filter="${field}" aria-label="${label}">${option('','All '+label.toLowerCase(),filters[field])}${options.map(x=>option(Array.isArray(x)?x[0]:x,Array.isArray(x)?x[1]:x,filters[field])).join('')}</select></label>`;}
function toolbar() {
  let out=`<div class="filters"><label class="search-field"><span aria-hidden="true">⌕</span><input id="search" type="search" value="${escapeHtml(filters.q||'')}" placeholder="${current==='students'?'Search name, enrollment or email…':current==='courses'?'Search course code or name…':current==='results'?'Search exam name…':'Search date or status…'}" aria-label="Search ${current}" maxlength="100"></label>`;
  if(current==='enrollments')out='<div class="filters">';
  if(current==='students')out+=selectFilter('program','Programs',programs);
  if(['students','courses'].includes(current))out+=selectFilter('semester','Semesters',Array.from({length:12},(_,i)=>i+1))+selectFilter('status','Statuses',current==='students'?['ACTIVE','INACTIVE','GRADUATED']:['ACTIVE','INACTIVE']);
  if(['enrollments','attendance','results'].includes(current))out+=selectFilter('studentId','Students',lookup.students.map(s=>[s.id,`${s.firstName} ${s.lastName} · ${s.enrollmentNumber}`]))+selectFilter('courseId','Courses',lookup.courses.map(c=>[c.id,`${c.courseCode} · ${c.courseName}`]));
  if(current==='attendance')out+=`<label class="filter-label"><span>Date</span><input type="date" data-filter="date" value="${escapeHtml(filters.date||'')}" aria-label="Attendance date"></label>`+selectFilter('status','Statuses',['PRESENT','ABSENT','LATE']);
  if(current==='results')out+=selectFilter('grade','Grades',['A+','A','B','C','D','F']);
  return out+'<button class="button secondary compact" id="apply-filters">Apply</button><button class="text-button" id="clear-filters">Reset</button></div>';
}
const columns={students:['Student','Contact','Program','Semester','Status','Actions'],courses:['Course','Credits','Semester','Status','Actions'],enrollments:['Student','Course','Enrolled on','Actions'],attendance:['Student','Course','Date','Status','Actions'],results:['Student','Course / Exam','Date','Marks','Percentage','Grade','Actions']};
function rowCells(r) {
  const person=`<strong>${escapeHtml(r.studentName)}</strong><small>${escapeHtml(r.enrollmentNumber)}</small>`;
  const course=`<strong>${escapeHtml(r.courseName)}</strong><small>${escapeHtml(r.courseCode)}</small>`;
  if(current==='students')return [studentCell(r),`<span>${escapeHtml(r.email)}</span><small>${escapeHtml(r.phone)}</small>`,escapeHtml(r.program),`Semester ${r.semester}`,badge(r.status)];
  if(current==='courses')return [`<strong>${escapeHtml(r.courseName)}</strong><small>${escapeHtml(r.courseCode)}</small>`,`${r.credits} credits`,`Semester ${r.semester}`,badge(r.status)];
  if(current==='enrollments')return [person,course,date(r.createdAt)];
  if(current==='attendance')return [person,course,date(r.date),badge(r.status)];
  return [person,`<strong>${escapeHtml(r.courseCode)} · ${escapeHtml(r.examName)}</strong><small>${escapeHtml(r.courseName)}</small>`,date(r.examDate),`${r.obtainedMarks} <span class="muted">/ ${r.maximumMarks}</span>`,`${r.percentage}%`,badge(r.grade)];
}
async function listing(token) {
  if (current === 'students') programs = await api('programs');
  else if (['enrollments','attendance','results'].includes(current)) await loadLookups();
  const params=new URLSearchParams({...filters,page});const d=await api(current+'?'+params);if(token!==generation)return;listData=d.items;
  const subtitles={students:'A home for every student record. Search, organize, and keep details up to date.',courses:'Build a clear academic catalog for your students.',enrollments:'Connect students with the courses that shape their learning.',attendance:'Every class counts. Keep an accurate record of participation.',results:'Turn assessment records into a clear view of academic progress.'};
  const actionLabels={students:'Add student',courses:'Add course',enrollments:'Enroll student',attendance:'Mark attendance',results:'Add result'};
  setContent(pageHeading(names[current],subtitles[current],`<button id="add-record" class="button primary">+ ${actionLabels[current]}</button>`)+
    `<section class="panel list-panel"><div class="panel-heading"><div class="inline-heading"><h2>All ${current}</h2><span class="count-pill">${d.total} records</span></div>${['students','courses'].includes(current)?`<label class="sort-label">Sort by <select id="sort" aria-label="Sort records">${[['createdAt','Newest first'],[current==='students'?'firstName':'courseName','Name A–Z'],[current==='students'?'enrollmentNumber':'courseCode','Code A–Z']].map(([v,l])=>option(v,l,filters.sort||'createdAt')).join('')}</select></label>`:''}</div>${toolbar()}<div class="table-scroll"><table><thead><tr>${columns[current].map(c=>`<th>${c}</th>`).join('')}</tr></thead><tbody>${d.items.map(r=>`<tr>${rowCells(r).map(cell=>`<td>${cell}</td>`).join('')}<td><div class="row-actions">${current==='students'?`<a href="#student/${r.id}" class="row-link">View</a>`:''}${current==='courses'?`<button class="text-button" data-enrollments="${r.id}">Students</button>`:''}${current!=='enrollments'?`<button class="text-button" data-edit="${r.id}">Edit</button>`:''}<button class="text-button delete-button" data-delete="${r.id}" aria-label="Delete ${escapeHtml(r.enrollmentNumber||r.courseCode||singular[current])}">Delete</button></div></td></tr>`).join('')}</tbody></table></div>${d.items.length?'':empty()}<div class="pagination"><span>${d.total?`Showing ${(page-1)*10+1}–${Math.min(page*10,d.total)} of ${d.total}`:'0 records'}</span><div><button class="button secondary compact" id="previous" ${page===1?'disabled':''}>← Previous</button><span>Page ${page} of ${d.pages}</span><button class="button secondary compact" id="next" ${page>=d.pages?'disabled':''}>Next →</button></div></div></section>${current==='attendance'?'<p class="help-note">Late counts as attended. Excellent ≥ 90% · Good ≥ 75% · Warning below 75%. Only recorded classes enter the calculation.</p>':current==='results'?'<p class="help-note">Grades are calculated in Java: A+ ≥ 90 · A ≥ 80 · B ≥ 70 · C ≥ 60 · D ≥ 50 · F below 50.</p>':''}`);
  $('#add-record').onclick=()=>openEditor();
  $('#apply-filters').onclick=applyFilters;$('#search')?.addEventListener('keydown',e=>{if(e.key==='Enter')applyFilters();});
  document.querySelectorAll('[data-filter]').forEach(el=>el.onchange=applyFilters);
  $('#clear-filters').onclick=()=>{Object.keys(filters).forEach(k=>delete filters[k]);page=1;render();};
  $('#sort')?.addEventListener('change',e=>{filters.sort=e.target.value;filters.direction=e.target.value==='createdAt'?'desc':'asc';page=1;render();});
  $('#previous').onclick=()=>{page--;render();};$('#next').onclick=()=>{page++;render();};
  document.querySelectorAll('[data-edit]').forEach(b=>b.onclick=()=>openEditor(listData.find(r=>r.id===b.dataset.edit)));
  document.querySelectorAll('[data-delete]').forEach(b=>b.onclick=()=>confirmDelete(b.dataset.delete));
  document.querySelectorAll('[data-enrollments]').forEach(b=>b.onclick=()=>{location.hash='enrollments?courseId='+b.dataset.enrollments;});
}
function applyFilters() { if($('#search'))filters.q=$('#search').value.trim();document.querySelectorAll('[data-filter]').forEach(el=>filters[el.dataset.filter]=el.value);page=1;render(); }
const field=(key,label,type='text',extra={})=>({key,label,type,...extra});
function definitions() {
  const studentOptions=lookup.students.map(s=>[s.id,`${s.enrollmentNumber} · ${s.firstName} ${s.lastName}`]);
  const courseOptions=lookup.courses.map(c=>[c.id,`${c.courseCode} · ${c.courseName}`]);
  const refs=[field('studentId','Student','select',{options:studentOptions}),field('courseId','Course','select',{options:courseOptions})];
  return {
    students:[field('enrollmentNumber','Enrollment number','text',{max:30,placeholder:'SMS-2026-021'}),field('status','Status','select',{options:['ACTIVE','INACTIVE','GRADUATED']}),field('firstName','First name','text',{max:60}),field('lastName','Last name','text',{max:60}),field('gender','Gender','select',{options:['FEMALE','MALE','OTHER','PREFER_NOT_TO_SAY']}),field('dateOfBirth','Date of birth','date'),field('email','Email address','email',{max:120}),field('phone','Phone number','tel',{max:20}),field('program','Program','text',{max:80,placeholder:'BCA'}),field('semester','Semester','number',{min:1,max:12}),field('admissionDate','Admission date','date'),field('address','Address','textarea',{max:400})],
    courses:[field('courseCode','Course code','text',{max:30}),field('courseName','Course name','text',{max:100}),field('credits','Credits','number',{min:1,max:10}),field('semester','Semester','number',{min:1,max:12}),field('status','Status','select',{options:['ACTIVE','INACTIVE']}),field('description','Description','textarea',{max:500})],
    enrollments:refs,
    attendance:[...refs,field('date','Class date','date'),field('status','Attendance status','select',{options:['PRESENT','ABSENT','LATE']})],
    results:[...refs,field('examName','Exam name','text',{max:80,placeholder:'Midterm'}),field('examDate','Exam date','date'),field('maximumMarks','Maximum marks','number',{min:1,max:1000,step:'any'}),field('obtainedMarks','Obtained marks','number',{min:0,max:1000,step:'any'})]
  }[current];
}
function openEditor(record=null) {
  editing=record;$('#editor-title').textContent=(record?'Edit ':'Add ')+singular[current].toLowerCase();$('#editor-error').hidden=true;
  $('#editor-fields').innerHTML=definitions().map(f=>{const value=record?.[f.key]??(f.key==='maximumMarks'?100:f.key==='semester'?1:'');
    const input=f.type==='select'?`<select name="${f.key}" required>${option('','Choose '+f.label.toLowerCase(),value)}${f.options.map(x=>option(Array.isArray(x)?x[0]:x,Array.isArray(x)?x[1]:x,value)).join('')}</select>`:f.type==='textarea'?`<textarea name="${f.key}" rows="3" required maxlength="${f.max}">${escapeHtml(value)}</textarea>`:`<input name="${f.key}" type="${f.type}" value="${escapeHtml(value)}" required ${f.min!==undefined?`min="${f.min}"`:''} ${f.max?`${f.type==='number'?'max':'maxlength'}="${f.max}"`:''} ${f.step?`step="${f.step}"`:''} placeholder="${f.placeholder||''}">`;
    return `<label class="${f.type==='textarea'?'full-width':''}">${f.label}<span class="required"> *</span>${input}</label>`;}).join('');
  $('#editor').showModal();
}
$('#editor-form').onsubmit=async e=>{e.preventDefault();const b=$('#save-button');b.disabled=true;$('#editor-error').hidden=true;
  try {await api(current+(editing?'/'+editing.id:''),{method:editing?'PUT':'POST',body:JSON.stringify(Object.fromEntries(new FormData(e.target)))});$('#editor').close();toast(singular[current]+(editing?' updated successfully.':' added successfully.'));await render();}
  catch(error){$('#editor-error').textContent=error.message;$('#editor-error').hidden=false;}finally{b.disabled=false;}
};
document.querySelectorAll('.close-dialog').forEach(b=>b.onclick=()=>$('#editor').close());
function confirmDelete(id) {const dialog=$('#confirm-dialog');dialog.showModal();$('#cancel-delete').onclick=()=>dialog.close();$('#confirm-delete').onclick=async()=>{const b=$('#confirm-delete');b.disabled=true;try{await api(current+'/'+id,{method:'DELETE'});dialog.close();toast(singular[current]+' deleted successfully.');if(listData.length===1 && page>1)page--;await render();}catch(e){dialog.close();toast(e.message,true);}finally{b.disabled=false;}};}
async function studentProfile(id,token) {
  const d=await api('profile?studentId='+encodeURIComponent(id));if(token!==generation)return;const s=d.student,a=d.attendance;
  setContent(`<a href="#students" class="back-link">← Back to students</a>`+pageHeading('Student profile','A complete picture of academic progress.',`<a href="#enrollments?studentId=${s.id}" class="button secondary">Manage enrollments →</a>`)+
  `<section class="panel profile-header"><span class="avatar large">${initials(s.firstName,s.lastName)}</span><div><h2>${escapeHtml(s.firstName+' '+s.lastName)}</h2><p>${escapeHtml(s.enrollmentNumber)} <span>·</span> ${escapeHtml(s.program)} <span>·</span> Semester ${s.semester}</p></div>${badge(s.status)}</section>`+
  `<section class="profile-grid"><article class="panel"><div class="panel-heading"><h2>Personal & academic information</h2></div><dl class="details-grid">${[['Gender',s.gender.replaceAll('_',' ')],['Date of birth',date(s.dateOfBirth)],['Email',s.email],['Phone',s.phone],['Address',s.address],['Program',s.program],['Semester',s.semester],['Admission date',date(s.admissionDate)]].map(([l,v])=>`<div><dt>${l}</dt><dd>${escapeHtml(v)}</dd></div>`).join('')}</dl></article><article class="panel attendance-summary"><span class="eyebrow">ATTENDANCE SUMMARY</span><strong>${a.percentage}%</strong>${badge(a.label)}<div class="attendance-counts"><div><b>${a.held}</b><span>Classes held</span></div><div><b>${a.attended}</b><span>Attended</span></div><div><b>${a.absent}</b><span>Absences</span></div></div><p class="muted">Late counts as attended.</p></article></section>`+
  `<section class="panel"><div class="panel-heading"><h2>Enrolled courses</h2><span class="count-pill">${d.courses.length} courses</span></div><div class="table-scroll"><table><thead><tr><th>Course</th><th>Credits</th><th>Classes held</th><th>Attended</th><th>Absences</th><th>Attendance</th></tr></thead><tbody>${d.courses.map(c=>`<tr><td><strong>${escapeHtml(c.courseName)}</strong><small>${escapeHtml(c.courseCode)}</small></td><td>${c.credits}</td><td>${c.attendance.held}</td><td>${c.attendance.attended}</td><td>${c.attendance.absent}</td><td>${c.attendance.percentage}%</td></tr>`).join('')}</tbody></table></div>${d.courses.length?'':empty('No enrollments yet','Enroll this student in a course.')}</section>`+
  `<section class="panel profile-results"><div class="panel-heading"><h2>Academic performance</h2><span class="count-pill">${d.results.length} results</span></div><div class="table-scroll"><table><thead><tr><th>Course</th><th>Exam</th><th>Date</th><th>Maximum marks</th><th>Obtained</th><th>Percentage</th><th>Grade</th></tr></thead><tbody>${d.results.map(r=>`<tr><td>${escapeHtml(r.courseName)}</td><td>${escapeHtml(r.examName)}</td><td>${date(r.examDate)}</td><td>${r.maximumMarks}</td><td>${r.obtainedMarks}</td><td>${r.percentage}%</td><td>${badge(r.grade)}</td></tr>`).join('')}</tbody></table></div>${d.results.length?'':empty('No results yet','Add an assessment result for this student.')}</section>`);
}
async function reports(token) { const [d,analytics]=await Promise.all([api('reports'),api('dashboard')]);if(token!==generation)return;
  setContent(pageHeading('Academic reports','Attendance and assessment summaries, calculated from your records.','<a class="button primary" href="/api/reports.csv">↓ Export CSV</a>')+
    `<section class="dashboard-grid"><article class="panel"><div class="panel-heading"><h2>Students by semester</h2></div><div class="chart-bars">${bars(analytics.semesters)}</div></article><article class="panel"><div class="panel-heading"><h2>Admissions by month</h2></div><div class="chart-bars">${bars(analytics.admissions,'blue')}</div></article></section>`+
    `<section class="panel"><div class="panel-heading"><div><h2>Student performance summary</h2><p>Average marks is the mean of each student's exam percentages.</p></div></div><div class="table-scroll"><table><thead><tr><th>Student</th><th>Program</th><th>Classes held</th><th>Attendance</th><th>Assessment average</th><th>Exams</th></tr></thead><tbody>${d.students.map(s=>`<tr><td><a class="text-link" href="#student/${s.id}">${escapeHtml(s.name)}</a><small>${escapeHtml(s.enrollmentNumber)}</small></td><td>${escapeHtml(s.program)}</td><td>${s.attendance.held}</td><td><strong>${s.attendance.percentage}%</strong>${badge(s.attendance.label)}</td><td>${s.averageMarks===null?'No results':s.averageMarks+'%'}</td><td>${s.exams}</td></tr>`).join('')}</tbody></table></div>${d.students.length?'':empty()}</section>`);
}
function adminProfile() {setContent(pageHeading('Your profile','Administrator account and workspace information.')+`<section class="panel admin-card"><span class="avatar large">${initials(user.name.split(' ')[0],user.name.split(' ')[1])}</span><h2>${escapeHtml(user.name)}</h2><p>${escapeHtml(user.email)}</p>${badge('Administrator')}<dl class="details-grid"><div><dt>Access level</dt><dd>Full academic administration</dd></div><div><dt>Session timeout</dt><dd>30 minutes of inactivity</dd></div><div><dt>Workspace</dt><dd>Local demonstration</dd></div><div><dt>Account management</dt><dd>Read-only demo profile</dd></div></dl><p class="help-note">The demo account is intended for local learning. Account editing and password resets are not part of this version.</p></section>`);}
async function render() {
  const token=++generation;setContent('<div class="loading"><span class="spinner"></span> Loading your workspace…</div>');
  try {const hash=location.hash.slice(1)||'dashboard';if(hash.startsWith('student/'))await studentProfile(hash.slice(8),token);else if(current==='dashboard')await dashboard(token);else if(current==='reports')await reports(token);else if(current==='profile')adminProfile();else await listing(token);}
  catch(e){if(token!==generation)return;setContent(`<div class="panel error-panel"><h2>We couldn’t load this view.</h2><p>${escapeHtml(e.message)}</p><button id="retry" class="button primary">Try again</button></div>`);$('#retry').onclick=render;}
}
function route() {
  const hash=location.hash.slice(1)||'dashboard';const [routeName,query]=hash.split('?');current=routeName.startsWith('student/')?'students':(names[routeName]?routeName:'dashboard');
  Object.keys(filters).forEach(k=>delete filters[k]);new URLSearchParams(query).forEach((v,k)=>filters[k]=v);page=1;
  $('#page-crumb').textContent=names[current];document.title=names[current]+' · Student Management System';
  document.querySelectorAll('#navigation a').forEach(a=>{a.classList.toggle('active',a.dataset.page===current);if(a.dataset.page===current)a.setAttribute('aria-current','page');else a.removeAttribute('aria-current');});
  document.body.classList.remove('sidebar-open');$('#menu-button').setAttribute('aria-expanded','false');render();
}
$('#navigation').innerHTML=Object.entries(names).map(([key,label])=>`<a href="#${key}" data-page="${key}">${icon(key)}<span>${label}</span></a>`).join('');
$('#today').textContent=new Date().toLocaleDateString('en-IN',{day:'numeric',month:'short',year:'numeric'});
$('#menu-button').onclick=()=>{const open=document.body.classList.toggle('sidebar-open');$('#menu-button').setAttribute('aria-expanded',String(open));};
$('#sidebar-scrim').onclick=()=>{$('body').classList.remove('sidebar-open');$('#menu-button').setAttribute('aria-expanded','false');};
$('#logout').onclick=async()=>{try{await api('auth/logout',{method:'POST'});location.assign('/login.html');}catch(e){toast(e.message,true);}};
window.addEventListener('hashchange',route);
(async()=>{try{const data=await api('auth/me');csrf=data.csrf;user=data.user;$('#user-name').textContent=user.name;$('#user-initials').textContent=user.name.split(' ').map(n=>n[0]).slice(0,2).join('');route();}catch(e){toast(e.message,true);}})();
