package edu.zju.icsoft.taskcontext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.wayne.cs.severe.jstereocode.core.analyzer.ProjectInformation;
import edu.wayne.cs.severe.jstereocode.core.element.StereotypeIdentifier;
import edu.zju.icsoft.taskcontext.analyze.Analyzer;
import edu.zju.icsoft.taskcontext.geometry.Relationship;
import edu.zju.icsoft.taskcontext.view.InterestCodeView;

public class Handler extends AbstractHandler {
	private IJavaProject project=null;
	protected HashMap<String, ProjectInformation> projectsInfo;
	private HashMap<String, String>map=new HashMap<String, String>();
	private ArrayList<Relationship>relatemap = new ArrayList<Relationship>();
	private InterestCodeView interestCodeView = new InterestCodeView();
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	this.projectsInfo = new HashMap<String, ProjectInformation>();
		List<?> selectionList = ((IStructuredSelection) HandlerUtil.getActiveMenuSelection(event)).toList();
		if(selectionList.size()>1)return null;
		Object selected = selectionList.get(0);
		interestCodeView.addTreeNode((IMember)selected);
		
		
		IJavaProject pro = ((IMember)selected).getJavaProject();
		if(project == null ||! pro.getHandleIdentifier().equals(project.getHandleIdentifier())) {
			project = pro;
			IPackageFragment[] var8;
	        int var7;
			try {
				var7 = (var8 = project.getPackageFragments()).length;
				for(int var6 = 0; var6 < var7; ++var6) {
		            IPackageFragment packFragment = var8[var6];
		            stereotype(packFragment);
		        }
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			interestCodeView.setMap(map,relatemap);
		}
		
		
		
        return null;
    }

	private void stereotype(IPackageFragment packFragment) {
		try {
			ICompilationUnit[] var9;
	        int var8;
			var8 = (var9 = packFragment.getCompilationUnits()).length;
			for(int var7 = 0; var7 < var8; ++var7) {
	            ICompilationUnit unit = var9[var7];
	            StereotypeIdentifier identifier = new StereotypeIdentifier();
	            stereotype(unit,identifier);
	        }
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	private void stereotype(ICompilationUnit unit, StereotypeIdentifier identifier) {
		ProjectInformation projectInfo = this.getProjectInfo(unit.getJavaProject());
		identifier.setParameters(unit,projectInfo.getMethodsMean(), projectInfo.getMethodsStdDev());
        identifier.identifyStereotypes();
        
        Analyzer analyser = new Analyzer(identifier.getParser(), identifier.getStereotypedElements());
        analyser.update(map,relatemap);
	}

	protected ProjectInformation getProjectInfo(IJavaProject project) {
        ProjectInformation projectInformation;
        if (!this.projectsInfo.containsKey(project.getElementName())) {
            projectInformation = new ProjectInformation(project);
            projectInformation.compute();
            this.projectsInfo.put(project.getElementName(), projectInformation);
        } else {
            projectInformation = (ProjectInformation)this.projectsInfo.get(project.getElementName());
        }

        return projectInformation;
    }
}
