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
