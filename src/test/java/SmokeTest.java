import com.crimemanagement.dao.FIRDAO;
import com.crimemanagement.dao.OfficerDAO;
import com.crimemanagement.dao.CaseDAO;
import com.crimemanagement.entity.FIR;
import com.crimemanagement.entity.Officer;
import com.crimemanagement.entity.Case;

public class SmokeTest {
    public static void main(String[] args) throws Exception {
        OfficerDAO od = new OfficerDAO();
        Officer o = new Officer();
        o.setBadgeNumber("OFF999");
        o.setFirstName("Test");
        o.setLastName("Officer");
        o.setRank("Detective");
        o.setStationId(1);
        od.create(o);

        FIRDAO fd = new FIRDAO();
        FIR f = new FIR();
        f.setFirNumber("FIR-TEST-001");
        f.setCrimeType("Test");
        f.setCrimeDescription("Desc");
        f.setLocation("Loc");
        f.setDateOfIncident(java.time.LocalDateTime.now());
        f.setDateReported(java.time.LocalDateTime.now());
        f.setCitizenId(1);
        f.setStatus("Open");
        f.setAssignedOfficerId(o.getOfficerId());
        fd.addFIR(f);

        CaseDAO cd = new CaseDAO();
        Case c = new Case();
        c.setCaseNumber("CASE-TEST-001");
        c.setFirId(f.getFirId());
        c.setCaseTitle("Title");
        c.setPriority("Low");
        c.setStatus("Open");
        cd.create(c);

        System.out.println("Smoke test complete: created officer="+o.getOfficerId()+" fir="+f.getFirId()+" case="+c.getCaseId());
    }
}
