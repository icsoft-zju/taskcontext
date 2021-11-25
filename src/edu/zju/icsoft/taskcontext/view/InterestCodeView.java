package edu.zju.icsoft.taskcontext.view;


import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.*;

import edu.zju.icsoft.taskcontext.Activator;
import edu.zju.icsoft.taskcontext.geometry.Graph;
import edu.zju.icsoft.taskcontext.geometry.Relationship;
import edu.zju.icsoft.taskcontext.geometry.ToolNode;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

public class InterestCodeView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.zju.icsoft.taskcontext.view.InterestCodeView";

	@Inject IWorkbench workbench;
	private static InterestCodeWindow window;
	
	private String cpath = (Activator.getDefault().getBundle().getLocation()+"src/images/class.png").substring(16);
	private String ipath = (Activator.getDefault().getBundle().getLocation()+"src/images/interface.png").substring(16);
	private String mpath = (Activator.getDefault().getBundle().getLocation()+"src/images/method.png").substring(16);
	private String fpath = (Activator.getDefault().getBundle().getLocation()+"src/images/field.png").substring(16);
	
	
	private Image cImage = new Image(Display.getCurrent(),cpath);
	private Image iImage = new Image(Display.getCurrent(),ipath);
	private Image mImage = new Image(Display.getCurrent(),mpath);
	private Image fImage = new Image(Display.getCurrent(),fpath);
	
	private static ArrayList<IMember> headnodes = new ArrayList<IMember>();
	private static ArrayList<IMember> leafnodes = new ArrayList<IMember>();
	private static ArrayList<IMember> allnodes = new ArrayList<IMember>();
	
	private ArrayList<Boolean> visit = new ArrayList<Boolean>();
	private static HashMap<String, String> map;
	private static ArrayList<Relationship> map1;
	
	private PredictCodeView predictCodeView=new PredictCodeView();
	private Graph gragh;
	
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		@Override
		public Image getImage(Object obj) {
			return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		window = new InterestCodeWindow(parent);
		window.open();
		Button btnClear = window.getBtnClear();
		
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				headnodes.clear();
				leafnodes.clear();
				allnodes.clear();
				buildTree();
			}
		});
		ToolItem toolItem = window.getToolItem();
		toolItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.detail != SWT.ARROW) {
					gragh = new Graph();
					gragh.setMap(map,map1);
					gragh.setMirrorNodes(allnodes);
					gragh.initial();
					System.out.println("expand");
					gragh.expand_step(window.getStep());
					System.out.println("complete expand");
					ArrayList<ToolNode>predictNodes = gragh.getPredictNodes();
					predictCodeView.setPredictCode(predictNodes);
					predictCodeView.show();
				}
				
			}
		});
	}
	
	public void addTreeNode(IMember selectedNode) {
		if(checkExist(selectedNode, allnodes)) {
			return;
		}
		else {
			allnodes.add(selectedNode);
		}
		headnodes.clear();
		leafnodes.clear();
		for(IMember member:allnodes) {
			if(member.getParent() instanceof ICompilationUnit) {
				headnodes.add(member);
			}
			else {
				IMember p=(IMember)member.getParent();
				if(!checkExist(p, allnodes) && member instanceof IType) {
					headnodes.add(member);
				}
				else {
					leafnodes.add(member);
				}
			}
		}
		visit.clear();
		for(int i = 0; i < leafnodes.size(); i++) {
			visit.add(false);
		}
		buildTree();
	}
	
	

	private boolean checkExist(IMember selectedNode,ArrayList<IMember> check) {
		for(IMember node : check) {
			if(node.getHandleIdentifier().equals(selectedNode.getHandleIdentifier())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkEdgeExist(Relationship relate,ArrayList<Relationship>ships) {
		for(Relationship ship:ships) {
			if(ship.getNode1().getHandleIdentifier().equals(relate.getNode1().getHandleIdentifier()) && ship.getNode2().getHandleIdentifier().equals(relate.getNode2().getHandleIdentifier()) && ship.getRelate().equals(relate.getRelate())) {
				return true;
			}
		}
		return false;
	}
	

	private void buildTree() {
		Tree myTree = window.getTree();
		myTree.setItemCount(0);
		for(IMember node : headnodes) {
			TreeItem treeItem = new TreeItem(myTree, SWT.NONE);
			treeItem.setText(node.getElementName());
			try {
				if(((IType)node).isInterface()) {
					treeItem.setImage(iImage);
				}
				else {
					treeItem.setImage(cImage);
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			addothernodes(treeItem,(IType)node);
			treeItem.setExpanded(true);
		}
		for(int i=0;i < leafnodes.size();i++) {
			if(visit.get(i))continue;
			TreeItem treeItem = new TreeItem(myTree, SWT.NONE);
			if(leafnodes.get(i) instanceof IMethod) {
				treeItem.setText(getMethodName((IMethod)leafnodes.get(i)));
			}
			else {treeItem.setText(leafnodes.get(i).getElementName());}
			if(leafnodes.get(i) instanceof IMethod) {
				treeItem.setImage(mImage);
			}
			else if(leafnodes.get(i) instanceof IField){
				treeItem.setImage(fImage); 
			}
		}
		
	}

	private void addothernodes(TreeItem treeItem, IType node) {
		IJavaElement[] cleaves=null;
		try {
			cleaves = node.getChildren();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < leafnodes.size(); i++) {
			if(visit.get(i))continue;
			for(IJavaElement element:cleaves) {
				
				if(((IMember)element).getHandleIdentifier().equals(((IMember)leafnodes.get(i)).getHandleIdentifier() )) {
					addnodes(treeItem,node,leafnodes.get(i),i);
					
				}
			}
		}
	}

	private void addnodes(TreeItem treeItem,IType item,IMember node,int num) {
		TreeItem newitemItem = new TreeItem(treeItem, SWT.NONE);
		if(node instanceof IMethod) {
			newitemItem.setText(getMethodName((IMethod)node));
		}
		else {newitemItem.setText(node.getElementName());}
		visit.set(num, true);
		if(node instanceof IMethod) {
			newitemItem.setImage(mImage);
		}
		else if(node instanceof IField){
			
			newitemItem.setImage(fImage); 
		}
		else {
			try {
				if(((IType)node).isInterface()) {
					newitemItem.setImage(iImage);
				}
				else {
					newitemItem.setImage(cImage);
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			addothernodes(newitemItem,(IType)node);
			newitemItem.setExpanded(true);
		}
		
	}


	@Override
	public void setFocus() {
	
	}

	private static String getMethodName(IMethod method) {
		String identi = method.toString().split(" \\[in ")[0];
		try {
			if(method.isConstructor()) {
				return identi;
			}
			else {
				String name = "";
				String[] total = identi.split(" ");
				ArrayList<String> paras = new ArrayList<String>();
				for(String str:total) {
					paras.add(str);
				}
				if(paras.get(0).equals("static")) {
					paras.remove(0);
				}
				name += paras.get(1);
				for(int i=2;i<paras.size();i++) {
					name += " "+paras.get(i);
				}
				name+=":"+paras.get(0);
				return name;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}
	public void setMap(HashMap<String, String> m1, ArrayList<Relationship> m2) {
		map=m1;
		map1=m2;
	}
}
