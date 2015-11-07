package kw.nfc.communication;
import org.json.simple.JSONObject;


public class Guest {
	
	private int gid;
	private String name;
	private int teamId;
	private int cloakId;
	
	public Guest(int gid, String name) {
		this.setGid(gid);
		this.setName(name);
	}

	public Guest(int gid, String name, int tid, int cid) {
		this.setGid(gid);
		this.setName(name);
		this.setTeamId(tid);
		this.setCloakId(cid);
	}
	
	public JSONObject getJSONString() {
		JSONObject j = new JSONObject();
		j.put("gid", gid);
		j.put("guest_name", name);
		j.put("tid", teamId);
		j.put("cid", cloakId);
		
		return j;
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public int getCloakId() {
		return cloakId;
	}

	public void setCloakId(int cloakId) {
		this.cloakId = cloakId;
	}
	
	public String toString() {
		return name;
	}

}
