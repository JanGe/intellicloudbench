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
