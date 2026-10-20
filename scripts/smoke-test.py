"""Optional HTTP integration checks against a seeded, running local app. Python 3 standard library only.
Run: python scripts/smoke-test.py http://localhost:8080
Creates uniquely named records, tests CRUD/security, then removes its records.
"""
import urllib.request, urllib.error, http.cookiejar, json, sys, uuid
base = sys.argv[1] if len(sys.argv)>1 else 'http://localhost:8080'
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
csrf='';checks=[]
def request(path, method='GET', data=None, status=200, headers=None):
    h={'Content-Type':'application/json','X-CSRF-Token':csrf};h.update(headers or {})
    req=urllib.request.Request(base+path,data=json.dumps(data).encode() if data is not None else None,headers=h,method=method)
    try: response=opener.open(req)
    except urllib.error.HTTPError as e: response=e
    body=response.read().decode();assert response.code==status,(method,path,response.code,body)
    checks.append(method+' '+path+' -> '+str(status))
    return json.loads(body) if 'application/json' in response.headers.get('Content-Type','') else body

request('/api/students',status=401)
assert 'Welcome back.' in request('/app.html')
request('/api/auth/login','POST',{'email':'admin@studentmanagement.local','password':'incorrect'},401)
request('/api/auth/login','POST',{'email':'admin@studentmanagement.local','password':'Admin@123'},403,{'Origin':'https://example.invalid'})
auth=request('/api/auth/login','POST',{'email':'admin@studentmanagement.local','password':'Admin@123'})
csrf=auth['csrf'];assert request('/api/auth/me')['user']['role']=='Administrator'
before=request('/api/dashboard')['stats']
request('/api/students','POST',{},403,{'X-CSRF-Token':'wrong'})
request('/api/students','POST',{},400)
suffix=uuid.uuid4().hex[:8].upper()
student={'enrollmentNumber':'QA-'+suffix,'firstName':'Verification','lastName':'Student','gender':'OTHER','dateOfBirth':'2005-01-01','email':'qa@example.com','phone':'202-555-0100','program':'QA Program','semester':2,'address':'Fictional testing address','admissionDate':'2025-08-01','status':'ACTIVE'}
s=request('/api/students','POST',student,201);sid=s['id']
request('/api/students','POST',student,409)
assert request('/api/students/'+sid)['firstName']=='Verification'
assert request('/api/students?q='+suffix)['total']==1
assert request('/api/students?program=QA%20Program&semester=2&status=ACTIVE')['total']>=1
assert request('/api/students?page=1&size=10')['items'] != request('/api/students?page=2&size=10')['items']
assert request('/api/students?q=%2E%2A')['total']==0
assert request('/api/students?sort=firstName&direction=asc')['items']
request('/api/students?page=0',status=400)
student['firstName']='Updated';request('/api/students/'+sid,'PUT',student)
bad={**student,'semester':99};request('/api/students/'+sid,'PUT',bad,400)
bad={**student,'email':'bad'};request('/api/students/'+sid,'PUT',bad,400)
bad={**student,'phone':'abc'};request('/api/students/'+sid,'PUT',bad,400)
bad={**student,'dateOfBirth':'2005-02-30'};request('/api/students/'+sid,'PUT',bad,400)
course={'courseCode':'QA-'+suffix,'courseName':'Verification Course','description':'Temporary integration check','credits':3,'semester':2,'status':'ACTIVE'}
c=request('/api/courses','POST',course,201);cid=c['id'];request('/api/courses','POST',course,409)
assert request('/api/courses?q='+suffix)['total']==1
assert request('/api/courses?semester=2&status=ACTIVE')['total']>=1
course['courseName']='Updated course';request('/api/courses/'+cid,'PUT',course)
pair={'studentId':sid,'courseId':cid}
a={**pair,'date':'2026-09-01','status':'PRESENT'}
request('/api/attendance','POST',a,400)
en=request('/api/enrollments','POST',pair,201);eid=en['id'];request('/api/enrollments','POST',pair,409)
assert request('/api/enrollments?courseId='+cid)['total']==1
request('/api/students/'+sid,'DELETE',status=409);request('/api/courses/'+cid,'DELETE',status=409)
att=request('/api/attendance','POST',a,201);aid=att['id'];request('/api/attendance','POST',a,409)
a['status']='LATE';request('/api/attendance/'+aid,'PUT',a)
absent=request('/api/attendance','POST',{**pair,'date':'2026-09-02','status':'ABSENT'},201)
assert request('/api/attendance?studentId='+sid+'&date=2026-09-01')['total']==1
profile=request('/api/profile?studentId='+sid);assert profile['attendance']['percentage']==50
assert profile['attendance']['held']==2 and profile['attendance']['absent']==1
r={**pair,'examName':'Integration exam','examDate':'2026-09-03','maximumMarks':50,'obtainedMarks':45,'percentage':0,'grade':'F'}
result=request('/api/results','POST',r,201);rid=result['id'];assert result['percentage']==90 and result['grade']=='A+'
request('/api/results','POST',r,409)
request('/api/results/'+rid,'PUT',{**r,'obtainedMarks':51},400)
r['obtainedMarks']=30;updated=request('/api/results/'+rid,'PUT',r);assert updated['percentage']==60 and updated['grade']=='C'
assert request('/api/results?q=Integration&studentId='+sid+'&grade=C')['total']==1
request('/api/enrollments/'+eid,'DELETE',status=409)
assert request('/api/dashboard')['stats']['totalStudents']==before['totalStudents']+1
assert any(x['id']==sid for x in request('/api/reports')['students'])
assert 'Updated Student' in request('/api/reports.csv')
assert 'wrong turn' in request('/not-a-page',status=404)
assert 'Access is restricted' in request('/errors/403.html')
assert 'attention' in request('/errors/500.html')
request('/api/not-a-route',status=404)
request('/api/students/not-an-id',status=404)
request('/api/results/'+rid,'DELETE');request('/api/attendance/'+aid,'DELETE');request('/api/attendance/'+absent['id'],'DELETE')
request('/api/enrollments/'+eid,'DELETE');request('/api/courses/'+cid,'DELETE');request('/api/students/'+sid,'DELETE')
request('/api/students/'+sid,status=404)
assert request('/api/dashboard')['stats']==before
request('/api/auth/logout','POST');request('/api/auth/me',status=401)
print('\n'.join(checks));print(f'PASS: {len(checks)} HTTP checks plus response assertions. Temporary records removed.')

