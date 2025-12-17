package hpms.util;

import hpms.model.*;
import hpms.auth.User;
import java.time.LocalDateTime;
import java.util.*;

public class BackupUtil {
    public static java.nio.file.Path DEFAULT_PATH() {
        return java.nio.file.Paths.get(System.getProperty("user.home"), "hpms_backup.json");
    }

    public static String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"patients\":[");
        int i = 0;
        for (Patient p : DataStore.patients.values()) {
            if (i++ > 0)
                sb.append(',');
            sb.append('{')
                    .append("\"id\":\"").append(p.id).append("\",")
                    .append("\"name\":\"").append(p.name == null ? "" : p.name.replace("\"", "\\\"")).append("\",")
                    .append("\"age\":").append(p.age).append(',')
                    .append("\"gender\":\"").append(p.gender == null ? "" : p.gender.name()).append("\",")
                    .append("\"contact\":\"").append(p.contact == null ? "" : p.contact.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"address\":\"").append(p.address == null ? "" : p.address.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"heightCm\":")
                    .append(p.heightCm == null ? "null" : String.format(Locale.US, "%.2f", p.heightCm)).append(',')
                    .append("\"weightKg\":")
                    .append(p.weightKg == null ? "null" : String.format(Locale.US, "%.2f", p.weightKg)).append(',')
                    .append("\"bloodPressure\":\"")
                    .append(p.bloodPressure == null ? "" : p.bloodPressure.replace("\"", "\\\"")).append("\",")
                    .append("\"registrationType\":\"")
                    .append(p.registrationType == null ? "" : p.registrationType.replace("\"", "\\\"")).append("\",")
                    .append("\"incidentTime\":\"")
                    .append(p.incidentTime == null ? "" : p.incidentTime.replace("\"", "\\\"")).append("\",")
                    .append("\"broughtBy\":\"").append(p.broughtBy == null ? "" : p.broughtBy.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"initialBp\":\"").append(p.initialBp == null ? "" : p.initialBp.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"initialHr\":\"").append(p.initialHr == null ? "" : p.initialHr.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"initialSpo2\":\"")
                    .append(p.initialSpo2 == null ? "" : p.initialSpo2.replace("\"", "\\\"")).append("\",")
                    .append("\"chiefComplaint\":\"")
                    .append(p.chiefComplaint == null ? "" : p.chiefComplaint.replace("\"", "\\\"")).append("\",")
                    .append("\"allergies\":\"").append(p.allergies == null ? "" : p.allergies.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"medications\":\"")
                    .append(p.medications == null ? "" : p.medications.replace("\"", "\\\"")).append("\",")
                    .append("\"pastMedicalHistory\":\"")
                    .append(p.pastMedicalHistory == null ? "" : p.pastMedicalHistory.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"surgicalHistory\":\"")
                    .append(p.surgicalHistory == null ? "" : p.surgicalHistory.replace("\"", "\\\"")).append("\",")
                    .append("\"familyHistory\":\"")
                    .append(p.familyHistory == null ? "" : p.familyHistory.replace("\"", "\\\"")).append("\",")
                    .append("\"smokingStatus\":\"")
                    .append(p.smokingStatus == null ? "" : p.smokingStatus.replace("\"", "\\\"")).append("\",")
                    .append("\"alcoholUse\":\"").append(p.alcoholUse == null ? "" : p.alcoholUse.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"drugUse\":\"").append(p.drugUse == null ? "" : p.drugUse.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"occupation\":\"").append(p.occupation == null ? "" : p.occupation.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"attachmentPaths\":\"")
                    .append(p.attachmentPaths == null ? "" : String.join("|", p.attachmentPaths)).append("\",")
                    .append("\"xrayFilePath\":\"")
                    .append(p.xrayFilePath == null ? "" : p.xrayFilePath.replace("\"", "\\\"")).append("\",")
                    .append("\"xrayStatus\":\"").append(p.xrayStatus == null ? "" : p.xrayStatus.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"xraySummary\":\"")
                    .append(p.xraySummary == null ? "" : p.xraySummary.replace("\"", "\\\"")).append("\",")
                    .append("\"stoolFilePath\":\"")
                    .append(p.stoolFilePath == null ? "" : p.stoolFilePath.replace("\"", "\\\"")).append("\",")
                    .append("\"stoolStatus\":\"")
                    .append(p.stoolStatus == null ? "" : p.stoolStatus.replace("\"", "\\\"")).append("\",")
                    .append("\"stoolSummary\":\"")
                    .append(p.stoolSummary == null ? "" : p.stoolSummary.replace("\"", "\\\"")).append("\",")
                    .append("\"urineFilePath\":\"")
                    .append(p.urineFilePath == null ? "" : p.urineFilePath.replace("\"", "\\\"")).append("\",")
                    .append("\"urineStatus\":\"")
                    .append(p.urineStatus == null ? "" : p.urineStatus.replace("\"", "\\\"")).append("\",")
                    .append("\"urineSummary\":\"")
                    .append(p.urineSummary == null ? "" : p.urineSummary.replace("\"", "\\\"")).append("\",")
                    .append("\"bloodFilePath\":\"")
                    .append(p.bloodFilePath == null ? "" : p.bloodFilePath.replace("\"", "\\\"")).append("\",")
                    .append("\"bloodStatus\":\"")
                    .append(p.bloodStatus == null ? "" : p.bloodStatus.replace("\"", "\\\"")).append("\",")
                    .append("\"bloodSummary\":\"")
                    .append(p.bloodSummary == null ? "" : p.bloodSummary.replace("\"", "\\\"")).append("\",")
                    .append("\"diagnoses\":\"").append(p.diagnoses == null ? "" : String.join("|", p.diagnoses))
                    .append("\",")
                    .append("\"treatmentPlans\":\"")
                    .append(p.treatmentPlans == null ? "" : String.join("|", p.treatmentPlans)).append("\",")
                    .append("\"dischargeSummaries\":\"")
                    .append(p.dischargeSummaries == null ? "" : String.join("|", p.dischargeSummaries)).append("\",")
                    .append("\"insuranceProvider\":\"")
                    .append(p.insuranceProvider == null ? "" : p.insuranceProvider.replace("\"", "\\\"")).append("\",")
                    .append("\"insuranceId\":\"")
                    .append(p.insuranceId == null ? "" : p.insuranceId.replace("\"", "\\\"")).append("\",")
                    .append("\"insuranceGroup\":\"")
                    .append(p.insuranceGroup == null ? "" : p.insuranceGroup.replace("\"", "\\\"")).append("\",")
                    .append("\"policyHolderName\":\"")
                    .append(p.policyHolderName == null ? "" : p.policyHolderName.replace("\"", "\\\"")).append("\",")
                    .append("\"policyHolderDob\":\"")
                    .append(p.policyHolderDob == null ? "" : p.policyHolderDob.replace("\"", "\\\"")).append("\",")
                    .append("\"policyRelationship\":\"")
                    .append(p.policyRelationship == null ? "" : p.policyRelationship.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"secondaryInsurance\":\"")
                    .append(p.secondaryInsurance == null ? "" : p.secondaryInsurance.replace("\"", "\\\"")).append("\"")
                    .append('}');
        }
        sb.append(']');
        sb.append(',').append("\"staff\":[");
        i = 0;
        for (Staff s : DataStore.staff.values()) {
            if (i++ > 0)
                sb.append(',');
            sb.append('{')
                    .append("\"id\":\"").append(s.id).append("\",")
                    .append("\"name\":\"").append(s.name == null ? "" : s.name.replace("\"", "\\\"")).append("\",")
                    .append("\"role\":\"").append(s.role == null ? "" : s.role.name()).append("\",")
                    .append("\"department\":\"").append(s.department == null ? "" : s.department.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"phone\":\"").append(s.phone == null ? "" : s.phone.replace("\"", "\\\"")).append("\",")
                    .append("\"email\":\"").append(s.email == null ? "" : s.email.replace("\"", "\\\"")).append("\",")
                    .append("\"licenseNumber\":\"")
                    .append(s.licenseNumber == null ? "" : s.licenseNumber.replace("\"", "\\\"")).append("\",")
                    .append("\"specialty\":\"").append(s.specialty == null ? "" : s.specialty.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"subSpecialization\":\"")
                    .append(s.subSpecialization == null ? "" : s.subSpecialization.replace("\"", "\\\"")).append("\",")
                    .append("\"nursingField\":\"")
                    .append(s.nursingField == null ? "" : s.nursingField.replace("\"", "\\\"")).append("\",")
                    .append("\"yearsExperience\":").append(s.yearsExperience == null ? "null" : s.yearsExperience)
                    .append(',')
                    .append("\"yearsPractice\":").append(s.yearsPractice == null ? "null" : s.yearsPractice).append(',')
                    .append("\"yearsOfWork\":").append(s.yearsOfWork == null ? "null" : s.yearsOfWork).append(',')
                    .append("\"clinicScheduleStr\":\"")
                    .append(s.clinicSchedule_str == null ? "" : s.clinicSchedule_str.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"qualifications\":\"")
                    .append(s.qualifications == null ? "" : s.qualifications.replace("\"", "\\\"")).append("\",")
                    .append("\"certifications\":\"")
                    .append(s.certifications == null ? "" : s.certifications.replace("\"", "\\\"")).append("\",")
                    .append("\"bio\":\"").append(s.bio == null ? "" : s.bio.replace("\"", "\\\"")).append("\",")
                    .append("\"employeeId\":\"").append(s.employeeId == null ? "" : s.employeeId.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"status\":\"").append(s.status == null ? "" : s.status.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"photoPath\":\"").append(s.photoPath == null ? "" : s.photoPath.replace("\"", "\\\""))
                    .append("\",")
                    .append("\"isAvailable\":").append(s.isAvailable).append('}');
        }
        sb.append(']');
        sb.append(',').append("\"rooms\":[");
        i = 0;
        for (Room r : DataStore.rooms.values()) {
            if (i++ > 0)
                sb.append(',');
            sb.append('{')
                    .append("\"id\":\"").append(r.id).append("\",")
                    .append("\"status\":\"").append(r.status.name()).append("\",")
                    .append("\"occupant\":\"").append(r.occupantPatientId == null ? "" : r.occupantPatientId)
                    .append("\"}");
        }
        sb.append(']');
        sb.append(',').append("\"appointments\":[");
        i = 0;
        for (Appointment a : DataStore.appointments.values()) {
            if (i++ > 0)
                sb.append(',');
            sb.append('{')
                    .append("\"id\":\"").append(a.id).append("\",")
                    .append("\"patient\":\"").append(a.patientId).append("\",")
                    .append("\"staff\":\"").append(a.staffId).append("\",")
                    .append("\"datetime\":\"").append(a.dateTime.toString()).append("\",")
                    .append("\"department\":\"").append(a.department).append("\"}");
        }
        sb.append(']');
        sb.append(',').append("\"bills\":[");
        i = 0;
        for (Bill b : DataStore.bills.values()) {
            if (i++ > 0)
                sb.append(',');
            sb.append('{')
                    .append("\"id\":\"").append(b.id).append("\",")
                    .append("\"patient\":\"").append(b.patientId).append("\",")
                    .append("\"total\":").append(String.format(Locale.US, "%.2f", b.total)).append(',')
                    .append("\"paid\":").append(b.paid).append(',')
                    .append("\"method\":\"").append(b.paymentMethod == null ? "" : b.paymentMethod.name())
                    .append("\"}");
        }
        sb.append(']');
        // Medicine functionality removed
        sb.append(',');
        sb.append("\"users\":[");
        int u = 0;
        for (User us : DataStore.users.values()) {
            if (u++ > 0)
                sb.append(',');
            sb.append('{')
                    .append("\"username\":\"").append(us.username).append("\",")
                    .append("\"password\":\"").append(us.password == null ? "" : us.password).append("\",")
                    .append("\"salt\":\"").append(us.salt == null ? "" : us.salt).append("\",")
                    .append("\"displayPassword\":\"")
                    .append(us.displayPassword == null ? "" : us.displayPassword.replace("\"", "\\\"")).append("\",")
                    .append("\"role\":\"").append(us.role == null ? "" : us.role.name()).append("\"");
            sb.append('}');
        }
        sb.append(']');
        sb.append('}');
        return sb.toString();
    }

    public static void fromJson(String json) {
        DataStore.patients.clear();
        DataStore.staff.clear();
        DataStore.rooms.clear();
        DataStore.appointments.clear();
        DataStore.bills.clear();
        // Medicine functionality removed
        DataStore.users.clear();
        String sec = json;
        String patients = between(sec, "\"patients\":[", "]");
        if (patients != null && !patients.trim().isEmpty())
            for (String o : splitObjs(patients)) {
                Map<String, String> m = parseObj(o);
                String id = m.get("id");
                String name = m.get("name");
                int age = Integer.parseInt(m.get("age"));
                String genderStr = m.get("gender");
                Gender g;
                try {
                    // Handle new gender values and map them to enum
                    if ("MALE".equals(genderStr.toUpperCase(java.util.Locale.ROOT)))
                        g = Gender.Male;
                    else if ("FEMALE".equals(genderStr.toUpperCase(java.util.Locale.ROOT)))
                        g = Gender.Female;
                    else if ("LGBTQ+".equals(genderStr))
                        g = Gender.LGBTQ_PLUS;
                    else
                        g = Gender.valueOf(genderStr);
                } catch (Exception e) {
                    g = Gender.OTHER;
                }
                String contact = m.get("contact");
                String address = m.get("address");
                Patient patient = new Patient(id, name, age, "", g, contact, address, LocalDateTime.now());
                try {
                    String hStr = m.get("heightCm");
                    if (hStr != null && !hStr.trim().isEmpty() && !"null".equals(hStr))
                        patient.heightCm = Double.parseDouble(hStr);
                } catch (Exception ex) {
                }
                try {
                    String wStr = m.get("weightKg");
                    if (wStr != null && !wStr.trim().isEmpty() && !"null".equals(wStr))
                        patient.weightKg = Double.parseDouble(wStr);
                } catch (Exception ex) {
                }
                patient.bloodPressure = n(m.get("bloodPressure"));
                patient.registrationType = n(m.get("registrationType"));
                patient.incidentTime = n(m.get("incidentTime"));
                patient.broughtBy = n(m.get("broughtBy"));
                patient.initialBp = n(m.get("initialBp"));
                patient.initialHr = n(m.get("initialHr"));
                patient.initialSpo2 = n(m.get("initialSpo2"));
                patient.chiefComplaint = n(m.get("chiefComplaint"));
                patient.allergies = n(m.get("allergies"));
                patient.medications = n(m.get("medications"));
                patient.pastMedicalHistory = n(m.get("pastMedicalHistory"));
                patient.surgicalHistory = n(m.get("surgicalHistory"));
                patient.familyHistory = n(m.get("familyHistory"));
                patient.smokingStatus = n(m.get("smokingStatus"));
                patient.alcoholUse = n(m.get("alcoholUse"));
                patient.drugUse = n(m.get("drugUse"));
                patient.occupation = n(m.get("occupation"));
                String attachments = m.get("attachmentPaths");
                if (attachments != null && !attachments.isEmpty())
                    patient.attachmentPaths = new java.util.ArrayList<>(
                            java.util.Arrays.asList(attachments.split("\\|")));
                patient.xrayFilePath = n(m.get("xrayFilePath"));
                patient.xrayStatus = n(m.get("xrayStatus"));
                patient.xraySummary = n(m.get("xraySummary"));
                patient.stoolFilePath = n(m.get("stoolFilePath"));
                patient.stoolStatus = n(m.get("stoolStatus"));
                patient.stoolSummary = n(m.get("stoolSummary"));
                patient.urineFilePath = n(m.get("urineFilePath"));
                patient.urineStatus = n(m.get("urineStatus"));
                patient.urineSummary = n(m.get("urineSummary"));
                patient.bloodFilePath = n(m.get("bloodFilePath"));
                patient.bloodStatus = n(m.get("bloodStatus"));
                patient.bloodSummary = n(m.get("bloodSummary"));
                String di = m.get("diagnoses");
                if (di != null && !di.isEmpty())
                    patient.diagnoses = new java.util.ArrayList<>(java.util.Arrays.asList(di.split("\\|")));
                String tp = m.get("treatmentPlans");
                if (tp != null && !tp.isEmpty())
                    patient.treatmentPlans = new java.util.ArrayList<>(java.util.Arrays.asList(tp.split("\\|")));
                String ds = m.get("dischargeSummaries");
                if (ds != null && !ds.isEmpty())
                    patient.dischargeSummaries = new java.util.ArrayList<>(java.util.Arrays.asList(ds.split("\\|")));
                patient.insuranceProvider = n(m.get("insuranceProvider"));
                patient.insuranceId = n(m.get("insuranceId"));
                patient.insuranceGroup = n(m.get("insuranceGroup"));
                patient.policyHolderName = n(m.get("policyHolderName"));
                patient.policyHolderDob = n(m.get("policyHolderDob"));
                patient.policyRelationship = n(m.get("policyRelationship"));
                patient.secondaryInsurance = n(m.get("secondaryInsurance"));
                DataStore.patients.put(id, patient);
            }
        String staff = between(sec, "\"staff\":[", "]");
        if (staff != null && !staff.trim().isEmpty())
            for (String o : splitObjs(staff)) {
                Map<String, String> m = parseObj(o);
                String id = m.get("id");
                String name = m.get("name");
                StaffRole role = StaffRole.valueOf(m.get("role"));
                String dept = m.get("department");
                Staff s = new Staff(id, name, role, dept, LocalDateTime.now());
                s.phone = n(m.get("phone"));
                s.email = n(m.get("email"));
                s.licenseNumber = n(m.get("licenseNumber"));
                s.specialty = n(m.get("specialty"));
                s.subSpecialization = n(m.get("subSpecialization"));
                s.nursingField = n(m.get("nursingField"));
                try {
                    String ye = m.get("yearsExperience");
                    if (ye != null && !ye.equals("null"))
                        s.yearsExperience = Integer.parseInt(ye);
                } catch (Exception ex) {
                }
                try {
                    String yp = m.get("yearsPractice");
                    if (yp != null && !yp.equals("null"))
                        s.yearsPractice = Integer.parseInt(yp);
                } catch (Exception ex) {
                }
                try {
                    String yw = m.get("yearsOfWork");
                    if (yw != null && !yw.equals("null"))
                        s.yearsOfWork = Integer.parseInt(yw);
                } catch (Exception ex) {
                }
                s.clinicSchedule_str = n(m.get("clinicScheduleStr"));
                s.qualifications = n(m.get("qualifications"));
                s.certifications = n(m.get("certifications"));
                s.bio = n(m.get("bio"));
                s.employeeId = n(m.get("employeeId"));
                s.status = n(m.get("status"));
                s.photoPath = n(m.get("photoPath"));
                try {
                    String av = m.get("isAvailable");
                    if (av != null)
                        s.isAvailable = Boolean.parseBoolean(av);
                } catch (Exception ex) {
                }
                DataStore.staff.put(id, s);
            }
        String rooms = between(sec, "\"rooms\":[", "]");
        if (rooms != null && !rooms.trim().isEmpty())
            for (String o : splitObjs(rooms)) {
                Map<String, String> m = parseObj(o);
                String id = m.get("id");
                RoomStatus st = RoomStatus.valueOf(m.get("status"));
                String occ = m.get("occupant");
                DataStore.rooms.put(id, new Room(id, st, occ == null || occ.isEmpty() ? null : occ));
            }
        String appts = between(sec, "\"appointments\":[", "]");
        if (appts != null && !appts.trim().isEmpty())
            for (String o : splitObjs(appts)) {
                Map<String, String> m = parseObj(o);
                String id = m.get("id");
                String pid = m.get("patient");
                String sid = m.get("staff");
                java.time.LocalDateTime dt = java.time.LocalDateTime.parse(m.get("datetime"));
                String dep = m.get("department");
                DataStore.appointments.put(id, new Appointment(id, pid, sid, dt, dep, LocalDateTime.now()));
            }
        String bills = between(sec, "\"bills\":[", "]");
        if (bills != null && !bills.trim().isEmpty())
            for (String o : splitObjs(bills)) {
                Map<String, String> m = parseObj(o);
                String id = m.get("id");
                String pid = m.get("patient");
                double total = Double.parseDouble(m.get("total"));
                boolean paid = Boolean.parseBoolean(m.get("paid"));
                String method = m.get("method");
                Bill b = new Bill(id, pid, total, LocalDateTime.now());
                b.paid = paid;
                b.paymentMethod = Validators.empty(method) ? null : PaymentMethod.valueOf(method);
                DataStore.bills.put(id, b);
            }
        // Medicine functionality removed
        String users = between(sec, "\"users\":[", "]");
        if (users != null && !users.trim().isEmpty())
            for (String o : splitObjs(users)) {
                Map<String, String> m = parseObj(o);
                String username = m.get("username");
                String password = m.get("password");
                String salt = m.get("salt");
                String role = m.get("role");
                String disp = m.get("displayPassword");
                if (username == null)
                    continue;
                UserRole r = null;
                try {
                    if (role != null && !role.isEmpty())
                        r = UserRole.valueOf(role);
                } catch (Exception ex) {
                    r = UserRole.CASHIER;
                }
                User u = new User(username, password == null ? "" : password, salt == null ? "" : salt,
                        r == null ? UserRole.CASHIER : r);
                u.displayPassword = disp == null ? "" : disp;
                DataStore.users.put(username, u);
            }
        LogManager.log("restore");
    }

    static String between(String s, String start, String endToken) {
        int i = s.indexOf(start);
        if (i < 0)
            return null;
        int j = s.indexOf(endToken, i + start.length());
        if (j < 0)
            return null;
        return s.substring(i + start.length(), j);
    }

    static List<String> splitObjs(String s) {
        List<String> list = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') {
                if (depth++ == 0)
                    start = i;
            } else if (c == '}') {
                if (--depth == 0)
                    list.add(s.substring(start, i + 1));
            }
        }
        return list;
    }

    static java.util.Map<String, String> parseObj(String obj) {
        java.util.Map<String, String> m = new java.util.LinkedHashMap<>();
        String body = obj.substring(1, obj.length() - 1);
        String[] parts = body.split(",");
        for (String part : parts) {
            int c = part.indexOf(':');
            if (c > 0) {
                String k = part.substring(0, c).trim().replace("\"", ""), v = part.substring(c + 1).trim();
                if (v.startsWith("\""))
                    v = v.substring(1, v.endsWith("\"") ? v.length() - 1 : v.length());
                m.put(k, v);
            }
        }
        return m;
    }

    static String n(String s) {
        return s == null ? "" : s;
    }

    public static boolean saveToFile(java.nio.file.Path path) {
        try {
            java.nio.file.Files.write(path, toJson().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            LogManager.log("backup_save " + path.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean loadFromFile(java.nio.file.Path path) {
        try {
            String txt = new String(java.nio.file.Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);
            fromJson(txt);
            LogManager.log("backup_load " + path.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean saveToDefault() {
        // Disabled backup saving to use database instead
        // try {
        //     return saveToFile(DEFAULT_PATH());
        // } catch (Exception e) {
        //     return false;
        // }
        return true; // Pretend saving succeeded
    }
}
