package edu.zju.icsoft.taskcontext.gragh;

import java.util.ArrayList;


public class FreGraph {
	private ArrayList<Vertical>verticals;
	private ArrayList<Edge>edges;
	private int freq;
	public ArrayList<Vertical> getVerticals() {
		return verticals;
	}
	public ArrayList<Edge> getEdges() {
		return edges;
	}
	public int getFreq() {
		return freq;
	}
	public void setFreq(int freq) {
		this.freq = freq;
	}
	public FreGraph() {
		verticals = new ArrayList<Vertical>();
		edges = new ArrayList<Edge>();
	}
	public void addV(Vertical v) {
		this.verticals.add(v);
	}
	public void addE(Edge e) {
		this.edges.add(e);
	}
	public void show() {
		for(Vertical v:verticals) {
			System.out.println(v.getId()+" "+v.getStereoType());
		}
		for(Edge e : edges) {
			int start = e.getStartV().getId();
			int end = e.getEndV().getId();
			System.out.println(start+" "+e.getRelate()+" "+end);
		}
		System.out.println(freq);
		System.out.println("~~~~~~");
	}
}
