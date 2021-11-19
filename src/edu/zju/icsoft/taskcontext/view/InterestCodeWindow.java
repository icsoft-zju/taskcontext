package edu.zju.icsoft.taskcontext.view;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class InterestCodeWindow {

	private Composite composite;
	private Button btnClear;
	private Tree tree;
	private int step = 1;
	private ToolItem toolItem;
	public Tree getTree() {
		return tree;
	}

	public Button getBtnClear() {
		return btnClear;
	}

	public int getStep() {
		return step;
	}

	public ToolItem getToolItem() {
		return toolItem;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public InterestCodeWindow(Composite composite) {
		this.composite = composite;
	}

	/**
	 * Open the window.
	 */
	public void open() {
		createContents();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(100);
		fd_composite.right = new FormAttachment(85);
		fd_composite.top = new FormAttachment(0);
		fd_composite.left = new FormAttachment(0);
		composite.setLayoutData(fd_composite);
		composite.setLayout(new GridLayout(2, false));
		
		tree = new Tree(composite, SWT.BORDER);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		btnClear = new Button(composite, SWT.NONE);
		btnClear.setText("Clear");
		
		ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);
		GridData gd_toolBar = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_toolBar.widthHint = 85;
		toolBar.setLayoutData(gd_toolBar);
		
		toolItem = new ToolItem(toolBar, SWT.DROP_DOWN);
		toolItem.setText("Submit");
		
		Menu menu = new Menu(toolBar);
		
		MenuItem item1 = new MenuItem(menu, SWT.RADIO);
		item1.setText("1-step");
		item1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				step = 1;
			}
		});
		
		MenuItem item2 = new MenuItem(menu, SWT.RADIO);
		item2.setText("2-step");
		item2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				step = 2;
			}
		});
		MenuItem item3 = new MenuItem(menu, SWT.RADIO);
		item3.setText("3-step");
		item3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				step = 3;
			}
		});
		
		
		
		toolItem.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				if (event.detail == SWT.ARROW) {
					Rectangle rect = toolItem.getBounds();
					Point pt = new Point(rect.x, rect.y + rect.height);
					pt = toolBar.toDisplay(pt);
					menu.setLocation(pt.x, pt.y);
					menu.setVisible(true);
				}
			}
		});
	}
}
