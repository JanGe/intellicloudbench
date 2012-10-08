/*
* This file is part of IntelliCloudBench.
*
* Copyright (c) 2012, Jan Gerlinger <jan.gerlinger@gmx.de>
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* * Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimer.
* * Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimer in the
* documentation and/or other materials provided with the distribution.
* * Neither the name of the Institute of Applied Informatics and Formal
* Description Methods (AIFB) nor the names of its contributors may be used to
* endorse or promote products derived from this software without specific prior
* written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.kit.aifb.IntelliCloudBench.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import edu.kit.aifb.IntelliCloudBench.model.User;
import edu.kit.aifb.IntelliCloudBench.ui.tree.ProviderNodeComponent;
import edu.kit.aifb.libIntelliCloudBench.model.Credentials;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.Provider;

public class ServiceSelectionPanel extends Panel {

	private static final long serialVersionUID = 3800161433575870120L;

	private VerticalLayout content;

	private Collection<ProviderNodeComponent> allBranches = new HashSet<ProviderNodeComponent>();

	public ServiceSelectionPanel(String caption, User user) {
		super();
		Collection<InstanceType> checked = user.getUiState().getCheckedInstanceTypes();

		setCaption(caption);
		content = ((VerticalLayout) getContent());
		content.setSpacing(true);

		ProviderNodeComponent branch;
		BeanItem<Provider> model;

		Map<String, Credentials> credentialsForProviders = user.loadCredentialsForProvider();
		Credentials credentials;

		/* Provider tree */
		for (Provider provider : user.getService().getAllProviders()) {
			credentials = credentialsForProviders.get(provider.getId());
			if (credentials != null) {
				provider.getCredentials().setKey(credentials.getKey());
				provider.getCredentials().setSecret(credentials.getSecret());
			}
			model = new BeanItem<Provider>(provider);

			branch = new ProviderNodeComponent(user.getService(), model, checked);
			model.addListener(branch);
			provider.registerCredentialsChangedListener(user);

			branch.setWidth("100%");
			addComponent(branch);
			allBranches.add(branch);
			content.setComponentAlignment(branch, Alignment.TOP_CENTER);
		}
	}

	public List<InstanceType> getCheckedInstanceTypes() {
		List<InstanceType> allCheckedLeafs = new LinkedList<InstanceType>();
		for (ProviderNodeComponent regionNode : allBranches) {
			allCheckedLeafs.addAll(regionNode.getCheckedInstanceTypes());
		}
		return allCheckedLeafs;
	}

}
