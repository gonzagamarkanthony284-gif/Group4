package hpms.test;

import hpms.service.PatientService;
import hpms.auth.AuthService;

public class PatientPasswordTest {
    public static void main(String[] args) throws Exception {
        // create patient via service so it registers in DataStore
        java.util.List<String> out = PatientService.add("Test Patient", "30", "1993-07-25", "Male", "555-1111",
                "Some Address", "INPATIENT");
        System.out.println(out);
        String created = out.get(0);
        String id = created.split(" ")[2];
        // generate password and create patient portal account
        String pwd = AuthService.generateRandomPasswordForUI();
        System.out.println("Registering patient user " + id + " with password " + pwd);
        System.out.println(AuthService.createPatientAccount(id, pwd));
        // verify
        boolean ok = AuthService.verifyCredentials(id, pwd);
        if (!ok)
            throw new RuntimeException("Verify failed");
        System.out.println("Verified OK");
    }
}
