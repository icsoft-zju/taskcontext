package edu.zju.icsoft.taskcontext.gragh;


public class Vertical implements Comparable{
	int id;
	String stereoType;
	public Vertical(int id,String stereoType) {
		this.id = id;
		this.stereoType = stereoType;
	}
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public String getStereoType() {
		return stereoType;
	}
	
	@Override
	public int compareTo(Object o) {
		Vertical s = (Vertical)o;
		return this.getStereoType().compareTo(s.getStereoType());
	}
	
}
