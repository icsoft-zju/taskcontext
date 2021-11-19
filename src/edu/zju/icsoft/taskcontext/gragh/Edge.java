package edu.zju.icsoft.taskcontext.gragh;

public class Edge{
	Vertical startV,endV;
	String relate;
	public Edge(Vertical startV,Vertical endV,String relate) {
		this.startV = startV;
		this.endV = endV;
		this.relate = relate;
	}
	
	public Vertical getStartV() {
		return startV;
	}

	public Vertical getEndV() {
		return endV;
	}

	public String getRelate() {
		return relate;
	}
	
}
