package edu.kit.aifb.IntelliCloudBench.ui;

import com.vaadin.data.Item.PropertySetChangeEvent;
import com.vaadin.data.Item.PropertySetChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Form;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import edu.kit.aifb.libIntelliCloudBench.model.Credentials;
import edu.kit.aifb.libIntelliCloudBench.model.Provider;

public class CredentialsWindow extends Window implements PropertySetChangeListener {
  private static final long serialVersionUID = -4280620633117853989L;
  
  BeanItem<Credentials> model;
  Provider provider;
  
  public CredentialsWindow(final Provider provider, Credentials credentials) {
  	this.provider = provider;
  	model = new BeanItem<Credentials>(credentials);
  	model.addListener(this);

  	setModal(true);
  	setWidth("400px");
  	setCaption(provider.getName());
  	
  	center();
  	
  	VerticalLayout layout = new VerticalLayout();
  	layout.setSpacing(true);
  	layout.setMargin(true);
  	setContent(layout);
  	
  	Form form = new Form();
  	form.setCaption("Please enter your Credentials:");
  	form.setItemDataSource(model);
  	form.setSizeFull();
  	layout.addComponent(form);
  	layout.setExpandRatio(form, 1.0f);
  	
  	Button save = new Button("Close");
  	save.setStyleName("big");
  	save.addListener(new ClickListener() {
      private static final long serialVersionUID = 5356614540664446119L;

			@Override
      public void buttonClick(ClickEvent event) {
			  provider.credentialsChanged();
	      close();
      }
  		
  	});
  	layout.addComponent(save);
  	layout.setComponentAlignment(save, Alignment.BOTTOM_RIGHT);
  }

	@Override
  public void itemPropertySetChange(PropertySetChangeEvent event) {
	  provider.credentialsChanged();
  }

}
