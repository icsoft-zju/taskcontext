package edu.zju.icsoft.taskcontext.geometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import edu.zju.icsoft.taskcontext.Activator;
import edu.zju.icsoft.taskcontext.gragh.Edge;
import edu.zju.icsoft.taskcontext.gragh.FreGraph;
import edu.zju.icsoft.taskcontext.gragh.SubGraph;
import edu.zju.icsoft.taskcontext.gragh.Vertical;

public class Graph {
	private static String path = (Activator.getDefault().getBundle().getLocation()+"src/pattern_mylyn").substring(16);
	private int nowId,step,total_match,maxVertical,minVertical;
	private boolean[] visit;
	
	private HashMap<String, String> map1;
	private ArrayList<Relationship> map2;
	
	private ArrayList<IMember>mirrorNodes;
	private ArrayList<ToolNode>toolNodes;
	private ArrayList<ToolNode>temp = new ArrayList<ToolNode>();
	
	private ArrayList<ToolRelation>toolRelates;
	
	private ArrayList<FreGraph>graphs = new ArrayList<FreGraph>();
	private ArrayList<SubGraph>subGraphs = new ArrayList<SubGraph>();
	
	
	private ArrayList<String> cutNodes = new ArrayList<String>();
	
	public Graph() {
		mirrorNodes = new ArrayList<IMember>();
		toolNodes = new ArrayList<ToolNode>();
		toolRelates = new ArrayList<ToolRelation>();
	}
	public void setMirrorNodes(ArrayList<IMember> allnodes) {
		
		this.mirrorNodes.clear();
		for(IMember node : allnodes) {
			this.mirrorNodes.add(node);
		}
	}
	
	public void initial() {
		toolNodes.clear();
		toolRelates.clear();
		graphs.clear();
		subGraphs.clear();
		cutNodes.clear();
		nowId = 0;
		total_match = 0;
		maxVertical = 0;
        minVertical = 9999;
        
        
		for(IMember member:mirrorNodes) {
			ToolNode node = new ToolNode(member);
			node.setId(nowId++);
			toolNodes.add(node);
		}
	}
	public void expand_step(int step) {
		this.step = step;
		for(ToolNode node : toolNodes) {
			node.setRemain_step(step);
		}
		
		for(int i = step ; i > 0; i-- ) {
			temp.clear();
			for(ToolNode node:toolNodes) {
				if(node.getRemain_step() != i)continue;
				if(node.getMember() instanceof IField) {
					expend_field(node,i);
				}
				else if(node.getMember() instanceof IMethod) {
					expand_method(node,i);
				}
				else {
					expend_type(node,i);
				}
			}
			for(ToolNode node:temp) {
				node.setId(nowId);
				toolNodes.add(node);
			}
		}
		
	}
	
	private void expend_field(ToolNode node, int step) {
		//declared by class
		ToolNode parent = new ToolNode((IMember)(node.getMember().getParent()));
		ToolRelation relate = new ToolRelation(parent, node, "declare");
		if(!checkNodeExist(parent, toolNodes) && !checkNodeExist(parent, temp)) {
			parent.setRemain_step(step-1);
			temp.add(parent);
		}
		if(!checkRelateExist(relate, toolRelates)) {
			toolRelates.add(relate);
		}
	}
	private void expand_method(ToolNode node, int step) {
		//declared by class
		ToolNode parent = new ToolNode((IMember)(node.getMember().getParent()));
		ToolRelation relate = new ToolRelation(parent, node, "declare");
		if(!checkNodeExist(parent, toolNodes) && !checkNodeExist(parent, temp)) {
			parent.setRemain_step(step-1);
			temp.add(parent);
		}
		if(!checkRelateExist(relate, toolRelates)) {
			toolRelates.add(relate);
		}
		//call and called methods
		IMember member = (IMember)node.getMember();
		
		for(Relationship ship:map2) {
			if(ship.getNode1().getHandleIdentifier().equals(member.getHandleIdentifier())) {
				ToolNode newNode = new ToolNode(ship.getNode2());
				ToolRelation newRelate = new ToolRelation(node, newNode, ship.getRelate());
				if(!checkNodeExist(newNode, toolNodes) && !checkNodeExist(newNode, temp)) {
					newNode.setRemain_step(step-1);
					temp.add(newNode);
				}
				if(!checkRelateExist(newRelate, toolRelates)) {
					toolRelates.add(newRelate);
				}
			}
			else if(ship.getNode2().getHandleIdentifier().equals(member.getHandleIdentifier())) {
				ToolNode newNode = new ToolNode(ship.getNode1());
				ToolRelation newRelate = new ToolRelation(newNode, node, ship.getRelate());
				if(!checkNodeExist(newNode, toolNodes) && !checkNodeExist(newNode, temp)) {
					newNode.setRemain_step(step-1);
					temp.add(newNode);
				}
				if(!checkRelateExist(newRelate, toolRelates)) {
					toolRelates.add(newRelate);
				}
			}
		}
	}
	private void expend_type(ToolNode node, int step) {
		//declared by class
		if(node.getMember().getParent() instanceof IType) {
			ToolNode parent = new ToolNode((IType)(node.getMember().getParent()));
			ToolRelation relate = new ToolRelation(parent, node, "declare");
			if(!checkNodeExist(parent, toolNodes) && !checkNodeExist(parent, temp)) {
				parent.setRemain_step(step-1);
				temp.add(parent);
			}
			if(!checkRelateExist(relate, toolRelates)) {
				toolRelates.add(relate);
			}
		}
		//declare
		try {
			IJavaElement[] children = node.getMember().getChildren();
			for(IJavaElement element : children) {
				if((element instanceof IField)||(element instanceof IMethod)||(element instanceof IType)) {
					ToolNode child = new ToolNode((IMember)element);
					ToolRelation relate = new ToolRelation(node, child, "declare");
					if(!checkNodeExist(child, toolNodes) && !checkNodeExist(child, temp)) {
						child.setRemain_step(step-1);
						temp.add(child);
					}
					if(!checkRelateExist(relate, toolRelates)) {
						toolRelates.add(relate);
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		//inherit and implement
		IMember member = (IMember)node.getMember();
		
		for(Relationship ship:map2) {
			if(ship.getNode1().getHandleIdentifier().equals(member.getHandleIdentifier())) {
				ToolNode newNode = new ToolNode(ship.getNode2());
				ToolRelation newRelate = new ToolRelation(node, newNode, ship.getRelate());
				if(!checkNodeExist(newNode, toolNodes) && !checkNodeExist(newNode, temp)) {
					newNode.setRemain_step(step-1);
					temp.add(newNode);
				}
				if(!checkRelateExist(newRelate, toolRelates)) {
					toolRelates.add(newRelate);
				}
			}
			else if(ship.getNode2().getHandleIdentifier().equals(member.getHandleIdentifier())) {
				ToolNode newNode = new ToolNode(ship.getNode1());
				ToolRelation newRelate = new ToolRelation(newNode, node, ship.getRelate());
				if(!checkNodeExist(newNode, toolNodes) && !checkNodeExist(newNode, temp)) {
					newNode.setRemain_step(step-1);
					temp.add(newNode);
				}
				if(!checkRelateExist(newRelate, toolRelates)) {
					toolRelates.add(newRelate);
				}
			}
		}
	
		
	}
	
	private boolean checkNodeExist(ToolNode node,ArrayList<ToolNode>check) {
		for(ToolNode cnode : check) {
			if(node.getMember().getHandleIdentifier().equals(cnode.getMember().getHandleIdentifier())) {
				return true;
			}
		}
		return false;
	}
	private boolean checkRelateExist(ToolRelation relate,ArrayList<ToolRelation> check) {
		for(ToolRelation ship:check) {
			if(relate.getNode1().getMember().getHandleIdentifier().equals(ship.getNode1().getMember().getHandleIdentifier()) && relate.getNode2().getMember().getHandleIdentifier().equals(ship.getNode2().getMember().getHandleIdentifier()) && relate.getRelate().equals(ship.getRelate())) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<ToolNode> getPredictNodes() {
		readPatterns();
		assignStereoType();
		System.out.println("split");
        split_subgragh();
        System.out.println("complete split");
        System.out.println("match");
        ArrayList<SubGraph>matchgragh= new ArrayList<SubGraph>();
        match(matchgragh);
        System.out.println("complete match");
        ArrayList<ToolNode>predictNodes = new ArrayList<ToolNode>();
		filter_expandNodes(matchgragh, predictNodes);
		confidence(predictNodes,matchgragh);
       
		return predictNodes;
	}
	
	
	private void readPatterns() {
		File file = new File(path);
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
            	String nowpath = path + "\\" +tempList[i].getName();
            	try {
					BufferedReader pattern = new BufferedReader(new InputStreamReader(new FileInputStream(nowpath)));
					String linestr;
					String[] line;
					
		            while ((linestr = pattern.readLine()) != null) {
		            	if(linestr.startsWith("t")) {
		            		FreGraph gragh = new FreGraph();
		            		while ((linestr = pattern.readLine()) != null && (linestr.startsWith("v")||linestr.startsWith("e"))) {
		            			if(linestr.startsWith("v")) {
				                	line = linestr.split(" ");
				                	Vertical v = new Vertical(Integer.parseInt(line[1]), line[2]);
				                	
				                	String stereo = line[2];
				                	if(!cutNodes.contains(stereo)) {
				                		cutNodes.add(stereo);
				                	}
				                	
				                	gragh.addV(v);
				                }
		            			else if(linestr.startsWith("e")) {
		            				line = linestr.split(" ");
		            				Vertical v1 = null,v2 =null;
		            				for(Vertical v:gragh.getVerticals()) {
		            					if(v.getId() == Integer.parseInt(line[1])) {
		            						v1 = v;
		            					}
		            					if(v.getId() == Integer.parseInt(line[2])) {
		            						v2 = v;
		            					}
		            				}
		            				Edge e = new Edge(v1,v2,line[3]);
		            				gragh.addE(e);
		            			}
		            		}
		            		gragh.setFreq(Integer.parseInt(linestr));
		            		if(gragh.getVerticals().size()>maxVertical) {
		            			maxVertical = gragh.getVerticals().size();
		            		}
		            		if(gragh.getVerticals().size()<minVertical) {
		            			minVertical = gragh.getVerticals().size();
		            		}
		            		Collections.sort(gragh.getVerticals());
		            		for(int j=0;j<gragh.getVerticals().size();j++) {
		            			gragh.getVerticals().get(j).setId(j);
		            		}
		            		graphs.add(gragh);
		            	}
		            }
		            pattern.close();
				} catch (Exception e) {
		            e.printStackTrace();
		            System.out.println("read files error");
		        }
            }
        }
	}
	
	private void assignStereoType() {
		for(ToolNode node:toolNodes) {
			if(node.getMember() instanceof IType || node.getMember() instanceof IMethod) {
				String stereoname = map1.get(node.getMember().getHandleIdentifier());
				node.setStereotype(stereoname);
			}
			else {
				node.setStereotype("FIELD");
			}
		}
		for(ToolRelation relate:toolRelates) {
			if(relate.getNode1().getStereotype() == null ) {
				for(ToolNode node:toolNodes) {
					if(node.getMember().getHandleIdentifier().equals(relate.getNode1().getMember().getHandleIdentifier())) {
						relate.setNode1(node);
					}
				}
			}
			if(relate.getNode2().getStereotype() == null ) {
				for(ToolNode node:toolNodes) {
					if(node.getMember().getHandleIdentifier().equals(relate.getNode2().getMember().getHandleIdentifier())) {
						relate.setNode2(node);
					}
				}
			}
		}
		for(int i=0;i<toolNodes.size();i++) {
			if(!cutNodes.contains(toolNodes.get(i).getStereotype())) {
				if(toolNodes.get(i).getId()<0) {
					toolNodes.remove(i);
					i--;
				}
				
			}
		}
		for(int i = 0 ; i < toolRelates.size() ; i++) {
			ToolRelation relate = toolRelates.get(i);
			String str1,str2;
			str1 = relate.getNode1().getStereotype();
			str2 = relate.getNode2().getStereotype();
			if(!(cutNodes.contains(str2)&&cutNodes.contains(str1))) {
				toolRelates.remove(i);
				i--;
			}
		}
	}
	
	private void split_subgragh() {
		nowId = 0;
		for(ToolNode node : toolNodes) {
			node.setId(nowId++);
		}
		visit = new boolean[toolNodes.size()];
        for(int i=0;i<toolNodes.size();i++) {
        	visit[i] = false;
        }
        if(minVertical<this.step+1) {
        	minVertical = this.step+1;
        }
		for(int num = minVertical; num <= maxVertical && num<=toolNodes.size(); num++) {
			for(int i = 0 ; i < toolNodes.size(); i++) {
				for(int j = i;j < toolNodes.size(); j++) {
					visit[j]=false;
				}
				SubGraph gragh = new SubGraph();
				dfs(i,num,gragh);
			}
		}
	}
	
	private void dfs(int i,int num,SubGraph gragh) {
		ToolNode nowvisit = toolNodes.get(i);
		visit[i] = true;
		gragh.addV(nowvisit);
		if(gragh.getVerticals().size() > num) {return;}
		if(gragh.getVerticals().size() == num) {
			//storage
			if(!checkGraghExist(gragh)) {
				SubGraph newGragh = new SubGraph();
				for(ToolNode node : gragh.getVerticals()){
					newGragh.addV(node);
				}
				Collections.sort(newGragh.getVerticals());
				for(ToolRelation ship : gragh.getEdges()) {
					newGragh.addE(ship);
				}
				for(ToolRelation ship : toolRelates) {
					if(checkNodeExist(ship.getNode1(), gragh.getVerticals()) && checkNodeExist(ship.getNode2(), gragh.getVerticals()) && !checkRelateExist(ship, gragh.getEdges())) {
						newGragh.addE(ship);
					}
				}
				subGraphs.add(newGragh);
			}
			return;
		}
		
		for(ToolRelation ship : toolRelates) {
			if(ship.getNode1().getId() == i && !visit[ship.getNode2().getId()]) {
				gragh.addE(ship);
				dfs(ship.getNode2().getId(),num,gragh);
				gragh.removeLast();
			}
			if(ship.getNode2().getId() == i && !visit[ship.getNode1().getId()]) {
				gragh.addE(ship);
				dfs(ship.getNode1().getId(),num,gragh);
				gragh.removeLast();
			}
		}
		
	}
	
	private boolean checkGraghExist(SubGraph gragh) {
		ArrayList<ToolNode> subVertice = new ArrayList<ToolNode>();
		for(ToolNode node:gragh.getVerticals()) {
			subVertice.add(node);
		};
		Collections.sort(subVertice);
		ArrayList<ToolNode> check;
		boolean t;
		for(SubGraph subgragh:subGraphs) {
			check = subgragh.getVerticals();
			if(check.size() != subVertice.size())continue;
			t=true;
			for(int i=0;i<check.size();i++) {
				if(check.get(i).getId() != subVertice.get(i).getId()) {
					t=false;
					break;
				}
			}
			if(t) return true;
		}
		return false;
	}

	private void match(ArrayList<SubGraph> matchgragh) {
		for(SubGraph graph:subGraphs) {
			for(FreGraph pattern: graphs) {
				if(match_fre_pattern(graph,pattern)) {
					graph.addMatch_time(1);
					
					if(!matchgragh.contains(graph)) {
						if(checkgraph(graph)) {
							matchgragh.add(graph);
						}
					}
				};
			}
		}
	}
	
	private boolean checkgraph(SubGraph graph) {
		ArrayList<ToolNode> nodes = graph.getVerticals();
		int check = mirrorNodes.size();
		for(ToolNode node:nodes) {
			if(node.getId()<check)return true;
		}
		return false;
	}
	private boolean match_fre_pattern(SubGraph gragh,FreGraph pattern) {
		if(gragh.getVerticals().size() != pattern.getVerticals().size()) {
			return false;
		}
		
		ArrayList<Vertical> pv = pattern.getVerticals();
		ArrayList<ToolNode> gv = gragh.getVerticals();
		for(int i = 0; i < pv.size(); i++) {
			if(! pv.get(i).getStereoType().equals(gv.get(i).getStereotype())) {
				return false;
			}
		}
		ArrayList<String>pKey = new ArrayList<String>();
		for(Edge e:pattern.getEdges()) {
			pKey.add(String.valueOf(e.getStartV().getId())+"->"+String.valueOf(e.getEndV().getId()));
		}
		
		ArrayList<ArrayList<Integer>>list=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer>nowList= new ArrayList<Integer>();
		nowList.add(0);
		for(int i=1;i<gv.size();i++) {
			if(gv.get(i).getStereotype().equals(gv.get(i-1).getStereotype())) {
				nowList.add(i);
			}
			else {
				list.add(nowList);
				nowList = new ArrayList<Integer>();
				nowList.add(i);
			}
		}
		list.add(nowList);
		
		ArrayList<ArrayList<ArrayList<Integer>>>totalList = new ArrayList<ArrayList<ArrayList<Integer>>>();
		for(int i=0;i<list.size();i++) {
			ArrayList<ArrayList<Integer>>nowIn = new ArrayList<ArrayList<Integer>>();
			if(list.get(i).size()==1) {
				nowIn.add(list.get(i));
			}
			else {
				nowIn = getPermutation(list.get(i));
			}
			totalList.add(nowIn);
		}
		
		ArrayList<ArrayList<Integer>>allmaps = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>>now = new ArrayList<ArrayList<Integer>>();
		getAllmaps(now,totalList,0,allmaps,totalList.size());
		//¶ÔÃ¿Ò»¸ömap½øÐÐ±È¶Ô
		for(ArrayList<Integer>l:allmaps) {
    		for(int i=0;i<gv.size();i++) {
    			gv.get(i).setMatchId(l.get(i));
    		}
    		ArrayList<String>gKey = new ArrayList<String>();
    		for(ToolRelation e:gragh.getEdges()) {
    			gKey.add(String.valueOf(e.getNode1().getMatchId())+"->"+String.valueOf(e.getNode2().getMatchId()));
    		}
    		boolean t = true;
			for(String str:pKey) {
				if(!gKey.contains(str)) {
					t=false;
					break;
				}
			}
			if(t)return true;
    	}
		return false;
	}
	
	private void getAllmaps(ArrayList<ArrayList<Integer>>now,ArrayList<ArrayList<ArrayList<Integer>>> total,int i,ArrayList<ArrayList<Integer>>results,int length) {
		if(i==length) {
			ArrayList<Integer>insert = new ArrayList<Integer>();
			for(ArrayList<Integer>list:now) {
				insert.addAll(list);
			}
			results.add(insert);
			return;
		}
		ArrayList<ArrayList<Integer>>nxt = (ArrayList<ArrayList<Integer>>)now.clone();
		for(ArrayList<Integer> item:total.get(i)) {
			nxt.add(item);
			getAllmaps(nxt, total, i+1, results, length);
			nxt.remove(item);
		}
	}

	private ArrayList<ArrayList<Integer>> getPermutation(ArrayList<Integer> arrayList) {
		ArrayList<ArrayList<Integer>>permu = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer>nowList = new ArrayList<Integer>();
		perBacktrace(nowList,arrayList,permu,arrayList.size());
		return permu;
	}
	
	private void perBacktrace(ArrayList<Integer> now,ArrayList<Integer> list,ArrayList<ArrayList<Integer>>total,int length) {
		if(now.size() == length) {
			total.add(now);
		}
		for(Integer item : list) {
			ArrayList<Integer>nxt = new ArrayList<Integer>();
			nxt = (ArrayList<Integer>)now.clone();
			nxt.add(item);
			ArrayList<Integer> remove = new ArrayList<Integer>();
			remove = (ArrayList<Integer>)list.clone();
			remove.remove(item);
			perBacktrace(nxt, remove, total,length);
		}
	}

	private void filter_expandNodes(ArrayList<SubGraph> matchgragh, ArrayList<ToolNode> predictNodes) {
		for(SubGraph g:matchgragh) {
			for(ToolNode node :g.getVerticals()) {
				if(node.getId() < mirrorNodes.size()) {
					continue;
				}
				if(predictNodes.contains(node)) {
					node.setMatch_time(node.getMatch_time()+g.getMatch_time());
				}
				else {
					node.setMatch_time(g.getMatch_time());
					predictNodes.add(node);
				}
			}
		}
	}

	private void confidence(ArrayList<ToolNode> predictNodes,ArrayList<SubGraph> matchgragh) {
		for(SubGraph g:matchgragh) {
			total_match+=g.getMatch_time();
		}
		for(ToolNode node : predictNodes) {
			node.setConfidence(1.0*node.getMatch_time()/total_match);
		}
	}
	public void setMap(HashMap<String, String> m1, ArrayList<Relationship> m2) {
		this.map1=m1;
		this.map2=m2;
	}
}
