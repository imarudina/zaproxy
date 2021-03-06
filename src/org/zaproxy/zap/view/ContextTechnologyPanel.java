/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 * Note that this extension ane the other classes in this package are heavily 
 * based on the orriginal Paros ExtensionSpider! 
 */
package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;

public class ContextTechnologyPanel extends AbstractContextPropertiesPanel {

	private static final String PANEL_NAME = Constant.messages.getString("context.technology.title");
	private static final long serialVersionUID = -8337361808959321380L;

	private JPanel panelSession = null;
	private JScrollPane jScrollPane = null;
	private TreeModel model = null;
	private HashMap<Tech, DefaultMutableTreeNode> techToNodeMap = new HashMap<>();

	private JCheckBoxTree techTree = null;

	public static String getPanelName(int contextId) {
		// Panel names have to be unique, so precede with the context id
		return contextId + ": " + PANEL_NAME;
	}

	public ContextTechnologyPanel(Context context) {
		super(context.getIndex());
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setName(getPanelName(getContextIndex()));
		this.add(getPanelSession(), getPanelSession().getName());
	}

	/**
	 * This method initializes panelSession
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelSession() {
		if (panelSession == null) {

			panelSession = new JPanel();
			panelSession.setLayout(new GridBagLayout());
			panelSession.setName("SessionTech");

			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

			javax.swing.JLabel jLabel = new JLabel();

			jLabel.setText(Constant.messages.getString("context.technology.tree.root"));
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.gridheight = 1;
			gridBagConstraints1.insets = new java.awt.Insets(10, 0, 5, 0);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.weightx = 0.0D;

			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 0);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			panelSession.add(getJScrollPane(), gridBagConstraints2);
		}
		return panelSession;
	}

	private TreePath getPath(TreeNode node) {
		List<TreeNode> list = new ArrayList<>();

		// Add all nodes to list
		while (node != null) {
			list.add(node);
			node = node.getParent();
		}
		Collections.reverse(list);

		// Convert array of nodes to TreePath
		return new TreePath(list.toArray());
	}

	private JCheckBoxTree getTechTree() {
		if (techTree == null) {
			techTree = new JCheckBoxTree() {
				private static final long serialVersionUID = 1L;

				@Override
				protected void setExpandedState(TreePath path, boolean state) {
					// Ignore all collapse requests; collapse events will not be fired
					if (state) {
						super.setExpandedState(path, state);
					}
				}
			};
			// Initialise the structure based on all the tech we know about
			TechSet ts = new TechSet(Tech.builtInTech);
			Iterator<Tech> iter = ts.getIncludeTech().iterator();

			DefaultMutableTreeNode root = new DefaultMutableTreeNode("Technology");
			Tech tech;
			DefaultMutableTreeNode parent;
			DefaultMutableTreeNode node;
			while (iter.hasNext()) {
				tech = iter.next();
				if (tech.getParent() != null) {
					parent = techToNodeMap.get(tech.getParent());
				} else {
					parent = null;
				}
				if (parent == null) {
					parent = root;
				}
				node = new DefaultMutableTreeNode(tech.getName());
				parent.add(node);
				techToNodeMap.put(tech, node);
			}

			model = new DefaultTreeModel(root);
			techTree.setModel(model);
			techTree.expandAll();
			techTree.setCheckBoxEnabled(new TreePath(root), false);

		}
		return techTree;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTechTree());
			jScrollPane.setBorder(javax.swing.BorderFactory
					.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		return jScrollPane;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.contexts";
	}

	@Override
	public void initContextData(Session session, Context uiContext) {
		// Init model from context
		TechSet techSet = uiContext.getTechSet();
		Iterator<Entry<Tech, DefaultMutableTreeNode>> iter = techToNodeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Tech, DefaultMutableTreeNode> node = iter.next();
			TreePath tp = this.getPath(node.getValue());
			if (techSet.includes(node.getKey())) {
				this.getTechTree().check(tp, true);
			}
		}

	}

	@Override
	public void validateContextData(Session session) throws Exception {
		// Nothing to validate

	}

	@Override
	public void saveContextData(Session session) throws Exception {

		session.getContext(getContextIndex()).setTechSet(buildTechSet());

	}

	private TechSet buildTechSet(){
		TechSet techSet = new TechSet();

		Iterator<Entry<Tech, DefaultMutableTreeNode>> iter = techToNodeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Tech, DefaultMutableTreeNode> node = iter.next();
			TreePath tp = this.getPath(node.getValue());
			Tech tech = node.getKey();
			if (this.getTechTree().isSelectedFully(tp)) {
				techSet.include(tech);
			} else {
				techSet.exclude(tech);
			}
		}
		return techSet;
	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
		uiSharedContext.setTechSet(buildTechSet());
	}

}
